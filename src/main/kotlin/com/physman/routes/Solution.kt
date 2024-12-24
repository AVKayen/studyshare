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
import kotlinx.html.article
import kotlinx.html.body
import kotlinx.html.div
import kotlin.random.Random

fun Route.solutionRouter() {

    fun solutionCreationFormCreator(taskId: Int): Form {
        val solutionCreationForm = Form("Create a new solution", "solutionForm",  "tasks/${taskId}/solutions", mapOf(
            "hx-target" to "#solution-list",
            "hx-swap" to "beforeend"
        ))
        solutionCreationForm.addInput(TextlikeInput("title", "title", InputType.text, titleValidator))
        solutionCreationForm.addInput(TextlikeInput("additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
        return solutionCreationForm
    }

    // I have no clue why this works (but it works)
    globalFormRouter.routeForm(solutionCreationFormCreator(0))

    get {
        val taskId = call.parameters["id"]?.toIntOrNull()
        if(taskId == null) {
            call.response.status(HttpStatusCode.BadRequest)
            return@get
        }

        val solutions = InMemoryTaskRepository.getAllSolutions(taskId)
        if(solutions == null) {
            call.response.status(HttpStatusCode.NotFound)
            return@get
        }
        call.respondHtml {
            index("Solutions") {
                div {
                    attributes["id"] = "solution-list"
                    for (solution in solutions) {
                        solutionTemplate(solution)
                    }
                }
                article {
                    solutionCreationFormCreator(taskId).render(this)
                }
            }
        }
    }
    post {
        val taskId = call.parameters["id"]?.toIntOrNull()
        if (taskId == null) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        }
        val formParameters = call.receiveParameters()
        val title = formParameters["title"].toString()
        val additionalNotes = formParameters["additional_notes"].toString()

        val error: String? = titleValidator(title) ?: additionalNotesValidator(additionalNotes)
        if (error != null) {
            call.respondText(error, status = HttpStatusCode.BadRequest)
        }

        val newSolution = Solution(id = Random.nextInt(99999), title = title, additionalNotes = additionalNotes)

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
            val taskId = call.parameters["id"]?.toIntOrNull()
            if(taskId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@get
            }
            val solutionId = call.parameters["solutionId"]?.toIntOrNull()
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
            val taskId = call.parameters["id"]?.toIntOrNull()
            if(taskId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@delete
            }
            val solutionId = call.parameters["solutionId"]?.toIntOrNull()
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