package com.physman.image

class InMemoryImageRepository : ImageRepository {
    private val images = mutableMapOf<String, Image>()

    override suspend fun createImage(image: Image, content: ByteArray): Image {
        images[image.id] = image
        return image
    }

    override suspend fun deleteImage(id: String) {
        images.remove(id)
    }

    override suspend fun getImageLink(id: String): String? {
        return images[id]?.serverLocation
    }
}