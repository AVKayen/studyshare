package com.physman.templates

import com.physman.task.TaskView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskPreviewTemplate(taskView: TaskView) {
    article(classes = "flex-col task") {
        a(href = "/${taskView.task.groupId}/${taskView.task.id}") {
            h2 {
                +taskView.task.title
            }
        }
    }
}

