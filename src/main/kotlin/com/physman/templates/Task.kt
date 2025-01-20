package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.task.TaskView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskTemplate(taskView: TaskView) {
    val images = taskView.attachments.filter { attachmentView: AttachmentView -> attachmentView.attachment.isImage() }
    val nonImages =
        taskView.attachments.filter { attachmentView: AttachmentView -> !attachmentView.attachment.isImage() }
    article(classes = "task") {
        header {
            h2 {
                +taskView.task.title
            }

        }
        p {
            if (taskView.task.additionalNotes != null) {
                +"${taskView.task.additionalNotes}"
            }
        }
        section {
            classes = setOf("gallery")
            images.forEach { attachmentView: AttachmentView ->
                img(src = attachmentView.link, alt = attachmentView.attachment.originalFilename)
            }
        }

        section {
            classes = setOf("attachments")
            nonImages.forEach { attachmentView: AttachmentView ->
                a(href = attachmentView.link) {
                    +attachmentView.attachment.originalFilename
                }
            }
        }
        div {
            classes = setOf("button-container")
            formModalOpenButton(
                buttonText = "Create a solution",
                modalUrl = "/solutions/creation-modal?taskId=${taskView.task.id}"
            )
            showCommentsButton(taskView.task.id)
            // TODO: Button to comment :DDDDD
            // TODO: Hide comments button
        }
        div {
            id = "comments-${taskView.task.id}"
            classes = setOf("comments")
        }
    }
}
