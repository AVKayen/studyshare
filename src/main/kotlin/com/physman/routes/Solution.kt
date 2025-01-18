package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.forms.*
import com.physman.solution.Solution
import com.physman.solution.SolutionRepository
import com.physman.solution.additionalNotesValidator
import com.physman.solution.titleValidator
import com.physman.templates.index
import com.physman.templates.solutionTemplate
import com.physman.utils.validateObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.bson.types.ObjectId


fun Route.solutionRouter(solutionRepository: SolutionRepository) {
    val solutionCreationForm = Form("Create a new solution", "solutionForm", mapOf(
            "hx-swap" to "none" // because currently this form is on an empty page
        ))
    solutionCreationForm.addInput(TextlikeInput("title", "title", InputType.text, titleValidator))
    solutionCreationForm.addInput(TextlikeInput("additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
    solutionCreationForm.addInput(FileInput("select files", "files", listOf(), inputAttributes = mapOf("multiple" to "true")))

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

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        val solutionViews = solutionRepository.getSolutions(taskId = taskId, userId = userId)

        call.respondHtml {
            body {
                for (solutionView in solutionViews) {
                    solutionTemplate(solutionView)
                    div {
                        attributes["hx-get"] = "/comments?parentId=${solutionView.solution.id}"
                        attributes["hx-trigger"] = "load"

                        span(classes = "htmx-indicator") {
                            +"Loading..."
                        }
                    }
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

        solutionRepository.createSolution(newSolution, formSubmissionData.files)
        formSubmissionData.cleanup()


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

            val userSession = call.sessions.get<UserSession>()!!
            val userId = ObjectId(userSession.id)

            val newUpvoteCount = solutionRepository.upvoteSolution(solutionId, userId)

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    +newUpvoteCount.toString()

                    button {
                        attributes["id"] = "upvote-btn-$solutionId"
                        attributes["hx-swap-oob"] = "true"
                        attributes["disabled"] = "true"

                        +"upvote button"
                    }
                }
            }
        }

        delete {
            val objectIds = validateObjectIds(call, "id") ?: return@delete
            val solutionId = objectIds["id"]!!

            solutionRepository.deleteSolution(solutionId)
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}