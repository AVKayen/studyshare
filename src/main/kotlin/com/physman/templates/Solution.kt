package com.physman.templates

import com.physman.forms.Button
import com.physman.solution.Solution
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solution: Solution, taskId: String) {

    val url = "./${taskId}/solutions/${solution.id}/upvote"

    val voteButton = Button(
        "Thanks!",
        mapOf(
                    "hx-patch" to url,
                    "hx-swap" to "outerHTML",
                    "hx-target" to "closest .flex-col-solution"
    ))

    article(classes = "flex-col-solution") {
        header {
            h2 {
                +solution.title

                voteButton.render(this)

                +" : ${solution.upvotes}"

            }
        }
        div {

            if (solution.additionalNotes != null) {
                println(solution.additionalNotes)
                +"Notes: ${solution.additionalNotes}"
                +" Id: ${solution.id}"
            }
        }
        if (solution.images.isNotEmpty()) {
            div {
                for (imageId in solution.images) {
                    a(href = "/images/$imageId")
                }
            }
        }
    }
}
