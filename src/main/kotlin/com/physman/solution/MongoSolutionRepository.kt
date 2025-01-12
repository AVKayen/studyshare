package com.physman.solution

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.physman.attachment.AttachmentRepository
import com.physman.forms.UploadFileData
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

// TODO: error handling
class MongoSolutionRepository(
    mongoDatabase: MongoDatabase,
    private val attachmentRepository: AttachmentRepository
) : SolutionRepository {

    private val solutionCollection = mongoDatabase.getCollection<Solution>("solutions")

    override suspend fun createSolution(solution: Solution, files: List<UploadFileData>) {

        val attachments = attachmentRepository.createAttachments(files)

        val solutionWithAttachments = solution.copy(
            attachmentIds = attachments.map { it.id }
        )
        solutionCollection.insertOne(solutionWithAttachments)
    }

    override suspend fun deleteSolution(id: ObjectId) {
        val solution = solutionCollection.findOneAndDelete(Filters.eq("_id", id)) ?: return
        attachmentRepository.deleteAttachments(solution.attachmentIds)
    }

    override suspend fun upvoteSolution(id: ObjectId, userId: ObjectId): Int {
        val filter = Filters.eq("_id", id)
        val updates = Updates.addToSet(Solution::upvotes.name, userId)

        val solution = solutionCollection.findOneAndUpdate(filter, updates) ?: return 0 // TODO: handle error (solution does not exist)
        if (solution.upvotes.contains(userId)) {
            return solution.upvoteCount() // TODO: handle error (user has already upvoted this solution)
        }

        return solution.upvoteCount() + 1
    }

    override suspend fun deleteSolutions(taskId: ObjectId) {
        val filter = Filters.eq(Solution::taskId.name, taskId)
        solutionCollection.find(filter).collect { solution: Solution ->
            attachmentRepository.deleteAttachments(solution.attachmentIds)
        }
        solutionCollection.deleteMany(filter)
    }

    override suspend fun getSolutions(taskId: ObjectId): List<SolutionView> {
        val filter = Filters.eq(Solution::taskId.name, taskId)
        return solutionCollection.find(filter).toList().map { solution: Solution ->
            SolutionView(
                solution = solution,
                attachments = attachmentRepository.getAttachments(solution.attachmentIds)
            )
        }
    }
}