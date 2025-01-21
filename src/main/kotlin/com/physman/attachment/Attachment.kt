package com.physman.attachment

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId


data class Attachment(
    @BsonId
    val id: ObjectId = ObjectId(),
    val originalFilename: String,
    val mime: String?,
    val cachedUrl: String? = null,
    val cachedThumbnailUrl: String? = null,
    val isImage: Boolean = false
) {
    val blobName: String
        get() = "$id.${originalFilename.substringAfterLast('.')}"

    val thumbnailBlobName: String
        get() {
            return "thumb.$blobName"
        }

    fun hasImageMimeType(): Boolean = mime?.startsWith("image/") ?: false
}