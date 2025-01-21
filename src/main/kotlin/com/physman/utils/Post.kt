package com.physman.utils

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

abstract class Post{
    @get:BsonId
    abstract val id: ObjectId

    abstract val title: String
    abstract val additionalNotes: String?
    abstract val commentAmount: Int
    abstract val attachmentIds: List<ObjectId>
}