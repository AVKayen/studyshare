package com.physman.task

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Task(
    @BsonId
    val id: ObjectId = ObjectId(),

    val title: String,
    val additionalNotes: String? = null,
    val images: List<String> = emptyList(),
)