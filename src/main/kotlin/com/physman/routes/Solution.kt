package com.physman.routes

import com.physman.forms.Form
import com.physman.forms.FormSubmissionData
import com.physman.forms.TextlikeInput
import com.physman.forms.globalFormRouter
import com.physman.solution.Solution
import com.physman.solution.SolutionRepository
import com.physman.templates.index
import com.physman.templates.solutionTemplate
import com.physman.utils.additionalNotesValidator
import com.physman.utils.titleValidator
import com.physman.utils.validateObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.InputType
import kotlinx.html.body
import org.bson.types.ObjectId


fun Route.solutionRouter(solutionRepository: SolutionRepository) {
    val solutionCreationForm = Form("Create a new solution", "solutionForm", mapOf(
            "hx-swap" to "none" // because currently this form is on an empty page
        ))
    solutionCreationForm.addInput(TextlikeInput("title", "title", InputType.text, titleValidator))
        solutionCreationForm.addInput(TextlikeInput("additional notes", "additionalNotes", InputType.text, additionalNotesValidator))

    globalFormRouter.routeFormValidators(solutionCreationForm)

    get("/creation-form") {
        val taskId = call.request.queryParameters["taskId"]
        if (taskId == null) {
            call.respondText("Task Id not specified.", status = HttpStatusCode.BadRequest)
            return@get
        }
        call.respondHtml {
//            body {
//                taskCreationForm.render(this, "/tasks/$taskId/solutions")
//            }

            // index because of lack of htmx needed for testing (htmx is served with index page only)
            index("This won't be index") {
                solutionCreationForm.render(this, "/solutions?taskId=$taskId")
            }
        }
    }

    get {
        val objectIds = validateObjectIds(call, "taskId") ?: return@get
        val taskId = objectIds["taskId"]!!

        val solutions = solutionRepository.getSolutions(taskId = taskId)

        call.respondHtml {
            body {
                for (solution in solutions) {
                    solutionTemplate(solution, taskId.toString())
                }
            }
        }

    }

    post {
        val objectIds = validateObjectIds(call, "taskId") ?: return@post
        val taskId = objectIds["taskId"]!!

        val formSubmissionData: FormSubmissionData = solutionCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!

        val newSolution = Solution(title = title, additionalNotes = additionalNotes, taskId = taskId)

        val success = solutionRepository.createSolution(newSolution)
        if (!success) {
            call.respondText(text = "Solution has not been created.", status = HttpStatusCode.BadRequest)
            return@post
        }

        call.respondHtml(HttpStatusCode.OK) {
            body {
//                solutionTemplate(newSolution, taskId.toString())
            }
        }
    }

    route("/{id}") {

        get ("/upvote") {
            val objectIds = validateObjectIds(call, "id") ?: return@get
            val solutionId = objectIds["id"]!!

            val success = solutionRepository.upvoteSolution(solutionId, ObjectId()) // TODO use a real userId

            if (!success) {
                call.respondText(text = "Solution has not been upvoted.", status = HttpStatusCode.NotFound)
                return@get
            }

            call.respondHtml(HttpStatusCode.OK) {
                body {
//                    solutionTemplate(upvotedSolution, taskId)
                }
            }
        }

        delete {
            val objectIds = validateObjectIds(call, "id") ?: return@delete
            val solutionId = objectIds["id"]!!

            val success = solutionRepository.deleteSolution(solutionId)
            if(!success) {
                call.respondText(text = "Solution has not been deleted.", status = HttpStatusCode.NotFound)
                return@delete
            }
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}