package com.physman.repositories

import com.physman.models.Task
import com.physman.models.TaskUpdate

interface TaskRepository {
    suspend fun getAllTasks(): List<Task>
    suspend fun createTask(task: Task): Task
    suspend fun getTask(id: Int): Task?
    suspend fun deleteTask(id: Int): Task?
    suspend fun updateTask(id: Int, taskUpdate: TaskUpdate): Task?
}

object InMemoryTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()

    override suspend fun getAllTasks(): List<Task> = tasks

    override suspend fun getTask(id: Int): Task? {
        return tasks.find { it.id == id }
    }

    override suspend fun deleteTask(id: Int): Task? {
        val task: Task? = tasks.find { it.id == id }
        if (task == null) {
            return null
        }
        tasks.remove(task)
        return task
    }

    override suspend fun updateTask(id: Int, taskUpdate: TaskUpdate): Task? {
        val task: Task? = tasks.find { it.id == id }
        if (task == null) {
            return null
        }
        val updatedTask = task.copy(
            title = taskUpdate.title ?: task.title,
            additionalNotes = taskUpdate.additionalNotes ?: task.additionalNotes
        )
        tasks[tasks.indexOf(task)] = updatedTask
        return updatedTask
    }

    override suspend fun createTask(task: Task): Task {
        tasks.add(task)
        return tasks.last()
    }
}