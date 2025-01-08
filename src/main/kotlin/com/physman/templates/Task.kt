package com.physman.templates

import com.physman.image.ImageView
import com.physman.task.TaskView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskTemplate(taskView: TaskView) {
    article(classes = "flex-col task") {
        header {
            h2 {
                +taskView.task.title
            }
            a(href = "/solutions/creation-form?taskId=${taskView.task.id}") {
                +"Create solution"}
        }
        div {
            if (taskView.task.additionalNotes != null) {
                +"Notes: ${taskView.task.additionalNotes}"
            }
        }

        div {
            taskView.images.forEach { imageView: ImageView ->
                a(href=imageView.link) {
                    attributes["alt"] = imageView.image.originalFilename
                }
            }
        }
    }
}
