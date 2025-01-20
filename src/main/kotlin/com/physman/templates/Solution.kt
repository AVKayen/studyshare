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
            //TODO: the buttons work correctly only if reloaded after each use
            button {
                attributes["id"] = upvoteButtonId
                attributes["hx-target"] = "#$voteCountSpanId"

                if (!solutionView.isUpvoted) {
                    attributes["hx-get"] = "/solutions/${solutionView.solution.id}/upvote"
                    +"upvote button"

                } else {
                    attributes["hx-get"] = "/solutions/${solutionView.solution.id}/remove-upvote"
                    +"remove upvote button"

                }


            }
            button {
                attributes["id"] = downvoteButtonId
                attributes["hx-target"] = "#$voteCountSpanId"

                if (!solutionView.isDownvoted) {
                    attributes["hx-get"] = "/solutions/${solutionView.solution.id}/downvote"
                    +"downvote button"

                } else {
                    attributes["hx-get"] = "/solutions/${solutionView.solution.id}/remove-downvote"
                    +"remove downvote button"
                }


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
