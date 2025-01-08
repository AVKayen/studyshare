package com.physman.task

//class InMemoryTaskRepository : TaskRepository {
//    private val solutions1 = mutableListOf(
//        Task.Solution(title = "Odp:14", additionalNotes = "bo tak ;)"),
//        Task.Solution(title = "Nie wiem", additionalNotes = "lol")
//    )
//    private val solutions2 = mutableListOf(
//        Task.Solution(title =  "Odp:slon", additionalNotes = "ciezki jest"))
//
//
//
//    private val tasks = mutableListOf(
//        Task(id = "123", title = "zadanie 49 zbiór XYZ", solutions = solutions1),
//        Task(id = "321", title = "zad 52", additionalNotes = "tylko a) i b)", solutions = solutions2)
//    )
//
//    override suspend fun getTasks(): List<Task> = tasks
//
//    override suspend fun getTask(id: String): Task? {
//        return tasks.find { it.id == id }
//    }
//
//    override suspend fun createTask(task: Task) {
//        tasks.add(task)
//    }
//
//    override suspend fun deleteTask(id: String): Boolean {
//        val task: Task? = tasks.find { it.id == id }
//        if (task == null) {
//            return false
//        }
//        tasks.remove(task)
//        return true
//    }
//
//    override suspend fun updateTask(id: String, taskUpdate: TaskUpdate): Task? {
//        val task: Task? = tasks.find { it.id == id }
//        if (task == null) {
//            return null
//        }
//        val updatedTask = task.copy(
//            title = taskUpdate.title ?: task.title,
//            additionalNotes = taskUpdate.additionalNotes ?: task.additionalNotes
//        )
//        tasks[tasks.indexOf(task)] = updatedTask
//        return updatedTask
//    }
//
//
//
//// SOLUTION
//    override suspend fun getSolutions(taskId: String): List<Task.Solution>? {
//        val task: Task = getTask(taskId) ?: return null
//        return task.solutions
//    }
//
//    override suspend fun getSolution(taskId: String, solutionId: String): Task.Solution? {
//        val allSolutions = getSolutions(taskId) ?: return null
//
//        val solution: Task.Solution? = allSolutions.find { it.id == solutionId }
//        if (solution == null) {
//            return null
//        }
//        return solution
//    }
//
//    override suspend fun createSolution(taskId: String, solution: Task.Solution): Boolean {
//        val task = getTask(taskId) ?: return false
//        task.solutions.add(solution)
//        return true
//    }
//
//    override suspend fun deleteSolution(taskId: String, solutionId: String): Task.Solution? {
//        val solution = getSolution(taskId, solutionId)
//        val task = getTask(taskId) ?: return null
//        task.solutions.remove(solution)
//        return solution
//    }
//
//    override suspend fun updateSolution(taskId: String, solutionId: String, solutionUpdate: TaskUpdate.SolutionUpdate): Task.Solution? {
//        val solution = getSolution(taskId, solutionId)
//        val updatedSolution = solution?.copy(
//            title = solutionUpdate.title ?: solution.title,
//            additionalNotes = solutionUpdate.additionalNotes ?: solution.additionalNotes
//        )
//        if (updatedSolution == null) {
//            return null
//        }
//
//        deleteSolution(taskId, solutionId)
//        createSolution(taskId, updatedSolution)
//        return updatedSolution
//    }
//
//    override suspend fun upvoteSolution(taskId: String, solutionId: String): Task.Solution? {
//       val solution = getSolution(taskId, solutionId) ?: return null
//        solution.upvotes += 1
//        return solution
//    }
//}