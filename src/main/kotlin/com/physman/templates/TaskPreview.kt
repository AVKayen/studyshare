package com.physman.templates

import com.physman.task.Task
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskPreviewTemplate(task: Task) {
    article(classes = "flex-col task") {
        a(href = "/tasks/${task.id}") {
            h2 {
                +task.title
            }
        }
    }
}

