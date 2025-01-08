package com.physman.templates

import com.physman.image.ImageView
import com.physman.solution.SolutionView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solutionView: SolutionView, taskId: String) {

    article(classes = "flex-col-solution") {
        header {
            h2 {
                +"+${solutionView.solution.upvoteCount()} ${solutionView.solution.title}"
            }
        }

        div {
            button {
                attributes["hx-get"] = "/solutions/${solutionView.solution.id}/upvote"
                attributes["hx-swap"] = "none" // TODO change this to replace the current upvote count

                +"upvote button"
            }
        }

        div {
            if (solutionView.solution.additionalNotes != null) {
                +"Notes: ${solutionView.solution.additionalNotes}"
            }
        }

        div {
            solutionView.images.forEach { imageView: ImageView ->
                a(href=imageView.link) {
                    attributes["alt"] = imageView.image.originalFilename
                }
            }
        }
    }
}
