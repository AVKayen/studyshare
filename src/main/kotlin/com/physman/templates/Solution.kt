package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.solution.SolutionView
import com.physman.solution.VoteUpdate
import com.physman.utils.objectIdToSimpleDateString
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solutionView: SolutionView, isAuthor: Boolean) {
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
                h2 {
                    +solutionView.solution.title
                }
                span {
                    if (isAuthor) {
                        postDeletionButton(solutionView.solution)
                    }
                }
                cite {
                    +"${solutionView.solution.authorName} @ ${objectIdToSimpleDateString(solutionView.solution.id)}"
                }
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
