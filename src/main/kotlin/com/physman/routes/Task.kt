package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.forms.FileInput
import com.physman.forms.Form
import com.physman.forms.TextlikeInput
import com.physman.forms.globalFormRouter
import com.physman.group.GroupRepository
import com.physman.task.TaskRepository
import com.physman.task.additionalNotesValidator
import com.physman.task.titleValidator
import com.physman.templates.*
import com.physman.utils.validateObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import com.physman.forms.*
import com.physman.task.Task
import com.physman.utils.smartRedirect
import org.bson.types.ObjectId

fun Route.taskRouter(taskRepository: TaskRepository, groupRepository: GroupRepository) {
    val taskCreationForm = routeTaskForms()
    route("/{groupId}") {
        postTaskCreation(taskRepository, groupRepository, taskCreationForm)
        route("/{taskId}") {
            getTaskView(taskRepository, groupRepository)
            deleteTask(taskRepository)
        }
        route("/tasks") {
            getTaskList(taskRepository, groupRepository)
        }
        route("/creation-modal") {
            getTaskCreationModal(taskCreationForm)
        }
        route("/deletion-modal") {
            getTaskDeletionModal()
        }
    }
}

fun Route.getTaskView(taskRepository: TaskRepository, groupRepository: GroupRepository) {
    get {
        val objectIds = validateObjectIds(call, "taskId", "groupId") ?: return@get
        val taskId = objectIds["taskId"]!!
        val groupId = objectIds["groupId"]!!

        val taskView = taskRepository.getTask(taskId)
        if(taskView == null) {
            call.respondText(text = "Task not found.", status = HttpStatusCode.NotFound)
            return@get
        }

        val userSession = call.sessions.get<UserSession>()!!

        call.respondHtml(HttpStatusCode.OK) {
            index(
                title = "StudyShare",
                username = userSession.name,
                breadcrumbs = mapOf(taskView.task.groupName to "/${groupId}"),
                lastBreadcrumb = taskView.task.title
            ) {

                taskTemplate(taskView, isAuthor = userSession.id == taskView.task.authorId.toHexString())
                formModalOpenButton(
                    buttonText = "Create a solution",
                    modalUrl = "/solutions/creation-modal?taskId=${taskView.task.id}"
                )
                contentLoadTemplate(url = "/solutions?taskId=${taskView.task.id}")
            }
        }
    }
}

fun routeTaskForms(): Form {
    val taskCreationForm = Form("Create a new task", "taskForm", formAttributes = mapOf(
//        "hx-target" to "#task-list",
//        "hx-swap" to "beforeend"
          "hx-swap" to "none" // because the form is on an empty page now
    ))

    taskCreationForm.addInput(TextlikeInput("Title", "title", InputType.text, titleValidator))
    taskCreationForm.addInput(TextlikeInput("Additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
    taskCreationForm.addInput(FileInput("Upload files", "files", inputAttributes = mapOf("multiple" to "true")))

    globalFormRouter.routeFormValidators(taskCreationForm)

    return taskCreationForm
}

fun Route.getTaskList(taskRepository: TaskRepository, groupRepository: GroupRepository) {
    get {
        val objectIds = validateObjectIds(call, "groupId") ?: return@get
        val groupId = objectIds["groupId"]!!
        val group = groupRepository.getGroup(groupId) ?: return@get call.respond(HttpStatusCode.NotFound)
        val taskViews = taskRepository.getTasks(groupId)

        call.respondHtml {
            body {
                for (task in taskViews) {
                    taskPreviewTemplate(task)
                }
            }
        }
    }
}

fun Route.getTaskCreationModal(taskCreationForm: Form) {
    get {
        val groupId = validateObjectIds(call, "groupId")?.get("groupId") ?: return@get
        call.respondHtml {
            body {
                formModalDialog(
                    form = taskCreationForm,
                    callbackUrl = "/${groupId}",
                    requestType = POST
                )
            }
        }
    }
}

fun Route.getTaskDeletionModal() {
    get {
        val objectIds = validateObjectIds(call, "taskId", "groupId") ?: return@get
        val taskId = objectIds["taskId"]!!
        val groupId = objectIds["groupId"]!!
        call.respondHtml {
            body {
                confirmationModalTemplate(
                    title = "Delete task?",
                    details = "Are you sure you want to delete this task?",
                    submitText = "Delete",
                    submitAttributes = mapOf(
                        "hx-delete" to "/$groupId/$taskId",
                        "hx-target" to "#article-$taskId", // TODO instead of deleting task, navigate to a parent group page
                        "hx-swap" to "outerHTML"
                    )
                )
            }
        }
    }
}

fun Route.postTaskCreation(taskRepository: TaskRepository, groupRepository: GroupRepository, taskCreationForm: Form) {
    post {
        val formSubmissionData: FormSubmissionData = taskCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!
        val files = formSubmissionData.files

        val groupId = validateObjectIds(call, "groupId")?.get("groupId") ?: return@post
        val group = groupRepository.getGroup(groupId) ?: return@post call.respond(HttpStatusCode.NotFound)

        val userSession = call.sessions.get<UserSession>()!!

        val task = Task(
            title = title,
            additionalNotes = additionalNotes,
            authorName = userSession.name,
            authorId = ObjectId(userSession.id),
            groupName = group.group.title, // TODO: Replace with real group data
            groupId = groupId
        )

        taskRepository.createTask(task, files)
        formSubmissionData.cleanup()

        call.smartRedirect(redirectUrl = "/${groupId}/${task.id}")
    }
}

fun Route.deleteTask(taskRepository: TaskRepository) {
    delete { //todo: redirect to group
        val objectIds = validateObjectIds(call, "taskId") ?: return@delete
        val taskId = objectIds["taskId"]!!
        val task = taskRepository.getTask(taskId)?.task ?: return@delete
        val authorId = task.authorId
        val groupId = task.groupId
        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)

        if (authorId == userId) {
            taskRepository.deleteTask(taskId)
            call.response.headers.append("HX-Redirect", "/$groupId")
            call.respondText { "Task Deleted, redirecting??" }
        } else {
            call.respondText(
                "Resource Modification Restricted - Ownership Required",
                status = HttpStatusCode.Forbidden
            )
            return@delete
        }
    }
}