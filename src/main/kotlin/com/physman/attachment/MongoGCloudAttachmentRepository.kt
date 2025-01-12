package com.physman.attachment
import com.google.cloud.storage.*
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.physman.forms.UploadFileData
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import java.nio.file.Files


class MongoGCloudAttachmentRepository(private val bucketName: String, database: MongoDatabase) : AttachmentRepository {
    private val storage: Storage = StorageOptions.getDefaultInstance().service
    private val attachmentCollection = database.getCollection<Attachment>("attachments")


    private fun uploadAttachments(attachment: Attachment, content: ByteArray) {
        val blobName = attachment.blobName

//        var blob: Blob? = storage.get(bucketName, blobName)
//
//        if (blob == null) {
//            val blobId = BlobId.of(bucketName, blobName)
//            val blobInfo = BlobInfo.newBuilder(blobId).setContentType(attachment.mime ?: "").build()
//            blob = storage.create(blobInfo)
//        }
//
//        val precondition: Storage.BlobTargetOption = Storage.BlobTargetOption.generationMatch(blob!!.generation)
//        storage.create(blob, content, precondition)

        val blobId = BlobId.of(bucketName, blobName)
        val blobInfo = BlobInfo.newBuilder(blobId).setContentType(attachment.mime ?: "application/octet-stream").build()
        storage.create(blobInfo, content)
    }

    override suspend fun createAttachments(files: List<UploadFileData>): List<Attachment> {
        val attachments = mutableListOf<Attachment>()

        files.forEach { file: UploadFileData ->
            val attachment = Attachment(
                originalFilename = file.originalName,
                mime = file.mimeType
            )
            uploadAttachments(attachment, Files.readAllBytes(file.filePath))
            attachments.add(attachment)
        }

        if (attachments.size > 0) {
            attachmentCollection.insertMany(attachments)
        }

        return attachments
    }

    private fun deleteUploadedAttachment(attachment: Attachment) {
        val blob: Blob? = storage.get(bucketName, attachment.blobName)

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
            deleteUploadedAttachment(attachment)
        }

        attachmentCollection.deleteMany(filter)
    }


    override suspend fun getAttachments(attachmentIds: List<ObjectId>): List<AttachmentView> {
        val filter = Filters.`in`("_id", attachmentIds)
        return attachmentCollection.find(filter).toList().map { attachment: Attachment ->
            AttachmentView(
                attachment = attachment,
                link = getAttachmentLink(attachment)
            )
        }
    }

    // TODO create and return a real media link
    override suspend fun getAttachmentLink(attachment: Attachment): String {
//        val blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, attachment.blobName)).build()
//        val url = storage.signUrl(blobInfo, 30, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature())
//        return url.toString()
        return "/static/placeholder_image.jpg"
    }
}