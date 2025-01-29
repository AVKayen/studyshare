package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.forms.*
import com.physman.group.GroupRepository
import com.physman.solution.*
import com.physman.task.TaskRepository
import com.physman.templates.*
import com.physman.utils.smartRedirect
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
    route("/solutions") {
        route("/creation-modal") {
            getSolutionCreationModal(solutionCreationForm)
        }
        route("/deletion-modal") {
            getSolutionDeletionModal()
        }
        route("/{id}") {
            deleteSolution(solutionRepository, taskRepository, groupRepository)
            route("/{voteAction}") {
                postVote(solutionRepository)
            }
        }
        getSolutions(taskRepository, solutionRepository, groupRepository)
        postSolutionCreation(solutionRepository, groupRepository, solutionCreationForm)
    }
}

fun routeSolutionCreationForm(): Form {
    val solutionCreationForm = Form("Create a new solution", "solutionForm", formAttributes = mapOf(
        "hx-target" to "#solution-list",
        "hx-swap" to "afterbegin"
    ))
    solutionCreationForm.addInput(TextlikeInput("Title", "title", InputType.text, titleValidator))
    solutionCreationForm.addInput(TextlikeInput("Additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
    solutionCreationForm.addInput(FileInput("Upload files", "files", inputAttributes = mapOf("multiple" to "true")))

    globalFormRouter.routeFormValidators(solutionCreationForm)

    return solutionCreationForm
}

fun Route.getSolutionCreationModal(solutionCreationForm: Form) {
    get {
        val taskId = call.request.queryParameters["taskId"]
        val groupId = call.request.queryParameters["groupId"]

        if (taskId == null || groupId == null) {
            call.respondText("Task/Group Id not specified.", status = HttpStatusCode.BadRequest)
            return@get
        }
        call.respondHtml {
            body {
                formModalDialog(
                    form = solutionCreationForm,
                    callbackUrl = "/solutions?taskId=$taskId&groupId=$groupId",
                    requestType = POST
                )
            }
        }
    }
}

fun Route.getSolutionDeletionModal() {
    get {
        val solutionId = call.request.queryParameters["solutionId"]
        val groupId = call.request.queryParameters["groupId"]

        if (solutionId == null || groupId == null) {
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
                        "hx-delete" to "/solutions/$solutionId?groupId=$groupId",
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

        val objectIds = validateRequiredObjectIds(call, "taskId", "groupId") ?: return@get
        val taskId = objectIds["taskId"]!!
        val groupId = objectIds["groupId"]!!

        val optionalObjectIds = validateOptionalObjectIds(call, "lastId") ?: return@get
        val lastId = optionalObjectIds["lastId"]

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        if (!groupRepository.isUserMember(groupId, userId)) {
            call.smartRedirect("/")
        }

        val parentTask = taskRepository.getTask(taskId) ?: return@get
        val parentAuthorId = parentTask.task.authorId

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

fun Route.postSolutionCreation(solutionRepository: SolutionRepository, groupRepository: GroupRepository, solutionCreationForm: Form) {
    post {
        val objectIds = validateRequiredObjectIds(call, "taskId", "groupId") ?: return@post
        val taskId = objectIds["taskId"]!!
        val groupId = objectIds["groupId"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)
        val userName = userSession.name

        if (!groupRepository.isUserMember(groupId, userId)) {
            call.smartRedirect("/")
        }

        val formSubmissionData: FormSubmissionData = solutionCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!

        val solution = Solution(title = title, additionalNotes = additionalNotes, taskId = taskId, authorId = userId, authorName = userName)

        val solutionView = solutionRepository.createSolution(solution, formSubmissionData.files, userId)
        formSubmissionData.cleanup()

        call.respondHtml(HttpStatusCode.OK) {
            body {
                solutionTemplate(solutionView, true)
            }
        }
    }
}

fun Route.deleteSolution(solutionRepository: SolutionRepository, taskRepository: TaskRepository, groupRepository: GroupRepository) {
    delete {
        val objectIds = validateRequiredObjectIds(call, "id", "groupId") ?: return@delete
        val solutionId = objectIds["id"]!!
        val groupId = objectIds["groupId"]!!

        val solution = solutionRepository.getSolution(solutionId) ?: return@delete
        val parentTask = taskRepository.getTask(solution.taskId) ?: return@delete

        val parentAuthorId = parentTask.task.authorId
        val authorId = solution.authorId
        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)

        if (!groupRepository.isUserMember(groupId, userId)) {
            call.smartRedirect("/")
        }

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

fun Route.postVote(solutionRepository: SolutionRepository) {
    post  {
        val action = call.parameters["voteAction"]
        val objectIds = validateRequiredObjectIds(call, "id") ?: return@post
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