package com.physman.image

import java.util.UUID

interface ImageRepository {
    suspend fun createImage(image: Image): Image
    suspend fun deleteImage(id: UUID): Image?
    suspend fun getFile(id: UUID): Image?
}