package com.physman.templates

import com.physman.attachment.AttachmentView
import com.physman.solution.SolutionView
import com.physman.solution.VoteUpdate
import com.physman.utils.objectIdToSimpleDateString
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solutionView: SolutionView) {
    val images = solutionView.attachments.filter { attachmentView: AttachmentView -> attachmentView.attachment.isImage }
    val nonImageAttachments =
        solutionView.attachments.filter { attachmentView: AttachmentView -> !attachmentView.attachment.isImage }

    article(classes = "solution") {
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
                cite {
                    +"${solutionView.solution.authorName} @ ${objectIdToSimpleDateString(solutionView.solution.id)}"
                }
            }

            if (!solutionView.solution.additionalNotes.isNullOrBlank()) {
                p {
                    +"${solutionView.solution.additionalNotes}"
                }
            }

            imageAttachmentTemplate(images)
            nonImageAttachmentTemplate(nonImageAttachments)
            // TODO: Hiding comments, button to comment, comment count
            showCommentsAccordion(solutionView.solution)

        }
    }
}
