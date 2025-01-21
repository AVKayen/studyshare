package com.physman.solution

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import com.physman.task.titleValidator
import com.physman.task.additionalNotesValidator

val titleValidator = titleValidator
val additionalNotesValidator = additionalNotesValidator

data class Solution(
    @BsonId
    val id: ObjectId = ObjectId(),

    val taskId: ObjectId,
    val title: String,
    val additionalNotes: String? = null,
    val commentAmount: Int = 0,
    val upvotes: List<ObjectId> = emptyList(), // Ids of users who upvoted the solution
    val downvotes: List<ObjectId> = emptyList(),
    val attachmentIds: List<ObjectId> = emptyList()
) : Comparable<Solution> {
    override fun compareTo(other: Solution): Int {
        return -1 * compareValues(this.voteCount(), other.voteCount())
    }

    fun voteCount(): Int {
        return upvotes.size - downvotes.size
    }

}