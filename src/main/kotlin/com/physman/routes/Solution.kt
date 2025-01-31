package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.forms.*
import com.physman.group.GroupRepository
import com.physman.solution.*
import com.physman.task.TaskRepository
import com.physman.templates.*
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
            getSolutionEditingModal(solutionEditingForm)
        }
        route("/deletion-modal") {
            getSolutionDeletionModal()
        }
        route("/{id}") {
            patchSolutionEditing(taskRepository, solutionRepository, solutionEditingForm)
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
    val solutionCreationForm = Form("Create a new solution", "solutionForm", formAttributes = mutableMapOf(
        "hx-target" to "#solution-list",
        "hx-swap" to "afterbegin"
    )
    )
    solutionCreationForm.addInput(TextlikeInput("Title", "title", InputType.text, titleValidator))
    solutionCreationForm.addInput(TextlikeInput("Additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
    solutionCreationForm.addInput(FileInput("Upload files", "files", inputAttributes = mapOf("multiple" to "true")))

    globalFormRouter.routeFormValidators(solutionCreationForm)

    return solutionCreationForm
}

fun routeSolutionEditingForm(): Form {
    val solutionEditingForm = Form("Edit your solution", "solutionEditingForm", formAttributes = mutableMapOf())

    solutionEditingForm.addInput(TextlikeInput("New Title", "title", InputType.text, titleValidator))
    solutionEditingForm.addInput(TextlikeInput("New Additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
    solutionEditingForm.addInput(FileInput("New Upload files", "files", inputAttributes = mapOf("multiple" to "true")))

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
                    callbackUrl = "/solutions?taskId=$taskId",
                    requestType = POST
                )
            }
        }
    }
}

fun Route.getSolutionEditingModal(solutionEditingForm: Form) {
    get {
        val id = call.request.queryParameters["id"]
        val taskId = call.request.queryParameters["taskId"]

        if (taskId == null) {
            call.respondText("Task Id not specified.", status = HttpStatusCode.BadRequest)
            return@get
        }

        solutionEditingForm.formAttributes["hx-target"] = "#article-${id}"
        solutionEditingForm.formAttributes["hx-swap"] = "outerHTML"

        call.respondHtml {
            body {
                formModalDialog(
                    form = solutionEditingForm,
                    callbackUrl = "/solutions/$id?taskId=${taskId}",
                    requestType = PATCH
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
                        val isAuthor: Boolean = userId == solutionView.solution.authorId
                                || userId == parentAuthorId

                        solutionTemplate(solutionView, isAuthor)
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
                solutionTemplate(solutionView, true)
            }
        }
    }
}

fun Route.patchSolutionEditing(taskRepository: TaskRepository, solutionRepository: SolutionRepository, solutionEditingForm: Form) {
    patch {
        val objectIds = validateRequiredObjectIds(call, "id", "taskId") ?: return@patch
        val solutionId = objectIds["id"]!!
        val taskId = objectIds["taskId"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)
        val userName = userSession.name

        val formSubmissionData: FormSubmissionData = solutionEditingForm.validateSubmission(call) ?: return@patch
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!

        val task = taskRepository.getTask(taskId) ?: return@patch call.respond(HttpStatusCode.NotFound)

        val solution = Solution(title = title, additionalNotes = additionalNotes, taskId = taskId, authorId = userId, authorName = userName, groupId = task.task.groupId, groupName = task.task.groupName)

        //TODO: change this to incorporate attachments
        val solutionView1 = SolutionView(solution, emptyList(), false, false)
        println("hi3.1")
        val solutionView = solutionRepository.updateSolution(solutionId, solutionView1)
        println("hi3.2")
        formSubmissionData.cleanup()

        call.respondHtml(HttpStatusCode.OK) {
            body {
                solutionTemplate(solutionView, true)
            }
        }
    }
}

fun Route.deleteSolution(solutionRepository: SolutionRepository, taskRepository: TaskRepository) {
    delete {
        println("now1")
        val objectIds = validateRequiredObjectIds(call, "id") ?: return@delete
        val solutionId = objectIds["id"]!!

        val solution = solutionRepository.getSolution(solutionId) ?: return@delete
        val parentTask = taskRepository.getTask(solution.taskId) ?: return@delete
        println("now2")
        val parentAuthorId = parentTask.task.authorId
        val authorId = solution.authorId
        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)

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

        if (!validateGroupBelonging(call, groupRepository, solutionRepository.getSolution(solutionId)?.groupId)) return@post

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