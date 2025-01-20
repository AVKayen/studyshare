package com.physman.templates

import com.physman.attachment.AttachmentView
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
            taskView.attachments.forEach { attachmentView: AttachmentView ->
                if (attachmentView.attachment.isImage()) {
                    img(src = attachmentView.url, alt = attachmentView.attachment.originalFilename)
                }
            }
        }

        div {
            taskView.attachments.forEach { attachmentView: AttachmentView ->
                if (!attachmentView.attachment.isImage()) {
                    a(href=attachmentView.url) {
                        +attachmentView.attachment.originalFilename
                    }
                }
            }
        }

        div {
            a(href = "/comments/comment?parentId=${taskView.task.id}") {
                +"Comment"}
        }
    }
}
