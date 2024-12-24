package com.physman.image

interface ImageRepository {
    suspend fun createImage(image: Image): Image
    suspend fun deleteImage(id: String): Image?
    suspend fun getFile(id: String): Image?
}