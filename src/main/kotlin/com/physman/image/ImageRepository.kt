package com.physman.image

import org.bson.types.ObjectId

interface ImageRepository {
    suspend fun createImage(image: Image, content: ByteArray): Image
    suspend fun deleteImage(id: String)
    suspend fun getImageLink(image: Image): String
    suspend fun getImages(imageIds: List<ObjectId>): List<ImageView>
}