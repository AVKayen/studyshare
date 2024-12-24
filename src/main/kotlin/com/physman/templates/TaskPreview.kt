package com.physman.templates

import com.physman.task.Task
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskPreviewTemplate(task: Task) {
    article(classes = "flex-col task") {
        header {
            a(href = "/tasks/${task.id}") {
                h2 {
                    +task.title
                }
            }
        }
        div {
            if (task.additionalNotes != null) {
                println(task.additionalNotes)
                +"Notes: ${task.additionalNotes}"
            }
        }

        if (task.images.isNotEmpty()) {
            div {
                a(href = "/images/${task.images.first()}")
            }
        }
    }
}

