package com.physman.templates

import com.physman.task.Task
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskPreviewTemplate(task: Task) {
    a(href = "/${task.groupId}/${task.id}") {
        h2 {
            classes = setOf("task-preview-title")
            +task.title
        }
    }
    cite {
        +task.authorName
    }
    task.additionalNotes?.take(100)?.let {
        p {
            +it
            if (task.additionalNotes.length >= 100) + "..."
        }
    }
    hr {
        classes = setOf("task-preview-hr")
    }
}

