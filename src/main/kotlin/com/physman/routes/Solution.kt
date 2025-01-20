package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.forms.*
import com.physman.solution.Solution
import com.physman.solution.SolutionRepository
import com.physman.solution.additionalNotesValidator
import com.physman.solution.titleValidator
import com.physman.templates.formModalDialog
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
    val solutionCreationForm = Form("Create a new solution", "solutionForm", formAttributes = mapOf(
        "hx-target" to "#solution-list",
        "hx-swap" to "beforeend"
    ))
    solutionCreationForm.addInput(TextlikeInput("title", "title", InputType.text, titleValidator))
    solutionCreationForm.addInput(TextlikeInput("additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
    solutionCreationForm.addInput(FileInput("select files", "files", listOf(), inputAttributes = mapOf("multiple" to "true")))

    globalFormRouter.routeFormValidators(solutionCreationForm)

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
                        div {
                            attributes["hx-get"] = "/comments?parentId=${solutionView.solution.id}"
                            attributes["hx-trigger"] = "load"

                            article(classes = "htmx-indicator") {
                                attributes["aria-busy"] = "true"
                            }
                        }
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

        val formSubmissionData: FormSubmissionData = solutionCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!

        val solution = Solution(title = title, additionalNotes = additionalNotes, taskId = taskId)

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


        //votes
        //TODO: add redirection to buttons
        get ("/upvote") {
            val objectIds = validateObjectIds(call, "id") ?: return@get
            val solutionId = objectIds["id"]!!

            val userSession = call.sessions.get<UserSession>()!!
            val userId = ObjectId(userSession.id)

            val newVoteCount = solutionRepository.upvote(solutionId, userId)

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    +newVoteCount.toString()

                    button {
                        attributes["id"] = "upvote-btn-$solutionId"
                        attributes["hx-swap-oob"] = "true"
                        attributes["hx-get"] = "/solutions/${solutionId}/remove-upvote"
                        attributes["hx-target"] = "vote-count-${solutionId}"

                        +"remove upvote button"
                    }
                }
            }
        }

        get ("/downvote") {
            val objectIds = validateObjectIds(call, "id") ?: return@get
            val solutionId = objectIds["id"]!!

            val userSession = call.sessions.get<UserSession>()!!
            val userId = ObjectId(userSession.id)

            val newVoteCount = solutionRepository.downvote(solutionId, userId)

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    +newVoteCount.toString()

                    button {
                        attributes["id"] = "downvote-btn-$solutionId"
                        attributes["hx-swap-oob"] = "true"
                        attributes["hx-get"] = "/solutions/${solutionId}/remove-downvote"
                        attributes["hx-target"] = "vote-count-${solutionId}"

                        +"remove downvote button"
                    }
                }
            }
        }


        get ("/remove-upvote") {
            val objectIds = validateObjectIds(call, "id") ?: return@get
            val solutionId = objectIds["id"]!!

            val userSession = call.sessions.get<UserSession>()!!
            val userId = ObjectId(userSession.id)

            val newVoteCount = solutionRepository.removeUpvote(solutionId, userId)

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    +newVoteCount.toString()

                    button {
                        attributes["id"] = "upvote-btn-$solutionId"
                        attributes["hx-swap-oob"] = "true"
                        attributes["hx-get"] = "/solutions/${solutionId}/upvote"
                        attributes["hx-target"] = "vote-count-${solutionId}"


                        +"upvote button"
                    }
                }
            }
        }

        get ("/remove-downvote") {
            val objectIds = validateObjectIds(call, "id") ?: return@get
            val solutionId = objectIds["id"]!!

            val userSession = call.sessions.get<UserSession>()!!
            val userId = ObjectId(userSession.id)

            val newVoteCount = solutionRepository.removeDownvote(solutionId, userId)

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    +newVoteCount.toString()

                    button {
                        attributes["id"] = "downvote-btn-$solutionId"
                        attributes["hx-swap-oob"] = "true"
                        attributes["hx-get"] = "/solutions/${solutionId}/downvote"
                        attributes["hx-target"] = "vote-count-${solutionId}"

                        +"downvote button"
                    }
                }
            }
        }
    }
}