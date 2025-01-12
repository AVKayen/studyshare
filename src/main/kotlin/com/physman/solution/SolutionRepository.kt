package com.physman.solution

import com.physman.forms.UploadFileData
import org.bson.types.ObjectId

// TODO Add update method
interface SolutionRepository {
    suspend fun createSolution(solution: Solution, files: List<UploadFileData>)
    suspend fun deleteSolution(id: ObjectId)
//    suspend fun updateSolution(id: ObjectId, solutionUpdate: SolutionUpdate): Solution?
    suspend fun upvoteSolution(id: ObjectId, userId: ObjectId)
    suspend fun deleteSolutions(taskId: ObjectId)
    suspend fun getSolutions(taskId: ObjectId): List<SolutionView>
}