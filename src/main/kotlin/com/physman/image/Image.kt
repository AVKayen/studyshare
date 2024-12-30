package com.physman.image

import org.bson.BsonType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonRepresentation
import org.bson.types.ObjectId

data class Image(
    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)

    val id: String = ObjectId().toHexString(),
    val originalFilename: String,

    val serverLocation: String = "$id.${originalFilename.substringAfterLast('.')}"
)