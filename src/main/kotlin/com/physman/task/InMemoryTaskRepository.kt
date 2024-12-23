package com.physman.task

import com.physman.solution.Solution
import com.physman.solution.SolutionUpdate

object InMemoryTaskRepository : TaskRepository {
    private val solutions1 = mutableListOf(
        Solution(1, "Odp:14", "bo tak ;)"),
        Solution(2, "Nie wiem", "lol")
    )
    private val solutions2 = mutableListOf(
        Solution(1, "Odp:slon", "ciezki jest"))



    private val tasks = mutableListOf<Task>(
        Task(1, "zadanie 49 zbiór XYZ", solutions = solutions1),
        Task(2, "zad 52", "tylko a) i b)", solutions = solutions2)
    )

    override suspend fun getAllTasks(): List<Task> = tasks

    override suspend fun getTask(id: Int): Task? {
        return tasks.find { it.id == id }
    }

    override suspend fun createTask(task: Task): Task {
        tasks.add(task)
        return tasks.last()
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



// SOLUTION
    override suspend fun getAllSolutions(taskId: Int): MutableList<Solution>? {
        val task: Task? = getTask(taskId)
        if (task == null) {
            return null
        }
        return task.solutions
    }

    override suspend fun getSolution(taskId: Int, solutionId: Int): Solution? {
        val allSolutions = getAllSolutions(taskId)
        if (allSolutions == null) {
            return null
        }

        val solution: Solution? = allSolutions.find { it.id == solutionId }
        if (solution == null) {
            return null
        }
        return solution
    }

    override suspend fun createSolution(taskId: Int, solution: Solution): Solution? {
        val task = getTask(taskId)
        if (task == null) {
            return null
        }
        task.solutions.add(solution)
        return task.solutions.last()
    }

    override suspend fun deleteSolution(taskId: Int, solutionId: Int): Solution? {
        val solution = getSolution(taskId, solutionId)
        val task = getTask(taskId)
        if (task == null) {
            return null
        }
        task.solutions.remove(solution)
        return solution
    }

    override suspend fun updateSolution(taskId: Int, solutionId: Int, solutionUpdate: SolutionUpdate): Solution? {
        val solution = getSolution(taskId, solutionId)
        val updatedSolution = solution?.copy(
            title = solutionUpdate.title ?: solution.title,
            additionalNotes = solutionUpdate.additionalNotes ?: solution.additionalNotes
        )
        if (updatedSolution == null) {
            return null
        }

        deleteSolution(taskId, solutionId)
        createSolution(taskId, updatedSolution)
        return updatedSolution
    }


}