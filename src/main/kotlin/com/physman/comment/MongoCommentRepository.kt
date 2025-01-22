package com.physman.comment

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

// TODO: error handling
class MongoCommentRepository(
    mongoDatabase: MongoDatabase,
) : CommentRepository {

    private val commentCollection = mongoDatabase.getCollection<Comment>("comments")


    override suspend fun createComment(comment: Comment) {
        commentCollection.insertOne(comment)
    }

    override suspend fun getComments(parentId: ObjectId): List<Comment> {
        val filter = Filters.eq(Comment::parentId.name, parentId)
        return commentCollection.find(filter).toList()
    }

    override suspend fun deleteComment(id: ObjectId) {
        commentCollection.findOneAndDelete(Filters.eq("_id", id))
    }

    override suspend fun deleteComments(parentId: ObjectId) {
        val filter = Filters.eq(Comment::parentId.name, parentId)
        commentCollection.deleteMany(filter)
    }


}