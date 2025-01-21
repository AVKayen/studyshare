package com.physman.attachment

import com.google.cloud.storage.*
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.physman.forms.UploadFileData
import kotlinx.coroutines.flow.toList
import kotlin.io.path.createTempFile
import net.coobird.thumbnailator.Thumbnails
import org.bson.types.ObjectId
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.time.ZonedDateTime.parse
import java.time.format.DateTimeFormatter.ofPattern
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

    override suspend fun createAttachments(files: List<UploadFileData>): List<Attachment> {
        val attachments = mutableListOf<Attachment>()

        files.forEach { file: UploadFileData ->
            val attachment = Attachment(
                originalFilename = file.originalName,
                mime = file.mimeType
            )
            uploadFile(attachment.blobName, attachment.mime, Files.readAllBytes(file.filePath))

            // Create and upload a thumbnail
            attachment.thumbnailBlobName?.let { thumbnailBlobName: String ->
                val thumbnailPath = createThumbnail(file.filePath)
                uploadFile(thumbnailBlobName, "image/jpg", Files.readAllBytes(thumbnailPath))
                thumbnailPath.deleteIfExists()
            }
            attachments.add(attachment)
        }

        if (attachments.size > 0) {
            attachmentCollection.insertMany(attachments)
        }

        return attachments
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

    override suspend fun deleteAttachments(attachmentIds: List<ObjectId>) {
        val filter = Filters.`in`("_id", attachmentIds)
        attachmentCollection.find(filter).collect { attachment: Attachment ->
            deleteUploadedFile(attachment.blobName)
            attachment.thumbnailBlobName?.let { thumbnailBlobName: String ->
                deleteUploadedFile(thumbnailBlobName)
            }
        }

        attachmentCollection.deleteMany(filter)
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

        var thumbnailUrl: String? = null
        attachment.thumbnailBlobName?.let { thumbnailBlobName: String ->
            if (attachment.cachedThumbnailUrl != null && isCachedUrlValid(attachment.cachedThumbnailUrl)) {
                thumbnailUrl = attachment.cachedThumbnailUrl
            } else {
                val newUrl = signUrl(thumbnailBlobName)
                updateCachedThumbnailUrl(attachment.id, newUrl)
                thumbnailUrl = newUrl
            }
        }

        return AttachmentView(
            attachment = attachment,
            url = url,
            thumbnailUrl = thumbnailUrl
        )
    }
}