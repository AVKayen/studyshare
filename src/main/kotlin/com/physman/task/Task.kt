package com.physman.task

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

const val TITLE_MAX_LENGTH = 512
const val ADDITIONAL_NOTES_MAX_LENGTH = 512

val titleValidator = fun(title: String): String? {
     if(title.isEmpty()) {
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

data class Task(
    @BsonId
    val id: ObjectId = ObjectId(),

    val title: String,
    val additionalNotes: String? = null,
    val attachmentIds: List<ObjectId> = emptyList(),
)