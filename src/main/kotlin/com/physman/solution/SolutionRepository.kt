package com.physman.solution

import com.physman.forms.UploadFileData
import org.bson.types.ObjectId

interface SolutionRepository {
    suspend fun getSolution(solutionId: ObjectId): Solution?
    suspend fun getSolutions(taskId: ObjectId, userId: ObjectId, resultCount: Int, lastId: ObjectId?): List<SolutionView>
    suspend fun createSolution(solution: Solution, files: List<UploadFileData>, userId: ObjectId): SolutionView
    suspend fun updateSolution(id: ObjectId, solutionView: SolutionView): SolutionView
    suspend fun deleteSolutions(taskId: ObjectId)
    suspend fun deleteSolution(id: ObjectId)

    suspend fun updateCommentAmount(solutionId: ObjectId, amount: Int): Int

    suspend fun vote(id: ObjectId, userId: ObjectId, voteType: VoteType): VoteUpdate?
}