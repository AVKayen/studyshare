package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.solution.SolutionView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solutionView: SolutionView) {

    val upvoteCountSpanId = "upvote-count-${solutionView.solution.id}"
    val upvoteButtonId = "upvote-btn-${solutionView.solution.id}"

    article(classes = "flex-col-solution") {
        header {
            h2 {
                span {
                    attributes["id"] = upvoteCountSpanId
                    +solutionView.solution.upvoteCount().toString()
                }
                +" "
                +solutionView.solution.title
            }
        }

        div {
            button {
                attributes["id"] = upvoteButtonId
                attributes["hx-get"] = "/solutions/${solutionView.solution.id}/upvote"
                attributes["hx-target"] = "#$upvoteCountSpanId"

                if (solutionView.isUpvoted) {
                    attributes["disabled"] = "true"
                }

                +"upvote button"
            }
        }

        div {
            if (solutionView.solution.additionalNotes != null) {
                +"Notes: ${solutionView.solution.additionalNotes}"
            }
        }

        div {
            solutionView.attachments.forEach { attachmentView: AttachmentView ->
                if (attachmentView.attachment.isImage()) {
                    img(src = attachmentView.link, alt = attachmentView.attachment.originalFilename)
                }
            }
        }

        div {
            solutionView.attachments.forEach { attachmentView: AttachmentView ->
                if (!attachmentView.attachment.isImage()) {
                    a(href=attachmentView.link) {
                        +attachmentView.attachment.originalFilename
                    }
                }
            }
        }

        div {
            a(href = "/comments/comment?parentId=${solutionView.solution.id}") {
                +"Comment"}
        }
    }
}
