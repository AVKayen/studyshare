package com.studyshare.comment

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.studyshare.utils.ResourceNotFoundException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class MongoCommentRepository(
    mongoDatabase: MongoDatabase,
) : CommentRepository {

    private val commentCollection = mongoDatabase.getCollection<Comment>("comments")


    override suspend fun getComment(id: ObjectId): Comment {
        val filter = Filters.eq("_id", id)
        return commentCollection.find(filter).firstOrNull() ?: throw ResourceNotFoundException()
    }

    override suspend fun getComments(parentId: ObjectId): List<Comment> {
        val filter = Filters.eq(Comment::parentId.name, parentId)
        return commentCollection.find(filter).toList()
    }

    override suspend fun createComment(comment: Comment) {
        commentCollection.insertOne(comment)
    }

    override suspend fun deleteComment(id: ObjectId) {
        commentCollection.deleteOne(Filters.eq("_id", id))
    }

    override suspend fun deleteComments(parentId: ObjectId) {
        val filter = Filters.eq(Comment::parentId.name, parentId)
        commentCollection.deleteMany(filter)
    }
}