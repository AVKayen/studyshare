package com.physman.solution

import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.physman.image.ImageRepository
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

// TODO implement images
class MongoSolutionRepository(
    mongoDatabase: MongoDatabase,
    private val imageRepository: ImageRepository
) : SolutionRepository {
    private val solutionCollection = mongoDatabase.getCollection<Solution>("solutions")

    override suspend fun createSolution(solution: Solution): Boolean {
        try {
            solutionCollection.insertOne(solution)
            return true
        } catch (e: MongoException) {
            return false
        }
    }

    override suspend fun deleteSolution(id: ObjectId): Boolean {
        return try {
            solutionCollection.deleteOne(Filters.eq("_id", id)).deletedCount == 1L
        } catch (e: MongoException) {
            false
        }
    }

    override suspend fun upvoteSolution(id: ObjectId, userId: ObjectId): Boolean {
        val filter = Filters.eq("_id", id)
        val updates = Updates.addToSet(Solution::upvotes.name, userId)

        return try {
            solutionCollection.updateOne(filter, updates).modifiedCount == 1L
        } catch (e: MongoException) {
            false
        }
    }

    override suspend fun deleteSolutions(taskId: ObjectId): Boolean {
        val filter = Filters.eq(Solution::taskId.name, taskId)
        return try {
            return solutionCollection.deleteMany(filter).deletedCount > 0
        } catch (e: MongoException) {
            false
        }
    }

    override suspend fun getSolutions(taskId: ObjectId): List<SolutionView> {
        val filter = Filters.eq(Solution::taskId.name, taskId)
        return solutionCollection.find(filter).toList().map { solution: Solution ->
            SolutionView(
                solution = solution,
                images = imageRepository.getImages(solution.imageIds)
            )
        }
    }
}