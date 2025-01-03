package com.physman.task

interface TaskRepository {
    suspend fun getAllTasks(): List<Task>
    suspend fun createTask(task: Task): Task
    suspend fun getTask(id: String): Task?
    suspend fun deleteTask(id: String): Task?
    suspend fun updateTask(id: String, taskUpdate: TaskUpdate): Task?

    suspend fun getAllSolutions(taskId: String): MutableList<Task.Solution>?
    suspend fun createSolution(taskId: String, solution: Task.Solution): Task.Solution?
    suspend fun getSolution(taskId: String, solutionId: String): Task.Solution?
    suspend fun deleteSolution(taskId: String, solutionId: String): Task.Solution?
    suspend fun updateSolution(taskId: String, solutionId: String, solutionUpdate: TaskUpdate.SolutionUpdate): Task.Solution?
    suspend fun upvoteSolution(taskId: String, solutionId: String): Task.Solution?
}