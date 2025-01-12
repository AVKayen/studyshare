package com.physman.image
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull

class MongoGCloudImageRepository(projectId: String, private val bucketName: String, database: MongoDatabase) : ImageRepository {
    private val storage: Storage = StorageOptions.newBuilder().setProjectId(projectId).build().service
    private val images = database.getCollection<Image>("images")


    override suspend fun createImage(image: Image, content: ByteArray, mimeType: String): Image {
        val objectId = image.serverLocation
        var blob: Blob? = storage.get(bucketName, objectId)

        if (blob == null) {
            val blobId = BlobId.of(bucketName, objectId)
            val blobInfo = BlobInfo.newBuilder(blobId).setContentType(mimeType).build()
            blob = storage.create(blobInfo)
        }

        val precondition: Storage.BlobTargetOption = Storage.BlobTargetOption.generationMatch(blob!!.generation)

        storage.create(blob, content, precondition)
        images.insertOne(image)

        return image
    }

    override suspend fun deleteImage(id: String) {
        val image: Image = images.findOneAndDelete(eq("id", id)) ?: return
        val blob: Blob? = storage.get(bucketName, image.serverLocation)

        if (blob != null) {
            storage.delete(blob.blobId)
            return
        }

        // TODO: error logging
        throw Exception("Blob not found")
    }

    override suspend fun getImageLink(id: String): String? {
        val image: Image = images.find(eq("id", id)).firstOrNull() ?: return null
        return "https://storage.googleapis.com/$bucketName/${image.serverLocation}"
    }
}