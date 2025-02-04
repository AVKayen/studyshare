package com.studyshare.templates

import com.studyshare.attachment.AttachmentView
import com.studyshare.task.TaskView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskTemplate(taskView: TaskView, accessLevel: AccessLevel) {
    val images = taskView.attachments.filter { attachmentView: AttachmentView -> attachmentView.attachment.isImage }
    val nonImageAttachments =
        taskView.attachments.filter { attachmentView: AttachmentView -> !attachmentView.attachment.isImage }
    article(classes = "task") {
        id = "article-${taskView.task.id.toHexString()}"
        header {
            div {
                h2 {
                    +taskView.task.title
                }
                cite {
                    +"${taskView.task.authorName} @ "
                    localDateSpan(taskView.task.id)
                }
            }
            postActions(post = taskView.task, accessLevel)
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
