package com.physman.task

import org.bson.types.ObjectId

// TODO Add update method
interface TaskRepository {
    suspend fun getTasks(): List<TaskView>
    suspend fun createTask(task: Task): Boolean
    suspend fun getTask(id: ObjectId): TaskView?
    suspend fun deleteTask(id: ObjectId): Boolean
//    suspend fun updateTask(id: ObjectId, taskUpdate: TaskUpdate): Task?
}