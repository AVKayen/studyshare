package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.forms.*
import com.physman.solution.*
import com.physman.templates.formModalDialog
import com.physman.templates.solutionTemplate
import com.physman.templates.votingTemplate
import com.physman.utils.validateObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.bson.types.ObjectId


fun Route.solutionRouter(solutionRepository: SolutionRepository) {
    val solutionCreationForm = Form("Create a new solution", "solutionForm", formAttributes = mapOf(
        "hx-target" to "#solution-list",
        "hx-swap" to "beforeend"
    ))
    solutionCreationForm.addInput(TextlikeInput("Title", "title", InputType.text, titleValidator))
    solutionCreationForm.addInput(TextlikeInput("Additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
    solutionCreationForm.addInput(FileInput("Upload files", "files", inputAttributes = mapOf("multiple" to "true")))


    val solutionDeletionForm = Form("Are you sure you want to delete this solution?", "solutionForm", formAttributes = mapOf(
        "hx-target" to "#solution-list",
        "hx-swap" to "beforeend"
    ))

    globalFormRouter.routeFormValidators(solutionCreationForm)
    globalFormRouter.routeFormValidators(solutionDeletionForm)

    get("/creation-modal") {
        val taskId = call.request.queryParameters["taskId"]
        if (taskId == null) {
            call.respondText("Task Id not specified.", status = HttpStatusCode.BadRequest)
            return@get
        }
        call.respondHtml {
            body {
                formModalDialog(
                    form = solutionCreationForm,
                    callbackUrl = "/solutions?taskId=$taskId"
                )
            }
        }
    }

    get("/deletion-modal") {
        val taskId = call.request.queryParameters["taskId"]
        val solutionId = call.request.queryParameters["solutionId"]
        if (taskId == null) {
            call.respondText("Task Id not specified.", status = HttpStatusCode.BadRequest)
            return@get
        }
        if (solutionId == null) {
            call.respondText("Solution Id not specified.", status = HttpStatusCode.BadRequest)
            return@get
        }
        call.respondHtml {
            body {
                formModalDialog(
                    form = solutionDeletionForm,
                    callbackUrl = "/solutions?taskId=$taskId"
                )
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
                div {
                    attributes["id"] = "solution-list"
                  
                    for (solutionView in solutionViews) {
                        solutionTemplate(solutionView)
                    }
                }
            }
        }
    }

    post {
        val objectIds = validateObjectIds(call, "taskId") ?: return@post
        val taskId = objectIds["taskId"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)
        val userName = userSession.name

        val formSubmissionData: FormSubmissionData = solutionCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!

        val solution = Solution(title = title, additionalNotes = additionalNotes, taskId = taskId, authorId = userId, authorName = userName)

        val solutionView = solutionRepository.createSolution(solution, formSubmissionData.files, userId)
        formSubmissionData.cleanup()

        call.respondHtml(HttpStatusCode.OK) {
            body {
                solutionTemplate(solutionView)
            }
        }
    }

    route("/{id}") {

        delete {
            val objectIds = validateObjectIds(call, "id") ?: return@delete
            val solutionId = objectIds["id"]!!

            solutionRepository.deleteSolution(solutionId)
            call.response.status(HttpStatusCode.NoContent)
        }

        post ("/{voteAction}") {
            val action = call.parameters["voteAction"]
            val objectIds = validateObjectIds(call, "id") ?: return@post
            val solutionId = objectIds["id"]!!

            val userSession = call.sessions.get<UserSession>()!!
            val userId = ObjectId(userSession.id)

            if (action == null) {
                call.respondText("Action not specified.", status = HttpStatusCode.BadRequest)
                return@post
            }

            val voteType: VoteType = when (action) {
                "upvote" -> VoteType.UPVOTE
                "downvote" -> VoteType.DOWNVOTE
                else -> {
                    call.respondText("Invalid action.", status = HttpStatusCode.BadRequest)
                    return@post
                }
            }

            val voteUpdate = solutionRepository.vote(solutionId, userId, voteType) ?: return@post

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    votingTemplate(voteUpdate = voteUpdate, callbackId = solutionId)
                }
            }
        }
    }
}