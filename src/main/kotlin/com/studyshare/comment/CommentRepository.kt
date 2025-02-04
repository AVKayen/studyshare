package com.studyshare.comment

import org.bson.types.ObjectId

interface CommentRepository {
    suspend fun getComment(id: ObjectId): Comment?
    suspend fun getComments(parentId: ObjectId): List<Comment>
    suspend fun createComment(comment: Comment)
    suspend fun deleteComment(id: ObjectId)
    suspend fun deleteComments(parentId: ObjectId)
}

