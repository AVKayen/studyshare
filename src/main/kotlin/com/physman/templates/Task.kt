package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.task.TaskView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskTemplate(taskView: TaskView) {
    val images = taskView.attachments.filter { attachmentView: AttachmentView -> attachmentView.attachment.isImage() }
    val nonImageAttachments =
        taskView.attachments.filter { attachmentView: AttachmentView -> !attachmentView.attachment.isImage() }
    article(classes = "task") {
        header {
            h2 {
                +taskView.task.title
            }

        }
        if (taskView.task.additionalNotes != null) {
            p {
                    +"${taskView.task.additionalNotes}"
            }
        }
        div {
            a(href = "/comments/comment?parentId=${taskView.task.id}&post-type=task") {
                +"Comment"}
        }


        imageAttachmentTemplate(images)
        nonImageAttachmentTemplate(nonImageAttachments)
        // TODO: Hiding comments, button to comment
        showCommentsButton(taskView.task)
        div {
            id = "comments-${taskView.task.id}"
            classes = setOf("comments")
        }
    }
}
