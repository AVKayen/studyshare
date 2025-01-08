package com.physman.solution

import org.bson.types.ObjectId

// TODO Add update method
interface SolutionRepository {
    suspend fun createSolution(solution: Solution): Boolean
    suspend fun deleteSolution(id: ObjectId): Boolean
//    suspend fun updateSolution(id: ObjectId, solutionUpdate: SolutionUpdate): Solution?
    suspend fun upvoteSolution(id: ObjectId, userId: ObjectId): Boolean
    suspend fun deleteSolutions(taskId: ObjectId): Boolean
    suspend fun getSolutions(taskId: ObjectId): List<Solution>
}