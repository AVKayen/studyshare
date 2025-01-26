package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.task.TaskView
import com.physman.utils.objectIdToSimpleDateString
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskTemplate(taskView: TaskView, isAuthor: Boolean) {
    val images = taskView.attachments.filter { attachmentView: AttachmentView -> attachmentView.attachment.isImage }
    val nonImageAttachments =
        taskView.attachments.filter { attachmentView: AttachmentView -> !attachmentView.attachment.isImage }
    article(classes = "task") {
        id = "article-${taskView.task.id.toHexString()}"
        header {
            h2 {
                +taskView.task.title
            }
            span {
                if (isAuthor) {
                    postDeletionButton(taskView.task)
                }
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
