package com.physman.image
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class MongoGCloudImageRepository(projectId: String, private val bucketName: String, database: MongoDatabase) : ImageRepository {
    private val storage: Storage = StorageOptions.newBuilder().setProjectId(projectId).build().service
    private val imageCollection = database.getCollection<Image>("images")


    override suspend fun createImage(image: Image, content: ByteArray): Image {
        val objectId = image.serverLocation
        var blob: Blob? = storage.get(bucketName, objectId)

        if (blob == null) {
            val blobId = BlobId.of(bucketName, objectId)
            val blobInfo = BlobInfo.newBuilder(blobId).build()
            blob = storage.create(blobInfo)
        }

        val precondition: Storage.BlobTargetOption = Storage.BlobTargetOption.generationMatch(blob!!.generation)

        storage.create(blob, content, precondition)
        imageCollection.insertOne(image)

        return image
    }

    override suspend fun deleteImage(id: String) {
        val image: Image = imageCollection.findOneAndDelete(Filters.eq("id", id)) ?: return
        val blob: Blob? = storage.get(bucketName, image.serverLocation)

        if (blob != null) {
            storage.delete(blob.blobId)
            return
        }

        // TODO: error logging
        throw Exception("Blob not found")
    }

    override suspend fun getImageLink(image: Image): String {
        return "https://storage.googleapis.com/$bucketName/${image.serverLocation}"
    }

    override suspend fun getImages(imageIds: List<ObjectId>): List<ImageView> {
        val filter = Filters.`in`("_id", imageIds)
        return imageCollection.find(filter).toList().map { image: Image ->
            ImageView(
                image = image,
                link = getImageLink(image)
            )
        }
    }
}