package com.physman.solution

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import com.physman.task.titleValidator
import com.physman.task.additionalNotesValidator
import com.physman.utils.Post

val titleValidator = titleValidator
val additionalNotesValidator = additionalNotesValidator

data class Solution(
    @BsonId
    override val id: ObjectId = ObjectId(),

    val taskId: ObjectId,
    override val title: String,
    override val authorName: String,
    override val authorId: ObjectId,
    override val additionalNotes: String? = null,
    override val commentAmount: Int = 0,
    val upvotes: List<ObjectId> = emptyList(), // Ids of users who upvoted the solution
    val downvotes: List<ObjectId> = emptyList(),
    override val attachmentIds: List<ObjectId> = emptyList()
) : Post(), Comparable<Solution> {
    override fun compareTo(other: Solution): Int {
        return -1 * compareValues(this.voteCount(), other.voteCount())
    }

    fun voteCount(): Int {
        return upvotes.size - downvotes.size
    }

}