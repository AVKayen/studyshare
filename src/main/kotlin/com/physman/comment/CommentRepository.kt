package com.physman.comment

import org.bson.types.ObjectId

interface CommentRepository {
    suspend fun getComments(parentId: ObjectId): List<Comment>
    suspend fun createComment(comment: Comment)
    suspend fun deleteComment(id: ObjectId)
    suspend fun deleteComments(parentId: ObjectId)

    suspend fun getTaskIdFromSolution(solutionId: ObjectId) : ObjectId?
}

