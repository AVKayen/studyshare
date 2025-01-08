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

    override suspend fun getTasks(): List<TaskView> {
        return taskCollection.find().toList().map { task: Task ->
            TaskView(
                task =  task,
                images = imageRepository.getImages(task.imageIds)
            )
        }
    }

    override suspend fun getTask(id: ObjectId): TaskView? {
        val filter = Filters.eq("_id", id)
        val task = taskCollection.find(filter).firstOrNull() ?: return null
        return TaskView(
            task = task,
            images = imageRepository.getImages(task.imageIds)
        )
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