package com.studyshare.templates

import com.studyshare.solution.Solution
import com.studyshare.task.Task
import com.studyshare.post.Post
import kotlinx.html.*
import org.bson.types.ObjectId

enum class AccessLevel {
    NONE,
    DELETE,
    EDIT
}

fun getAccessLevel(userId: ObjectId, authorId: ObjectId, parentAuthorId: ObjectId? = null): AccessLevel {
    return when (userId) {
        authorId -> AccessLevel.EDIT
        parentAuthorId -> AccessLevel.DELETE
        else -> AccessLevel.NONE
    }
}

fun FlowContent.postDeletionButton(post: Post) {
    val url = when (post) {
        is Solution -> "/solutions/deletion-modal?solutionId=${post.id}"
        is Task -> "/${post.groupId}/deletion-modal?taskId=${post.id}"
        else -> throw IllegalArgumentException("Invalid post")
    }
    deletionButton(url)
}

fun FlowContent.postEditingButton(post: Post) {
    val url = when (post) {
        is Task -> "/tasks/editing-modal?taskId=${post.id}"
        is Solution -> "/solutions/editing-modal?id=${post.id}"
        else -> throw IllegalArgumentException("Invalid post")
    }
    editButton(url)
}

fun FlowContent.postActions(post: Post, accessLevel: AccessLevel) {
    when (accessLevel) {
        AccessLevel.NONE -> return
        AccessLevel.DELETE -> postDeletionButton(post)
        AccessLevel.EDIT -> {
            postEditingButton(post)
            postDeletionButton(post)
        }
    }
}