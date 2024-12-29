package com.physman.templates

import com.physman.solution.Solution
import com.physman.forms.Form
import com.physman.forms.TextlikeInput
import com.physman.forms.globalFormRouter
import com.physman.routes.additionalNotesValidator
import com.physman.routes.titleValidator
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.solutionTemplate(solution: Solution, taskId: String) {


    val voteCreationForm = Form("Create a new solution", "solutionForm", mapOf(
        "hx-swap" to "none" // because currently this form is on an empty page
    ))
    voteCreationForm.addInput(TextlikeInput("title", "title", InputType.button, titleValidator))
    voteCreationForm.addInput(TextlikeInput("additional notes", "additionalNotes", InputType.text, additionalNotesValidator))

    globalFormRouter.routeFormValidators(voteCreationForm)

    val url = "./${taskId}/solutions/${solution.id}/upvote"



    article(classes = "flex-col solution") {
        header {
            h2 {
                +solution.title

                +" Upvotes: ${solution.votes}"
                voteCreationForm.boberButton(this, url)
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
