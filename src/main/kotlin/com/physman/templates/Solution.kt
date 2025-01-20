package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.solution.SolutionView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solutionView: SolutionView) {
    val voteCountSpanId = "vote-count-${solutionView.solution.id}"
    val upvoteButtonId = "upvote-btn-${solutionView.solution.id}"
    val downvoteButtonId = "downvote-btn-${solutionView.solution.id}"

    article(classes = "flex-col-solution") {
        header {
            h2 {
                span {
                    attributes["id"] = voteCountSpanId
                    +solutionView.solution.voteCount().toString()
                }
                +" "
                +solutionView.solution.title
            }
        }

        div {
            button {
                attributes["id"] = upvoteButtonId
                attributes["hx-get"] = "/solutions/${solutionView.solution.id}/upvote"
                attributes["hx-target"] = "#$voteCountSpanId"

                if (solutionView.isUpvoted) {
                    attributes["disabled"] = "true"
                }

                +"upvote button"
            }
            button {
                attributes["id"] = downvoteButtonId
                attributes["hx-get"] = "/solutions/${solutionView.solution.id}/downvote"
                attributes["hx-target"] = "#$voteCountSpanId"

                if (solutionView.isDownvoted) {
                    attributes["disabled"] = "true"
                }

                +"downvote button"
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
