package com.physman.task

import com.physman.solution.Solution
import com.physman.solution.SolutionUpdate

interface TaskRepository {
    suspend fun getAllTasks(): List<Task>
    suspend fun createTask(task: Task): Task
    suspend fun getTask(id: String): Task?
    suspend fun deleteTask(id: String): Task?
    suspend fun updateTask(id: String, taskUpdate: TaskUpdate): Task?

    suspend fun getAllSolutions(taskId: String): MutableList<Solution>?
    suspend fun createSolution(taskId: String, solution: Solution): Solution?
    suspend fun getSolution(taskId: String, solutionId: String): Solution?
    suspend fun deleteSolution(taskId: String, solutionId: String): Solution?
    suspend fun updateSolution(taskId: String, solutionId: String, solutionUpdate: SolutionUpdate): Solution?
    suspend fun upvoteSolution(taskId: String, solutionId: String): Solution?
}