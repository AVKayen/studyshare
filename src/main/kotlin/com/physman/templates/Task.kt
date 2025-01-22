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
            a(href = "/solutions/creation-form?taskId=${taskView.task.id}") {
                +"Create solution"}

        }
        if (taskView.task.additionalNotes != null) {
            p {
                    +"${taskView.task.additionalNotes}"
            }
        }


        imageAttachmentTemplate(images)
        nonImageAttachmentTemplate(nonImageAttachments)
        // TODO: Hiding comments, button to comment
        showCommentsAccordion(taskView.task)
    }
}
