package com.studyshare.attachment

import com.google.cloud.storage.*
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.studyshare.forms.UploadFileData
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import net.coobird.thumbnailator.Thumbnails
import org.bson.types.ObjectId
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.ZonedDateTime.parse
import java.time.format.DateTimeFormatter.ofPattern
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists


class MongoGCloudAttachmentRepository(private val bucketName: String, database: MongoDatabase) : AttachmentRepository {
    private val storage: Storage = StorageOptions.getDefaultInstance().service
    private val attachmentCollection = database.getCollection<Attachment>("attachments")
    private val expiresAfterMinutes = 30L

    private fun uploadFile(blobName: String, mimeType: String?, content: ByteArray) {
        val blobId = BlobId.of(bucketName, blobName)
        val blobInfo = BlobInfo.newBuilder(blobId).setContentType(mimeType ?: "application/octet-stream").build()
        storage.create(blobInfo, content)
    }

    private fun createThumbnail(path: Path): Path {
        val thumbnailPath = createTempFile(suffix = ".jpg")
        Thumbnails.of(path.toFile())
            .size(450, 450) // Scale the image to fit in a 450x450 box
//            .outputQuality()
            .outputFormat("jpg")
            .toFile(thumbnailPath.toFile())

        return thumbnailPath
    }

    private fun processFileUpload(file: UploadFileData): Attachment {
        val attachment = Attachment(
            originalFilename = file.originalName
        )

        uploadFile(attachment.blobName, file.mimeType, Files.readAllBytes(file.filePath))

        if (file.mimeType?.startsWith("image/") == true) {
            try {
                val thumbnailPath = createThumbnail(file.filePath)

                uploadFile(attachment.thumbnailBlobName, "image/jpg", Files.readAllBytes(thumbnailPath))

                thumbnailPath.deleteIfExists()
                return attachment.copy(isImage = true) // File is an image and the thumbnail was created

            } catch (e: net.coobird.thumbnailator.tasks.UnsupportedFormatException) {
                return attachment
            }
        }
        return attachment
    }

    override suspend fun createAttachment(file: UploadFileData): AttachmentView {
        val attachment = processFileUpload(file)
        attachmentCollection.insertOne(attachment)
        return getAttachmentView(attachment)
    }

    override suspend fun createAttachments(files: List<UploadFileData>): List<AttachmentView> {

        val attachments = files.map { processFileUpload(it) }

        if (attachments.isNotEmpty()) {
            attachmentCollection.insertMany(attachments)
        }

        return attachments.map { getAttachmentView(it) }
    }

    private fun deleteUploadedFile(blobName: String) {
        val blob: Blob? = storage.get(bucketName, blobName)

        if (blob != null) {
            storage.delete(blob.blobId)
            return
        }

        // TODO: error logging
        throw Exception("Blob not found")
    }

    override suspend fun deleteAttachment(attachmentId: ObjectId) {
        val filter = Filters.eq("_id", attachmentId)
        val attachment = attachmentCollection.findOneAndDelete(filter) ?: return
        deleteUploadedFile(attachment.blobName)
        if (attachment.isImage) {
            deleteUploadedFile(attachment.thumbnailBlobName)
        }
    }

    override suspend fun deleteAttachments(attachmentIds: List<ObjectId>) {
        val filter = Filters.`in`("_id", attachmentIds)
        attachmentCollection.find(filter).collect { attachment: Attachment ->
            deleteUploadedFile(attachment.blobName)
            if (attachment.isImage) {
                deleteUploadedFile(attachment.thumbnailBlobName)
            }
        }

        attachmentCollection.deleteMany(filter)
    }

    override suspend fun getAttachment(attachmentId: ObjectId): AttachmentView? {
        val filter = Filters.eq("_id", attachmentId)
        val attachment = attachmentCollection.find(filter).firstOrNull() ?: return null
        return getAttachmentView(attachment)
    }

    override suspend fun getAttachments(attachmentIds: List<ObjectId>): List<AttachmentView> {
        val filter = Filters.`in`("_id", attachmentIds)
        return attachmentCollection.find(filter).toList().map { getAttachmentView(it) }
    }

    private fun isCachedUrlValid(cachedUrl: String): Boolean {
        val urlParts = cachedUrl.trim().split('&')
        val signatureTimeString = urlParts.find { it.startsWith("X-Goog-Date") }?.substringAfter("=")
        val signatureTime: Instant? = signatureTimeString?.let { parse(it, ofPattern("yyyyMMdd'T'HHmmssX")).toInstant() }
        val expirationTime: Instant? = signatureTime?.plusSeconds(expiresAfterMinutes * 60)
        return expirationTime != null && Instant.now().isBefore(expirationTime.minusSeconds(300))
    }

    private fun signUrl(blobName: String): String {
        val blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, blobName)).build()
        val url = storage.signUrl(
            blobInfo,
            expiresAfterMinutes,
            TimeUnit.MINUTES,
            Storage.SignUrlOption.withV4Signature()
        )
        return url.toString()
    }

    private suspend fun updateCachedUrl(attachmentId: ObjectId, url: String) {
        val filter = Filters.eq("_id", attachmentId)
        val updates = Updates.set(Attachment::cachedUrl.name, url)
        attachmentCollection.updateOne(filter, updates)
    }

    private suspend fun updateCachedThumbnailUrl(attachmentId: ObjectId, url: String) {
        val filter = Filters.eq("_id", attachmentId)
        val updates = Updates.set(Attachment::cachedThumbnailUrl.name, url)
        attachmentCollection.updateOne(filter, updates)
    }

    private suspend fun getAttachmentView(attachment: Attachment): AttachmentView {

        val url: String
        if (attachment.cachedUrl != null && isCachedUrlValid(attachment.cachedUrl)) {
            url = attachment.cachedUrl
        } else {
            val newUrl = signUrl(attachment.blobName)
            updateCachedUrl(attachment.id, newUrl)
            url = newUrl
        }

        if (!attachment.isImage) {
            return AttachmentView(
                attachment = attachment,
                url = url,
                thumbnailUrl = null
            )
        }

        val thumbnailUrl: String?

        if (attachment.cachedThumbnailUrl != null && isCachedUrlValid(attachment.cachedThumbnailUrl)) {
            thumbnailUrl = attachment.cachedThumbnailUrl
        } else {
            val newUrl = signUrl(attachment.thumbnailBlobName)
            updateCachedThumbnailUrl(attachment.id, newUrl)
            thumbnailUrl = newUrl
        }

        return AttachmentView(
            attachment = attachment,
            url = url,
            thumbnailUrl = thumbnailUrl
        )
    }
}