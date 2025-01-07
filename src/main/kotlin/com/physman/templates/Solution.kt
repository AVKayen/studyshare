package com.physman.templates

import com.physman.solution.Solution
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solution: Solution, taskId: String) {

    article(classes = "flex-col-solution") {
        header {
            h2 {
                +"+${solution.upvoteCount()} ${solution.title}"
            }
        }

        div {
            button {
                attributes["hx-get"] = "/solutions/${solution.id}/upvote"
                attributes["hx-swap"] = "none" // TODO change this to replace the current upvote count

                +"upvote button"
            }
        }

        div {
            if (solution.additionalNotes != null) {
                +"Notes: ${solution.additionalNotes}"
            }
        }
    }
}
