package com.studyshare.templates

import com.studyshare.attachment.AttachmentView
import com.studyshare.solution.Solution
import com.studyshare.solution.VoteUpdate
import com.studyshare.task.Task
import com.studyshare.utils.Post
import kotlinx.html.*
import org.bson.types.ObjectId

enum class AccessLevel {
    NONE,
    DELETE,
    EDIT
}

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

fun FlowContent.contentLoadTemplate(url: String) {
    div {
        attributes["hx-get"] = url
        attributes["hx-trigger"] = "load"
        attributes["hx-swap"] = "outerHTML"

        article {
            span(classes = "htmx-indicator") {
                attributes["aria-busy"] = "true"
            }
        }
    }
}

fun FlowContent.deletionButton(getUrl: String) {
    button(classes = "btn secondary outline") {
        attributes["hx-get"] = getUrl
        attributes["hx-target"] = "body"
        attributes["hx-swap"] = "beforeend"

        span(classes = "material-symbols-rounded") {
            +"delete"
        }
    }
}

fun FlowContent.postDeletionButton(post: Post) {
    val url = when (post) {
        is Solution -> "/solutions/deletion-modal?solutionId=${post.id}"
        is Task -> "/${post.groupId}/deletion-modal?taskId=${post.id}"
        else -> throw IllegalArgumentException("Invalid post")
    }

    deletionButton(url)
}

fun FlowContent.postEditingButton(post: Post) {
    val url = when (post) {
        is Task -> "/tasks/editing-modal?taskId=${post.id}"
        is Solution -> "/solutions/editing-modal?id=${post.id}"
        else -> throw IllegalArgumentException("Invalid post")
    }

    button(classes = "btn secondary outline") {
        attributes["hx-get"] = url
        attributes["hx-target"] = "body"
        attributes["hx-swap"] = "beforeend"

        span(classes = "material-symbols-rounded") {
            +"edit"
        }
    }
}

fun FlowContent.postActions(post: Post, accessLevel: AccessLevel)
{
    when (accessLevel) {
        AccessLevel.NONE -> return
        AccessLevel.DELETE -> postDeletionButton(post)
        AccessLevel.EDIT -> {postEditingButton(post); postDeletionButton(post)}
    }
}


fun FlowContent.localDateSpan(objectId: ObjectId) {
    val script = """
        on load 1
            put convertUTCDateToLocalDate(me.dataset.date) into me
        end
    """.trimIndent()
    span {
        attributes["_"] = script
        attributes["data-date"] = objectId.timestamp.toString()
    }
}

fun FlowContent.wideButton(buttonText: String, url: String, additionalButtonAttributes: Map<String, String>? = null) {
    button(classes = "btn primary outline wide-button") {
        attributes["hx-get"] = url
        if (additionalButtonAttributes != null) {
            attributes.putAll(additionalButtonAttributes)
        }

        +buttonText
    }
}