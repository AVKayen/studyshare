package com.studyshare.task

import com.studyshare.post.Post
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

const val TITLE_MAX_LENGTH = 512
const val ADDITIONAL_NOTES_MAX_LENGTH = 512
const val CATEGORY_MAX_LENGTH = 30

val titleValidator = fun(title: String): String? {
     if (title.isEmpty()) {
        return "Title must not be empty"
     }
     if (title.length > TITLE_MAX_LENGTH) {
        return "Title too long (max length $TITLE_MAX_LENGTH)"
     }
     return null
}

val additionalNotesValidator = fun(additionalNotes: String): String? {
    if(additionalNotes.length > ADDITIONAL_NOTES_MAX_LENGTH) {
        return "Additional notes too long (max length $ADDITIONAL_NOTES_MAX_LENGTH)"
    }
    return null
}

val categoryValidator = fun(category: String): String? {
    if (category.isEmpty()) {
        return "Category must not be empty"
    }
    if (category.length > CATEGORY_MAX_LENGTH) {
        return "Category too long (max length $CATEGORY_MAX_LENGTH)"
    }
    return null
}

data class Task (
    @BsonId
    override val id: ObjectId = ObjectId(),

    override val title: String,
    override val authorName: String,
    override val authorId: ObjectId,
    override val groupName: String,
    override val groupId: ObjectId,
    override val additionalNotes: String? = null,
    override val commentAmount: Int = 0,
    override val attachmentIds: List<ObjectId> = emptyList(),
    val category: String
) : Post()