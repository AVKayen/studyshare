package com.physman.routes

import com.physman.forms.Form
import com.physman.forms.FormSubmissionData
import com.physman.forms.TextlikeInput
import com.physman.forms.globalFormRouter
import com.physman.solution.Solution
import com.physman.task.TaskRepository
import com.physman.templates.index
import com.physman.templates.solutionTemplate
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.InputType
import kotlinx.html.body



fun Route.solutionRouter(taskRepository: TaskRepository) {



    val solutionCreationForm = Form("Create a new solution", "solutionForm", mapOf(
            "hx-swap" to "none" // because currently this form is on an empty page
        ))
        solutionCreationForm.addInput(TextlikeInput("title", "title", InputType.text, titleValidator))
        solutionCreationForm.addInput(TextlikeInput("additional notes", "additionalNotes", InputType.text, additionalNotesValidator))

    globalFormRouter.routeFormValidators(solutionCreationForm)

    get("/creation-form") {
        val taskId = call.parameters["id"]
        if (taskId == null) {
            call.response.status(HttpStatusCode.BadRequest)
            return@get
        }
        call.respondHtml {
//            body {
//                taskCreationForm.render(this, "/tasks/$taskId/solutions")
//            }

            // index because of lack of htmx needed for testing (htmx is served with index page only)
            index("This won't be index") {
                solutionCreationForm.render(this, "/tasks/$taskId/solutions")
            }
        }
    }

    post {
        val taskId = call.parameters["id"]
        if (taskId == null) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        }

        val formSubmissionData: FormSubmissionData = solutionCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!

        val newSolution = Solution(title = title, additionalNotes = additionalNotes)
        println("New solution: $newSolution")

        val solution = taskRepository.createSolution(taskId, newSolution)
        if (solution == null) {
            call.respondText(text = "Solution has not been created.", status = HttpStatusCode.NotFound)
            return@post
        }

        call.respondHtml(HttpStatusCode.OK) {
            body {
                solutionTemplate(solution, taskId)
            }
        }
    }
    route("/{solutionId}") {
          // we've decided not to show solutions on a separate pages, right? Not deleting, we may always change our minds
//        get {
//            val taskId = call.parameters["id"]
//            if(taskId == null) {
//                call.response.status(HttpStatusCode.BadRequest)
//                return@get
//            }
//            val solutionId = call.parameters["solutionId"]
//            if(solutionId == null) {
//                call.response.status(HttpStatusCode.BadRequest)
//                return@get
//            }
//
//            val solution = InMemoryTaskRepository.getSolution(taskId, solutionId)
//            if(solution == null) {
//                call.response.status(HttpStatusCode.NotFound)
//                return@get
//            }
//
//
//            call.respondHtml(HttpStatusCode.OK) {
//                index("Solution") {
//                    solutionTemplate(solution)
//                }
//            }
//        }
        get("/creation-form") {
            val taskId = call.parameters["id"]
            if (taskId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@get
            }
            call.respondHtml {
//            body {
//                taskCreationForm.render(this, "/tasks/$taskId/solutions")
//            }

                // index because of lack of htmx needed for testing (htmx is served with index page only)
                index("This won't be index") {
                    solutionCreationForm.render(this, "/tasks/$taskId/solutions")
                }
            }
        }


        patch ("/upvote") {
            val taskId = call.parameters["id"]
            val solutionId = call.parameters["solutionId"]

            if(taskId == null || solutionId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@patch
            }

            val upvotedSolution = taskRepository.upvoteSolution(taskId, solutionId)

            if (upvotedSolution == null) {
                call.respondText(text = "Solution has not been created.", status = HttpStatusCode.NotFound)
                return@patch
            }

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    solutionTemplate(upvotedSolution, taskId)
                }
            }
        }

        delete {
            val taskId = call.parameters["id"]
            val solutionId = call.parameters["solutionId"]

            if(taskId == null || solutionId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@delete
            }

            val deletedSolution = taskRepository.deleteSolution(taskId, solutionId)
            if(deletedSolution == null) {
                call.respondText(text = "Solution has not been deleted.", status = HttpStatusCode.NotFound)
                return@delete
            }
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}