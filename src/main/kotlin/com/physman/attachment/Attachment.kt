package com.physman.attachment

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Attachment(
    @BsonId
    val id: ObjectId = ObjectId(),
    val originalFilename: String,
    val mime: String?,
    val blobName: String = "$id.${originalFilename.substringAfterLast('.')}",
    val cachedUrl: String? = null,
) {
    fun isImage(): Boolean {
        return mime?.startsWith("image/") ?: false
    }
}