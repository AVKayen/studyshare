package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.solution.SolutionView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solutionView: SolutionView) {
    val voteCountSpanId = "vote-count-${solutionView.solution.id}"
    val upvoteButtonId = "upvote-btn-${solutionView.solution.id}"
    val downvoteButtonId = "downvote-btn-${solutionView.solution.id}"

    val images = solutionView.attachments.filter { attachmentView: AttachmentView -> attachmentView.attachment.isImage() }
    val nonImageAttachments =
        solutionView.attachments.filter { attachmentView: AttachmentView -> !attachmentView.attachment.isImage() }

    article(classes = "solution") {
        div {
            classes = setOf("solution-voting")
            span {
                button {
                    classes = setOf("voting-button")
                    attributes["id"] = upvoteButtonId
                    attributes["hx-target"] = "#$voteCountSpanId"

                    if (!solutionView.isUpvoted) {
                        attributes["hx-get"] = "/solutions/${solutionView.solution.id}/upvote"

                    } else {
                        attributes["hx-get"] = "/solutions/${solutionView.solution.id}/remove-upvote"
                    }

                    span {
                        classes = setOf("material-symbols-rounded", "voting-icon")
                        +"add"
                    }
                }
            }
            span {
                attributes["id"] = voteCountSpanId
                +solutionView.solution.voteCount().toString()
            }

            span {
                button {
                    classes = setOf("voting-button")
                    attributes["id"] = downvoteButtonId
                    attributes["hx-target"] = "#$voteCountSpanId"

                    if (!solutionView.isDownvoted) {
                        attributes["hx-get"] = "/solutions/${solutionView.solution.id}/downvote"

                    } else {
                        attributes["hx-get"] = "/solutions/${solutionView.solution.id}/remove-downvote"

                    }

                    span {
                        classes = setOf("material-symbols-rounded", "voting-icon")
                        +"remove"
                    }
                }
            }
        }

        div {
            if (solutionView.solution.additionalNotes != null) {
                +"Notes: ${solutionView.solution.additionalNotes}"
            }
        }
        div {
            classes = setOf("solution-content")
            header {
                div {
                    classes = setOf("upvotes")

                }
                h2 {
                    +solutionView.solution.title
                }
            }

            imageAttachmentTemplate(images)
            nonImageAttachmentTemplate(nonImageAttachments)
            // TODO: Hiding comments, button to comment, comment count
            showCommentsButton(solutionView.solution.id, 2137)
            div {
                id = "comments-${solutionView.solution.id}"
                classes = setOf("comments")
            }
        }
    }
}
