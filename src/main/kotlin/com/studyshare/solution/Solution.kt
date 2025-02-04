package com.studyshare.solution

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import com.studyshare.task.titleValidator
import com.studyshare.task.additionalNotesValidator
import com.studyshare.utils.Post

val titleValidator = titleValidator
val additionalNotesValidator = additionalNotesValidator

enum class VoteType {
    UPVOTE,
    DOWNVOTE
}

data class VoteUpdate (
    val isDownvoted: Boolean,
    val isUpvoted: Boolean,
    val voteCount: Int,
)

data class Solution(
    @BsonId
    override val id: ObjectId = ObjectId(),

    val taskId: ObjectId,
    override val title: String,
    override val authorName: String,
    override val authorId: ObjectId,
    override val groupName: String,
    override val groupId: ObjectId,
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