package com.studyshare.templates

import com.studyshare.solution.Solution
import com.studyshare.task.Task
import com.studyshare.utils.Post
import kotlinx.html.*

enum class AccessLevel {
    NONE,
    DELETE,
    EDIT
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
        AccessLevel.EDIT -> {postEditingButton(post); postDeletionButton(post)}
    }
}