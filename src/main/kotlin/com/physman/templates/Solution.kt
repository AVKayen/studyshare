package com.physman.templates

import com.physman.solution.Solution
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solution: Solution) {
    article(classes = "flex-col solution") {
        header {
            h2 {
                +solution.title
                a(href = "???") {
                    +" Upvote"}
                +"Upvotes: ${solution.votes}"
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
