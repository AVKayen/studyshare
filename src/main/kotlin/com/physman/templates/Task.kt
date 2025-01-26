package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.task.TaskView
import com.physman.utils.objectIdToSimpleDateString
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskTemplate(taskView: TaskView) {
    val images = taskView.attachments.filter { attachmentView: AttachmentView -> attachmentView.attachment.isImage }
    val nonImageAttachments =
        taskView.attachments.filter { attachmentView: AttachmentView -> !attachmentView.attachment.isImage }
    article(classes = "task") {
        header {
            h2 {
                +taskView.task.title
                postDeletionButton(taskView.task)
            }
            cite {
                +"${taskView.task.authorName} @ ${objectIdToSimpleDateString(taskView.task.id)}"
            }
        }
        if (taskView.task.additionalNotes != null) {
            p {
                    +"${taskView.task.additionalNotes}"
            }
        }


        galleryTemplate("gallery-${taskView.task.id}", images)
        nonImageAttachmentTemplate(nonImageAttachments)
        showCommentsAccordion(taskView.task)
    }
}
