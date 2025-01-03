package com.physman.task

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
) {
    data class Solution(
        @BsonId
        @BsonRepresentation(BsonType.OBJECT_ID)
        val id: String = ObjectId().toHexString(),

        val title: String,
        val additionalNotes: String? = null,
        var upvotes: Int = 0,
        val images: List<String> = emptyList()
    ) : Comparable<Solution> {
        override fun compareTo(other: Solution): Int {
            return -1 * compareValues(this.upvotes, other.upvotes)
        }
    }
}