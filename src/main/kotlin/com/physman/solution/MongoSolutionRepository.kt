package com.physman.solution

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.physman.attachment.AttachmentRepository
import com.physman.comment.CommentRepository
import com.physman.forms.UploadFileData
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson
import org.bson.types.ObjectId

class MongoSolutionRepository(
    mongoDatabase: MongoDatabase,
    private val commentRepository: CommentRepository,
    private val attachmentRepository: AttachmentRepository
) : SolutionRepository {

    private val solutionCollection = mongoDatabase.getCollection<Solution>("solutions")

    override suspend fun createSolution(solution: Solution, files: List<UploadFileData>, userId: ObjectId): SolutionView {

        val attachments = attachmentRepository.createAttachments(files)

        val solutionWithAttachments = solution.copy(
            attachmentIds = attachments.map { it.attachment.id }
        )
        solutionCollection.insertOne(solutionWithAttachments)

        return SolutionView(
            solution = solutionWithAttachments,
            attachments = attachments,
            isUpvoted = solution.upvotes.contains(userId),
            isDownvoted = solution.downvotes.contains(userId)
        )
    }
    //TODO: consider attachments for update
    override suspend fun updateSolution(id: ObjectId, solutionView: SolutionView): SolutionView {
        val filter = Filters.eq("_id", id)
        println("repo1")

        val updates = Updates.combine(
            Updates.set(Solution::title.name, solutionView.solution.title),
            Updates.set(Solution::additionalNotes.name, solutionView.solution.additionalNotes)
        )
        println("repo2")
        //IT STOPS HERE
        solutionCollection.findOneAndUpdate(filter, updates)
        println("repo3")

        return solutionView

    }

    override suspend fun deleteSolution(id: ObjectId) {
        val solution = solutionCollection.findOneAndDelete(Filters.eq("_id", id)) ?: return
        attachmentRepository.deleteAttachments(solution.attachmentIds)
        commentRepository.deleteComments(id)
    }


    override suspend fun deleteSolutions(taskId: ObjectId) {
        val filter = Filters.eq(Solution::taskId.name, taskId)
        solutionCollection.find(filter).collect { solution: Solution ->
            attachmentRepository.deleteAttachments(solution.attachmentIds)
            commentRepository.deleteComments(solution.id)
        }
        solutionCollection.deleteMany(filter)
    }

    override suspend fun getSolutions(taskId: ObjectId, userId: ObjectId, resultCount: Int, lastId: ObjectId?): List<SolutionView> {
        val filter = if (lastId != null) {
            Filters.and(
                Filters.eq(Solution::taskId.name, taskId),
                Filters.lt("_id", lastId)
            )
        } else {
            Filters.eq(Solution::taskId.name, taskId)
        }
        val sort = Sorts.descending("_id")
        return solutionCollection.find(filter).sort(sort).limit(resultCount).toList().map { solution: Solution ->
            SolutionView(
                solution = solution,
                attachments = attachmentRepository.getAttachments(solution.attachmentIds),
                isUpvoted = solution.upvotes.contains(userId),
                isDownvoted = solution.downvotes.contains(userId)
            )
        }
    }

    override suspend fun getSolution(solutionId: ObjectId, userId: ObjectId): SolutionView? {
        val filter = Filters.eq("_id", solutionId)
        val solution = solutionCollection.find(filter).firstOrNull() ?: return null
        return SolutionView(
                solution = solution,
                attachments = attachmentRepository.getAttachments(solution.attachmentIds),
                isUpvoted = solution.upvotes.contains(userId),
                isDownvoted = solution.downvotes.contains(userId)
            )
    }



    override suspend fun updateCommentAmount(solutionId: ObjectId, amount: Int): Int {
        val filter = Filters.eq("_id", solutionId)
        val updates = Updates.inc(Solution::commentAmount.name, amount)

        val solution = solutionCollection.findOneAndUpdate(filter, updates) ?: return 0

        return solution.commentAmount + 1
    }

    override suspend fun vote(id: ObjectId, userId: ObjectId, voteType: VoteType): VoteUpdate? {
        val filter = Filters.eq("_id", id)
        val solution = solutionCollection.find(filter).firstOrNull() ?: return null

        val removeDownvote = Updates.pull(Solution::downvotes.name, userId)
        val removeUpvote = Updates.pull(Solution::upvotes.name, userId)
        val addDownvote = Updates.addToSet(Solution::downvotes.name, userId)
        val addUpvote = Updates.addToSet(Solution::upvotes.name, userId)

        val wasUpvoted = solution.upvotes.contains(userId)
        val wasDownvoted = solution.downvotes.contains(userId)

        var isDownvoted = false
        var isUpvoted = false
        var voteCount = solution.voteCount()

        val updates: MutableList<Bson> = mutableListOf()

        if (wasDownvoted) {
            updates.add(removeDownvote)
            voteCount += 1
        }
        if (wasUpvoted) {
            updates.add(removeUpvote)
            voteCount -= 1
        }
        when (voteType) {
            VoteType.UPVOTE ->
                if (!wasUpvoted) {
                    updates.add(addUpvote)
                    isUpvoted = true
                    voteCount += 1
                }
            VoteType.DOWNVOTE ->
                if (!wasDownvoted) {
                    updates.add(addDownvote)
                    isDownvoted = true
                    voteCount -= 1
                }
        }

        solutionCollection.updateOne(filter, Updates.combine(updates))

        return VoteUpdate(
            isDownvoted,
            isUpvoted,
            voteCount
        )
    }
}