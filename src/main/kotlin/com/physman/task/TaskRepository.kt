package com.physman.task

interface TaskRepository {
    suspend fun getAllTasks(): List<Task>
    suspend fun createTask(task: Task): Task
    suspend fun getTask(id: Int): Task?
    suspend fun deleteTask(id: Int): Task?
    suspend fun updateTask(id: Int, taskUpdate: TaskUpdate): Task?
}