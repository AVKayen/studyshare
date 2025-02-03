package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.solution.SolutionView
import com.physman.solution.VoteUpdate
import kotlinx.html.FlowContent
import kotlinx.html.*

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
