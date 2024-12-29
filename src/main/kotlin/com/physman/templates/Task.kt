package com.physman.templates

import com.physman.task.Task
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskTemplate(task: Task) {
    article(classes = "flex-col task") {
        header {
            h2 {
                +task.title
            }
            a(href = "/tasks/${task.id}/solutions/creation-form") {
                +"Create solution"}
        }
        div {
            if (task.additionalNotes != null) {
                +"Notes: ${task.additionalNotes}"
            }
        }

        if (task.images.isNotEmpty()) {
            div {
                for (imageId in task.images) {
                    a(href = "/images/$imageId")
                }
            }
        }
    }
}
