package com.physman.image

import java.util.UUID

object InMemoryImageRepository : ImageRepository {
    private val images = mutableMapOf<UUID, Image>()

    override suspend fun createImage(image: Image): Image {
        images[image.id] = image
        return image
    }

    override suspend fun deleteImage(id: UUID): Image? {
        return images.remove(id)
    }

    override suspend fun getFile(id: UUID): Image? {
        return images[id]
    }
}