package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.forms.FileInput
import com.physman.forms.Form
import com.physman.forms.TextlikeInput
import com.physman.forms.globalFormRouter
import com.physman.group.GroupRepository
import com.physman.templates.*
import com.physman.utils.validateRequiredObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import com.physman.forms.*
import com.physman.task.*
import com.physman.utils.smartRedirect
import com.physman.utils.validateGroupBelonging
import com.physman.utils.validateOptionalObjectIds
import org.bson.types.ObjectId

fun Route.taskRouter(taskRepository: TaskRepository, groupRepository: GroupRepository) {
    val taskCreationForm = routeTaskCreationForm()
    val taskEditingForm = routeTaskEditingForm()
    route("/{groupId}") {
        postTaskCreation(taskRepository, groupRepository, taskCreationForm)
        route("/{taskId}") {
            getTaskView(taskRepository, groupRepository)
            patchTaskEditing(taskRepository, taskEditingForm)
            deleteTask(taskRepository, groupRepository)
        }
        route("/tasks") {
            getTaskList(taskRepository, groupRepository)
        }
        route("/creation-modal") {
            getTaskCreationModal(taskCreationForm, groupRepository)
        }
        route("/editing-modal") {
            getTaskEditingModal(taskEditingForm, taskRepository)
        }
        route("/deletion-modal") {
            getTaskDeletionModal()
        }
    }
}

fun Route.getTaskView(taskRepository: TaskRepository, groupRepository: GroupRepository) {
    get {
        validateGroupBelonging(call, groupRepository)

        val objectIds = validateRequiredObjectIds(call, "taskId", "groupId") ?: return@get
        val taskId = objectIds["taskId"]!!
        val groupId = objectIds["groupId"]!!

        val taskView = taskRepository.getTask(taskId)
        if(taskView == null) {
            call.respondText(text = "Task not found.", status = HttpStatusCode.NotFound)
            return@get
        }
        if(taskView.task.groupId != groupId) {
            call.respondText(text = "Task does not belong to this group. How did we get here?", status = HttpStatusCode.NotFound)
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
                div {
                    classes = setOf("wide-button-container")
                    formModalOpenButton(
                        buttonText = "Create a solution",
                        modalUrl = "/solutions/creation-modal?taskId=${taskView.task.id}&groupId=${groupId}",
                        additionalClasses = setOf("wide-button", "outline")
                    )
                }
                contentLoadTemplate(url = "/solutions?taskId=${taskView.task.id}&groupId=${groupId}")
            }
        }
    }
}

fun routeTaskCreationForm(): Form {
    val taskCreationForm = Form("Create a new task", "taskForm", formAttributes = mapOf(
        "hx-swap" to "none"
    ))

    taskCreationForm.addInput(TextlikeInput("Title", "title", InputType.text, titleValidator))
    taskCreationForm.addInput(TextlikeInput("Category", "category", InputType.text, categoryValidator))
    taskCreationForm.addInput(TextlikeInput("Additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
    taskCreationForm.addInput(FileInput("Upload files", "files", inputAttributes = mapOf("multiple" to "true")))

    globalFormRouter.routeFormValidators(taskCreationForm)

    return taskCreationForm
}

fun routeTaskEditingForm(): Form {
    val taskEditingForm = Form("Edit your task", "taskEditingForm", formAttributes = mapOf(
        "hx-swap" to "outerHTML"
    ))

    taskEditingForm.addInput(TextlikeInput("New Title", "title", InputType.text, titleValidator))
    taskEditingForm.addInput(TextlikeInput("New Additional notes", "additionalNotes", InputType.text, additionalNotesValidator))

    globalFormRouter.routeFormValidators(taskEditingForm)

    return taskEditingForm
}

fun Route.getTaskList(taskRepository: TaskRepository, groupRepository: GroupRepository) {
    get {
        validateGroupBelonging(call, groupRepository)

        val pageSize = 50

        val objectIds = validateRequiredObjectIds(call, "groupId") ?: return@get
        val groupId = objectIds["groupId"]!!

        val optionalObjectIds = validateOptionalObjectIds(call, "lastId") ?: return@get
        val lastId = optionalObjectIds["lastId"]

        val category = call.request.queryParameters["category"]
        if (category == null) {
            call.respondText(text = "Category must not be null.", status = HttpStatusCode.BadRequest)
            return@get
        }

        val tasks = taskRepository.getTasks(
            groupId = groupId, category = category, lastId = lastId, resultCount = pageSize
        )

        call.respondHtml {
            body {
                for (task in tasks) {
                    taskPreviewTemplate(task)
                }
                if (tasks.size == pageSize) {
                    val newLastId = tasks.last().id
                    contentLoadTemplate(url = "/$groupId/tasks?lastId=$newLastId&category=$category")
                }
            }
        }
    }
}

fun Route.getTaskCreationModal(taskCreationForm: Form, groupRepository: GroupRepository) {
    get {
        val groupId = validateRequiredObjectIds(call, "groupId")?.get("groupId") ?: return@get
        val groupView = groupRepository.getGroup(groupId)
        if (groupView == null) {
            call.respondText(text = "Group does not exist.", status = HttpStatusCode.NotFound)
            return@get
        }
        call.respondHtml {
            body {
                formModalDialog(
                    form = taskCreationForm,
                    callbackUrl = "/${groupId}",
                    inputDataLists = mapOf("category" to groupView.group.taskCategories)
                )
            }
        }
    }
}

fun Route.getTaskEditingModal(taskEditingForm: Form, taskRepository: TaskRepository) {
    get {
        val id = call.request.queryParameters["taskId"]
        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        if (id == null) {
            call.respondText("Id not specified.", status = HttpStatusCode.BadRequest)
            return@get
        }

        val taskView = taskRepository.getTask(ObjectId(id)) ?: return@get

        if (taskView.task.authorId != userId) {
            call.respondText("Resource Modification Restricted - Ownership Required", status = HttpStatusCode.Forbidden)
            return@get
        }

        call.respondHtml {
            body {
                formModalDialog(
                    form = taskEditingForm,
                    callbackUrl = "/tasks/$id",
                    requestType = HtmxRequestType.PATCH,
                    extraAttributes = mapOf(
                        "hx-target" to "#article-${id}"
                    ),
                    inputValues = listOf(
                        taskView.task.title,
                        taskView.task.additionalNotes
                    )
                )
            }
        }
    }
}

fun Route.getTaskDeletionModal() {
    get {
        val objectIds = validateRequiredObjectIds(call, "taskId", "groupId") ?: return@get
        val taskId = objectIds["taskId"]!!
        val groupId = objectIds["groupId"]!!
        call.respondHtml {
            body {
                confirmationModalTemplate(
                    title = "Delete task?",
                    details = "Are you sure you want to delete this task?",
                    submitText = "Delete",
                    submitAttributes = mapOf(
                        "hx-delete" to "/$groupId/$taskId"
                    )
                )
            }
        }
    }
}

fun Route.postTaskCreation(taskRepository: TaskRepository, groupRepository: GroupRepository, taskCreationForm: Form) {
    post {
        validateGroupBelonging(call, groupRepository)

        val formSubmissionData: FormSubmissionData = taskCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val category = formSubmissionData.fields["category"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!
        val files = formSubmissionData.files

        val groupId = validateRequiredObjectIds(call, "groupId")?.get("groupId") ?: return@post
        val group = groupRepository.getGroup(groupId) ?: return@post call.respond(HttpStatusCode.NotFound)

        val userSession = call.sessions.get<UserSession>()!!

        val task = Task(
            title = title,
            additionalNotes = additionalNotes,
            authorName = userSession.name,
            authorId = ObjectId(userSession.id),
            groupName = group.group.title,
            groupId = groupId,
            category = category.lowercase().replaceFirstChar(Char::titlecase)
        )

        taskRepository.createTask(task, files)
        groupRepository.addTaskCategory(groupId = groupId, taskCategory = task.category)
        formSubmissionData.cleanup()

        call.smartRedirect(redirectUrl = "/${groupId}/${task.id}")
    }
}
//TODO: if form is not right dont post it
fun Route.patchTaskEditing(taskRepository: TaskRepository, taskEditingForm: Form) {
    patch {
        val objectIds = validateRequiredObjectIds(call, "taskId") ?: return@patch
        val taskId = objectIds["taskId"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        val formSubmissionData: FormSubmissionData = taskEditingForm.validateSubmission(call) ?: return@patch
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!
        formSubmissionData.cleanup()

        val previousTask = taskRepository.getTask(taskId) ?: return@patch call.respond(HttpStatusCode.NotFound)

        if (previousTask.task.authorId != userId) {
            call.respondText("Resource Modification Restricted - Ownership Required", status = HttpStatusCode.Forbidden)
            return@patch
        }

        val newTask = Task(
            title = title,
            additionalNotes = additionalNotes,
            id = previousTask.task.id,
            authorName = previousTask.task.authorName,
            authorId = previousTask.task.authorId,
            groupName = previousTask.task.groupName,
            groupId = previousTask.task.groupId,
            commentAmount = previousTask.task.commentAmount,
            attachmentIds = previousTask.task.attachmentIds,
            category = previousTask.task.category)

        taskRepository.updateTask(taskId, newTask)

        val newTaskView = TaskView(newTask, previousTask.attachments)

        call.respondHtml(HttpStatusCode.OK) {
            body {
                taskTemplate(newTaskView, true)
            }
        }
    }
}

fun Route.deleteTask(taskRepository: TaskRepository, groupRepository: GroupRepository) {
    delete {
        val objectIds = validateRequiredObjectIds(call, "taskId") ?: return@delete
        val taskId = objectIds["taskId"]!!
        val task = taskRepository.getTask(taskId)?.task ?: return@delete
        val authorId = task.authorId
        val groupId = task.groupId
        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)

        if (authorId == userId) {
            val deletedTask = taskRepository.deleteTask(taskId)
            if (deletedTask != null && !taskRepository.doesCategoryExist(groupId = groupId, category = deletedTask.category)) {
                groupRepository.removeTaskCategory(groupId = groupId, taskCategory = deletedTask.category)
            }

            call.smartRedirect(redirectUrl = "/$groupId")
        } else {
            call.respondText(
                "Resource Modification Restricted - Ownership Required",
                status = HttpStatusCode.Forbidden
            )
            return@delete
        }
    }
}