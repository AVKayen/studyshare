package com.studyshare.solution

import com.studyshare.forms.UploadFileData
import com.studyshare.task.Task
import org.bson.types.ObjectId

interface SolutionRepository {
    suspend fun getSolutionView(solutionId: ObjectId, userId: ObjectId): SolutionView
    suspend fun getSolutionViews(taskId: ObjectId, userId: ObjectId, resultCount: Int, lastId: ObjectId?): List<SolutionView>
    suspend fun createSolution(solution: Solution, files: List<UploadFileData>, userId: ObjectId): SolutionView
    suspend fun updateSolution(id: ObjectId, userId: ObjectId, solutionUpdates: SolutionUpdates): SolutionView
    suspend fun deleteSolutions(taskId: ObjectId)
    suspend fun deleteSolution(id: ObjectId, userId: ObjectId, parentTask: Task)
    suspend fun updateCommentAmount(solutionId: ObjectId, amount: Int): Int
    suspend fun vote(id: ObjectId, userId: ObjectId, voteType: VoteType): VoteUpdate
}