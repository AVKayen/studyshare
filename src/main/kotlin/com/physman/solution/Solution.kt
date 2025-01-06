package com.physman.solution

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Solution(
    @BsonId
    val id: ObjectId = ObjectId(),

    val taskId: ObjectId,
    val title: String,
    val additionalNotes: String? = null,
    val upvotes: List<ObjectId> = emptyList(), // Ids of users who upvoted the solution
    val images: List<ObjectId> = emptyList()
) : Comparable<Solution> {
    override fun compareTo(other: Solution): Int {
        return -1 * compareValues(this.upvotes.size, other.upvotes.size)
    }

    fun upvoteCount(): Int {
        return upvotes.size
    }
}