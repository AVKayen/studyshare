package com.physman.comment

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Comment(
    @BsonId
    val id: ObjectId = ObjectId(),

    val content: String,
    // TODO: add author field

)