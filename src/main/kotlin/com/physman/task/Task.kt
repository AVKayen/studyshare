package com.physman.task

import com.physman.solution.Solution
import org.bson.BsonType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonRepresentation
import org.bson.types.ObjectId

data class Task(
    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)
    val id: String = ObjectId().toHexString(),

    val title: String,
    val additionalNotes: String? = null,
    val images: List<String> = emptyList(),

    val solutions: MutableList<Solution> = mutableListOf()
)