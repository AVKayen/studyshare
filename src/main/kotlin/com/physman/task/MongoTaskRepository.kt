package com.physman.task

import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.physman.image.ImageRepository
import com.physman.solution.SolutionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

// TODO implement images
class MongoTaskRepository(
    mongoDatabase: MongoDatabase,
    private val imageRepository: ImageRepository,
    private val solutionRepository: SolutionRepository
) : TaskRepository {
    private val taskCollection = mongoDatabase.getCollection<Task>("tasks")

    override suspend fun createTask(task: Task): Boolean {
        try {
            taskCollection.insertOne(task)
            return true
        } catch (e: MongoException) {
            return false
        }
    }

    override suspend fun getTasks(): List<Task> {
        return taskCollection.find().toList()
    }

    override suspend fun getTask(id: ObjectId): Task? {
        val filter = Filters.eq("_id", id)
        return taskCollection.find(filter).firstOrNull()
    }

    override suspend fun deleteTask(id: ObjectId): Boolean {
        val filter = Filters.eq("_id", id)
        try {
            val success = taskCollection.deleteOne(filter).deletedCount == 1L
            if (success) {
                 return solutionRepository.deleteSolutions(taskId = id)
            }
            return false
        } catch (e: MongoException) {
            return false
        }
    }
}