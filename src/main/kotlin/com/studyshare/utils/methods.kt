package com.studyshare.utils

import com.studyshare.templates.AccessLevel
import org.bson.types.ObjectId

fun className(any: Any): String {
    val str = any.javaClass.toString().lowercase()
    val lastDotIndex = str.lastIndexOf('.')
    return if (lastDotIndex != -1) {
        str.substring(lastDotIndex + 1)
    } else {
        str
    }
}

fun getAccessLevel(userId: ObjectId, authorId: ObjectId, parentAuthorId: ObjectId? = null): AccessLevel {
    return when (userId) {
        authorId -> AccessLevel.EDIT
        parentAuthorId -> AccessLevel.DELETE
        else -> AccessLevel.NONE
    }
}
