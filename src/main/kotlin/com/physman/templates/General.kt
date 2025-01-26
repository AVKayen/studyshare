package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.solution.Solution
import com.physman.solution.VoteUpdate
import com.physman.task.Task
import com.physman.utils.Post
import kotlinx.html.*
import org.bson.types.ObjectId

fun FlowContent.nonImageAttachmentTemplate(nonImageAttachments: List<AttachmentView>) {
    if (nonImageAttachments.isNotEmpty()) {
        section {
            classes = setOf("attachments")
            nonImageAttachments.forEach { attachmentView: AttachmentView ->
                a(href = attachmentView.url) {
                    +attachmentView.attachment.originalFilename
                }
            }
        }
    }
}

fun FlowContent.galleryTemplate(galleryId: String, images: List<AttachmentView>) {
    if (images.isNotEmpty()) {
        section {
            classes = setOf("gallery")
            images.forEach { attachmentView: AttachmentView ->
                a(href = attachmentView.url) {
                    attributes["data-fancybox"] = galleryId
                    img(src = attachmentView.thumbnailUrl, alt = attachmentView.attachment.originalFilename)
                }
            }
        }
    }
}

fun FlowContent.fancyboxSetupScript() {
    script {
        unsafe {
            +"""
                Fancybox.bind("[data-fancybox]", {
                    Toolbar: {
                        display: {
                            left: ["infobar"],
                            middle: [
                                "zoomIn",
                                "zoomOut",
                                "rotateCCW",
                            ],
                            right: ["close"],
                        },
                    },
                });
            """.trimIndent()
        }
    }
}

fun FlowContent.votingButton(
    isActive: Boolean,
    voteType: String,
    callbackId: ObjectId,
    voteUrl: String,
    icon: String
) {
    button {
        classes = setOf("btn", "outline", "solution-voting")
        classes +=
            if (isActive) "primary"
            else "secondary"

        attributes["hx-post"] = voteUrl
        attributes["hx-target"] = "#voting-${callbackId.toHexString()}"
        attributes["hx-swap"] = "outerHTML"
        span {
            classes = setOf("material-symbols-rounded", voteType)
            +icon
        }
    }
}

fun FlowContent.votingTemplate(voteUpdate: VoteUpdate, callbackId: ObjectId) {
    div {
        classes = setOf("solution-voting")
        id = "voting-${callbackId.toHexString()}"

        votingButton(
            isActive = voteUpdate.isUpvoted,
            voteType = "upvote",
            callbackId = callbackId,
            voteUrl = "/solutions/${callbackId.toHexString()}/upvote",
            icon = "arrow_upward"
        )

        span {
            classes = setOf("vote-count")
            +voteUpdate.voteCount.toString()
        }

        votingButton(
            isActive = voteUpdate.isDownvoted,
            voteType = "downvote",
            callbackId = callbackId,
            voteUrl = "/solutions/${callbackId.toHexString()}/downvote",
            icon = "arrow_downward"
        )
    }
}

fun FlowContent.postDeletionButton(post: Post) {
    val url = when (post) {
        is Solution -> "/solutions/deletion-modal?solutionId=${post.id}"
        is Task -> "/tasks/deletion-modal?taskId=${post.id}"
        else -> throw IllegalArgumentException("Invalid post")
    }

    button {
        attributes["hx-get"] = url
        attributes["hx-target"] = "body"
        attributes["hx-swap"] = "beforeend"

        span(classes = "material-symbols-rounded") {
            +"delete"
        }
    }
}