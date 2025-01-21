package com.physman.solution

import com.physman.forms.UploadFileData
import org.bson.types.ObjectId

// TODO Add update method
interface SolutionRepository {
    suspend fun getSolution(solutionId: ObjectId): Solution?
    suspend fun getSolutions(taskId: ObjectId, userId: ObjectId): List<SolutionView>
    suspend fun createSolution(solution: Solution, files: List<UploadFileData>, userId: ObjectId): SolutionView
    //    suspend fun updateSolution(id: ObjectId, solutionUpdate: SolutionUpdate): Solution?
    suspend fun deleteSolutions(taskId: ObjectId)
    suspend fun deleteSolution(id: ObjectId)

    suspend fun upvote(id: ObjectId, userId: ObjectId): Int
    suspend fun downvote(id: ObjectId, userId: ObjectId): Int
    suspend fun removeUpvote(id: ObjectId, userId: ObjectId): Int
    suspend fun removeDownvote(id: ObjectId, userId: ObjectId): Int
}