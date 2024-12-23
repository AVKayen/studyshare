package com.physman.task

import com.physman.solution.Solution
import com.physman.solution.SolutionUpdate

interface TaskRepository {
    suspend fun getAllTasks(): List<Task>
    suspend fun createTask(task: Task): Task
    suspend fun getTask(id: Int): Task?
    suspend fun deleteTask(id: Int): Task?
    suspend fun updateTask(id: Int, taskUpdate: TaskUpdate): Task?

    suspend fun getAllSolutions(taskId: Int): MutableList<Solution>?
    suspend fun createSolution(taskId: Int, solution: Solution): Solution?
    suspend fun getSolution(taskId: Int, solutionId: Int): Solution?
    suspend fun deleteSolution(taskId: Int, solutionId: Int): Solution?
    suspend fun updateSolution(taskId: Int, solutionId: Int, solutionUpdate: SolutionUpdate): Solution?
}