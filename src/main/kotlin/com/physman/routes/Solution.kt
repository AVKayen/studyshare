package com.physman.routes

import com.physman.forms.Form
import com.physman.forms.TextlikeInput
import com.physman.forms.globalFormRouter
import com.physman.solution.Solution
import com.physman.task.InMemoryTaskRepository
import com.physman.templates.index
import com.physman.templates.solutionTemplate
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.InputType
import kotlinx.html.body

fun Route.solutionRouter() {

    val solutionCreationForm = Form("Create a new solution", "solutionForm", mapOf(
            "hx-target" to "#solution-list",
            "hx-swap" to "beforeend"
        ))
        solutionCreationForm.addInput(TextlikeInput("title", "title", InputType.text, titleValidator))
        solutionCreationForm.addInput(TextlikeInput("additional notes", "additionalNotes", InputType.text, additionalNotesValidator))

    globalFormRouter.routeFormValidators(solutionCreationForm)

    get("/creation-form") {
        call.respondHtml {
            body {
                solutionCreationForm.render(this, "/tasks/{id}/solutions")
            }
        }
    }

    post {
        val taskId = call.parameters["id"]
        if (taskId == null) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        }
        val formParameters = call.receiveParameters()
        val title = formParameters["title"].toString()
        val additionalNotes = formParameters["additional_notes"].toString()

        val error: String? = titleValidator(title) ?: additionalNotesValidator(additionalNotes)
        println(title)
        if (error != null) {
            call.respondText(error, status = HttpStatusCode.BadRequest)
        }

        val newSolution = Solution(title = title, additionalNotes = additionalNotes)

        val solution = InMemoryTaskRepository.createSolution(taskId, newSolution)
        if (solution == null) {
            call.response.status(HttpStatusCode.NotFound)
            return@post
        }

        call.respondHtml(HttpStatusCode.OK) {
            body {
                solutionTemplate(solution)
            }
        }
    }
    route("/{solutionId}") {
        get {
            val taskId = call.parameters["id"]
            if(taskId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@get
            }
            val solutionId = call.parameters["solutionId"]
            if(solutionId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@get
            }

            val solution = InMemoryTaskRepository.getSolution(taskId, solutionId)
            if(solution == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@get
            }


            call.respondHtml(HttpStatusCode.OK) {
                index("Solution") {
                    solutionTemplate(solution)
                }
            }
        }
        delete {
            val taskId = call.parameters["id"]
            if(taskId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@delete
            }
            val solutionId = call.parameters["solutionId"]
            if(solutionId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@delete
            }

            val deletedSolution = InMemoryTaskRepository.deleteSolution(taskId, solutionId)
            if(deletedSolution == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@delete
            }
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}