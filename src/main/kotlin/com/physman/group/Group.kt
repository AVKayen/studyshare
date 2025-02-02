package com.physman.group

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Group(
    @BsonId
    val id: ObjectId = ObjectId(),
    val title: String,
    val description: String? = null,
    val leaderId: ObjectId,
    val memberIds: List<ObjectId>,
    val thumbnailId: ObjectId?, // ObjectId pointing to an Attachment
    val taskCategories: List<String> = emptyList()
)
