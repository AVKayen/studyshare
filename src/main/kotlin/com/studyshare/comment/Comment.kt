package com.studyshare.comment

import com.studyshare.task.ADDITIONAL_NOTES_MAX_LENGTH
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

val commentValidator = fun(content: String): String? {
    if(content.isEmpty()) {
        return "Comments must not be empty"
    }
    if (content.length > ADDITIONAL_NOTES_MAX_LENGTH) {
        return "Comment is too long (max length $ADDITIONAL_NOTES_MAX_LENGTH)"
    }
    return null
}

data class Comment(
    @BsonId
    val id: ObjectId = ObjectId(),

    val parentId: ObjectId,
    val authorName: String,
    val authorId: ObjectId,
    val content: String,

)