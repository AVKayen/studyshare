package com.physman.image

interface ImageRepository {
    suspend fun createImage(image: Image, content: ByteArray): Image
    suspend fun deleteImage(id: String): Unit
    suspend fun getImageLink(id: String): String?
}