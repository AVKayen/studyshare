package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.forms.*
import com.physman.group.GroupRepository
import com.physman.solution.*
import com.physman.task.TaskRepository
import com.physman.templates.*
import com.physman.utils.getAccessLevel
import com.physman.utils.validateGroupBelonging
import com.physman.utils.validateOptionalObjectIds
import com.physman.utils.validateRequiredObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.bson.types.ObjectId

fun Route.solutionRouter(solutionRepository: SolutionRepository, taskRepository: TaskRepository, groupRepository: GroupRepository) {
    val solutionCreationForm = routeSolutionCreationForm()
    val solutionEditingForm = routeSolutionEditingForm()
    route("/solutions") {
        route("/creation-modal") {
            getSolutionCreationModal(solutionCreationForm)
        }
        route("/editing-modal") {
            getSolutionEditingModal(solutionEditingForm, solutionRepository)
        }
        route("/deletion-modal") {
            getSolutionDeletionModal()
        }
        route("/{id}") {
            patchSolutionEditing(solutionRepository, solutionEditingForm)
            deleteSolution(solutionRepository, taskRepository)
            route("/{voteAction}") {
                postVote(solutionRepository, groupRepository)
            }
        }
        getSolutions(taskRepository, solutionRepository, groupRepository)
        postSolutionCreation(taskRepository, solutionRepository, groupRepository, solutionCreationForm)
    }
}

fun routeSolutionCreationForm(): Form {
    val solutionCreationForm = Form("Create a new solution", "solutionForm", formAttributes = mapOf(
        "hx-target" to "#solution-list",
        "hx-swap" to "afterbegin"
    )
    )
    solutionCreationForm.addInput(TextlikeInput("Title", "title", InputType.text, titleValidator))
    solutionCreationForm.addInput(TextlikeInput("Additional notes", "additionalNotes", InputType.text, additionalNotesValidator))

    globalFormRouter.routeFormValidators(solutionCreationForm)

    return solutionCreationForm
}

fun routeSolutionEditingForm(): Form {
    val solutionEditingForm = Form("Edit your solution", "solutionEditingForm", formAttributes = mapOf(
        "hx-swap" to "outerHTML"
    ))

    solutionEditingForm.addInput(TextlikeInput("New Title", "title", InputType.text, titleValidator))
    solutionEditingForm.addInput(TextlikeInput("New Additional notes", "additionalNotes", InputType.text, additionalNotesValidator))

    globalFormRouter.routeFormValidators(solutionEditingForm)

    return solutionEditingForm
}

fun Route.getSolutionCreationModal(solutionCreationForm: Form) {
    get {
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
}

fun Route.getSolutionEditingModal(solutionEditingForm: Form, solutionRepository: SolutionRepository) {
    get {
        val id = call.request.queryParameters["id"]
        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        if (id == null) {
            call.respondText("Id not specified.", status = HttpStatusCode.BadRequest)
            return@get
        }

        val solutionView = solutionRepository.getSolution(ObjectId(id), userId) ?: return@get

        call.respondHtml {
            body {
                formModalDialog(
                    form = solutionEditingForm,
                    callbackUrl = "/solutions/$id",
                    requestType = HtmxRequestType.PATCH,
                    extraAttributes = mapOf(
                        "hx-target" to "#article-${id}"
                    ),
                    inputValues = mapOf(
                        "title" to solutionView.solution.title,
                        "additionalNotes" to (solutionView.solution.additionalNotes ?: "")
                    )
                )
            }
        }
    }
}

fun Route.getSolutionDeletionModal() {
    get {
        val solutionId = call.request.queryParameters["solutionId"]

        if (solutionId == null) {
            call.respondText("Solution Id not specified.", status = HttpStatusCode.BadRequest)
            return@get
        }

        call.respondHtml {
            body {
                confirmationModalTemplate(
                    title = "Delete solution?",
                    details = "Are you sure you want to delete this solution?",
                    submitText = "Delete",
                    submitAttributes = mapOf(
                        "hx-delete" to "/solutions/$solutionId",
                        "hx-target" to "#article-$solutionId",
                        "hx-swap" to "outerHTML"
                    )
                )
            }
        }
    }
}

fun Route.getSolutions(taskRepository: TaskRepository, solutionRepository: SolutionRepository, groupRepository: GroupRepository) {
    get {

        val pageSize = 8

        val objectIds = validateRequiredObjectIds(call, "taskId") ?: return@get
        val taskId = objectIds["taskId"]!!

        val optionalObjectIds = validateOptionalObjectIds(call, "lastId") ?: return@get
        val lastId = optionalObjectIds["lastId"]

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        val parentTask = taskRepository.getTask(taskId) ?: return@get
        val parentAuthorId = parentTask.task.authorId

        if (!validateGroupBelonging(call, groupRepository, parentTask.task.groupId)) return@get

        val solutionViews = solutionRepository.getSolutions(
            taskId = taskId, userId = userId, lastId = lastId, resultCount = pageSize
        )


        call.respondHtml {
            body {
                div {
                    attributes["id"] = "solution-list"

                    for (solutionView in solutionViews) {
                        val accessLevel = getAccessLevel(userId, solutionView.solution.authorId, parentAuthorId)
                        solutionTemplate(solutionView, accessLevel)
                    }
                    if (solutionViews.size == pageSize) {
                        val newLastId = solutionViews.last().solution.id
                        contentLoadTemplate(url = "/solutions?taskId=$taskId&lastId=$newLastId")
                    }
                }
            }
        }
    }
}

fun Route.postSolutionCreation(taskRepository: TaskRepository, solutionRepository: SolutionRepository, groupRepository: GroupRepository, solutionCreationForm: Form) {
    post {
        val objectIds = validateRequiredObjectIds(call, "taskId") ?: return@post
        val taskId = objectIds["taskId"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)
        val userName = userSession.name

        val formSubmissionData: FormSubmissionData = solutionCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!

        val task = taskRepository.getTask(taskId) ?: return@post call.respond(HttpStatusCode.NotFound)

        if (!validateGroupBelonging(call, groupRepository, task.task.groupId)) return@post

        val solution = Solution(title = title, additionalNotes = additionalNotes, taskId = taskId, authorId = userId, authorName = userName, groupId = task.task.groupId, groupName = task.task.groupName)

        val solutionView = solutionRepository.createSolution(solution, formSubmissionData.files, userId)
        formSubmissionData.cleanup()

        call.respondHtml(HttpStatusCode.OK) {
            body {
                solutionTemplate(solutionView, AccessLevel.EDIT)
            }
        }
    }
}

fun Route.patchSolutionEditing(solutionRepository: SolutionRepository, solutionEditingForm: Form) {
    patch {
        val objectIds = validateRequiredObjectIds(call, "id") ?: return@patch
        val solutionId = objectIds["id"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        val formSubmissionData: FormSubmissionData = solutionEditingForm.validateSubmission(call) ?: return@patch
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!
        formSubmissionData.cleanup()

        val previousSolution = solutionRepository.getSolution(solutionId, userId) ?: return@patch call.respond(HttpStatusCode.NotFound)

        if (previousSolution.solution.authorId != userId) {
            call.respondText("Resource Modification Restricted - Ownership Required", status = HttpStatusCode.Forbidden)
            return@patch
        }

        val newSolution = previousSolution.solution.copy(
            title = title,
            additionalNotes = additionalNotes
        )

        solutionRepository.updateSolution(solutionId, newSolution)

        val newSolutionView = SolutionView(newSolution, previousSolution.attachments, previousSolution.isUpvoted, previousSolution.isDownvoted)

        call.respondHtml(HttpStatusCode.OK) {
            body {
                solutionTemplate(newSolutionView, AccessLevel.EDIT)
            }
        }
    }
}

fun Route.deleteSolution(solutionRepository: SolutionRepository, taskRepository: TaskRepository) {
    delete {
        val objectIds = validateRequiredObjectIds(call, "id") ?: return@delete
        val solutionId = objectIds["id"]!!
        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)

        val solutionView = solutionRepository.getSolution(solutionId, userId) ?: return@delete
        val parentTask = taskRepository.getTask(solutionView.solution.taskId) ?: return@delete

        val parentAuthorId = parentTask.task.authorId
        val authorId = solutionView.solution.authorId

        if (authorId == userId || parentAuthorId == userId) {
            solutionRepository.deleteSolution(solutionId)
            call.respondHtml { body() }
        }
        else {
            call.respondText("Resource Modification Restricted - Ownership Required", status = HttpStatusCode.Forbidden)
            return@delete
        }
    }
}

fun Route.postVote(solutionRepository: SolutionRepository, groupRepository: GroupRepository) {
    post  {
        val action = call.parameters["voteAction"]
        val objectIds = validateRequiredObjectIds(call, "id") ?: return@post
        val solutionId = objectIds["id"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        if (!validateGroupBelonging(call, groupRepository, solutionRepository.getSolution(solutionId, userId)?.solution?.groupId)) return@post

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