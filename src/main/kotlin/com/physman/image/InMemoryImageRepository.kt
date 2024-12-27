package com.physman.image

class InMemoryImageRepository : ImageRepository {
    private val images = mutableMapOf<String, Image>()

    override suspend fun createImage(image: Image): Image {
        images[image.id] = image
        return image
    }

    override suspend fun deleteImage(id: String): Image? {
        return images.remove(id)
    }

    override suspend fun getFile(id: String): Image? {
        return images[id]
    }
}