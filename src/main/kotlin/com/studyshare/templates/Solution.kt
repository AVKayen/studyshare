package com.studyshare.templates

import com.studyshare.attachment.AttachmentView
import com.studyshare.solution.SolutionView
import com.studyshare.solution.VoteUpdate
import kotlinx.html.FlowContent
import kotlinx.html.*
import org.bson.types.ObjectId

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

fun FlowContent.solutionTemplate(solutionView: SolutionView, accessLevel: AccessLevel) {
    val images = solutionView.attachments.filter { attachmentView: AttachmentView -> attachmentView.attachment.isImage }
    val nonImageAttachments =
        solutionView.attachments.filter { attachmentView: AttachmentView -> !attachmentView.attachment.isImage }

    article(classes = "solution") {
        id = "article-${solutionView.solution.id.toHexString()}"
        votingTemplate(
            VoteUpdate(solutionView.isDownvoted, solutionView.isUpvoted, solutionView.solution.voteCount()),
            solutionView.solution.id
        )


        div {
            classes = setOf("solution-content")
            header {
                div {
                    h2 {
                        +solutionView.solution.title
                    }
                    cite {
                        +"${solutionView.solution.authorName} @ "
                        localDateSpan(solutionView.solution.id)
                    }
                }
                postActions(post = solutionView.solution, accessLevel)
            }

            if (!solutionView.solution.additionalNotes.isNullOrBlank()) {
                p {
                    +"${solutionView.solution.additionalNotes}"
                }
            }

            galleryTemplate("gallery-${solutionView.solution.id}", images)
            nonImageAttachmentTemplate(nonImageAttachments)
            showCommentsAccordion(solutionView.solution)

        }
    }
}
