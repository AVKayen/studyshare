package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.solution.SolutionView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solutionView: SolutionView) {

    val upvoteCountSpanId = "upvote-count-${solutionView.solution.id}"
    val upvoteButtonId = "upvote-btn-${solutionView.solution.id}"

    val images = solutionView.attachments.filter { attachmentView: AttachmentView -> attachmentView.attachment.isImage() }
    val nonImages =
        solutionView.attachments.filter { attachmentView: AttachmentView -> !attachmentView.attachment.isImage() }

    article(classes = "solution") {
        header {
            div {
                classes = setOf("upvotes")
                span {
                    button {
                        classes = setOf("voting-button")
                        attributes["id"] = upvoteButtonId
                        attributes["hx-get"] = "/solutions/${solutionView.solution.id}/upvote"
                        attributes["hx-target"] = "#$upvoteCountSpanId"

                        if (solutionView.isUpvoted) {
                            attributes["disabled"] = "true"
                        }

                        +"up"
                    }
                }
                span {
                    attributes["id"] = upvoteCountSpanId
                    +solutionView.solution.upvoteCount().toString()
                }
                span {
                    // down
                }
            }
            h2 {
                +solutionView.solution.title
            }
        }

        section {
            classes = setOf("gallery")
            images.forEach { attachmentView: AttachmentView ->
                img(src = attachmentView.link, alt = attachmentView.attachment.originalFilename)
            }
        }

        section {
            classes = setOf("attachments")
            nonImages.forEach { attachmentView: AttachmentView ->
                a(href = attachmentView.link) {
                    +attachmentView.attachment.originalFilename
                }
            }
        }

        div {
            classes = setOf("button-container")
            showCommentsButton(solutionView.solution.id)
        }
        div {
            id = "comments-${solutionView.solution.id}"
            classes = setOf("comments")
        }
    }
}
