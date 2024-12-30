package com.physman.templates

import com.physman.forms.Button
import com.physman.solution.Solution
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solution: Solution, taskId: String) {


    val voteButton = Button(
        "Upvote2",
        mapOf(
            //"id" to solution.id  !add letters
    ))



    val url = "./${taskId}/solutions/${solution.id}/upvote"



    article(classes = "flex-col-solution") {

        header {
            h2 {

                +solution.title


                voteButton.boberButton(this, url)
                div(classes = "upvote") {
                    +" Upvotes: ${solution.votes}"
                }
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
