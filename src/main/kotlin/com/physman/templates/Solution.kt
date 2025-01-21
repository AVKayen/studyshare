package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.solution.SolutionView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solutionView: SolutionView) {
    val upvoteCountSpanId = "upvote-count-${solutionView.solution.id}"
    val upvoteButtonId = "upvote-btn-${solutionView.solution.id}"

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
                    attributes["hx-get"] = "/solutions/${solutionView.solution.id}/upvote"
                    attributes["hx-target"] = "#$upvoteCountSpanId"

                    if (solutionView.isUpvoted) {
                        attributes["disabled"] = "true"
                    }

                    span {
                        classes = setOf("material-symbols-rounded", "voting-icon")
                        +"add"
                    }
                }
            }
            span {
                attributes["id"] = upvoteCountSpanId
                +solutionView.solution.upvoteCount().toString()
            }
            span {
                button {
                    classes = setOf("voting-button")
                    /*attributes["id"] = upvoteButtonId
                    attributes["hx-get"] = "/solutions/${solutionView.solution.id}/upvote"
                    attributes["hx-target"] = "#$upvoteCountSpanId"

                    if (solutionView.isUpvoted) {
                        attributes["disabled"] = "true"
                    }*/

                    span {
                        classes = setOf("material-symbols-rounded", "voting-icon")
                        +"remove"
                    }
                }
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
            // TODO: Hiding coments, button to comment
            showCommentsButton(solutionView.solution.id)
            div {
                id = "comments-${solutionView.solution.id}"
                classes = setOf("comments")
            }
        }

    }
}
