package com.studyshare.routes

import com.studyshare.authentication.user.UserSession
import com.studyshare.forms.FileInput
import com.studyshare.forms.Form
import com.studyshare.forms.TextlikeInput
import com.studyshare.forms.globalFormRouter
import com.studyshare.group.GroupRepository
import com.studyshare.templates.*
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import com.studyshare.forms.*
import com.studyshare.task.*
import com.studyshare.utils.*
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

        val taskView = try {
            taskRepository.getTask(taskId)
        } catch (e: ResourceNotFoundException) {
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
                taskTemplate(taskView, getAccessLevel(ObjectId(userSession.id), taskView.task.authorId))
                div {
                    classes = setOf("wide-button-container")
                    modalOpenButton(
                        buttonText = "Create a solution",
                        modalUrl = "/solutions/creation-modal?taskId=${taskView.task.id}&groupId=${groupId}"
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
    taskEditingForm.addInput(FileInput("Upload new files", "newFiles", inputAttributes = mapOf("multiple" to "true")))
    taskEditingForm.addInput(FileDeletionInput("deletedFiles", "taskEditFileDeletion"))

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

        val groupView = try {
            groupRepository.getGroup(groupId)
        } catch (e: ResourceNotFoundException) {
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

        if (id == null) {
            call.respondText("Id not specified.", status = HttpStatusCode.BadRequest)
            return@get
        }

        val taskView = try {
            taskRepository.getTask(ObjectId(id))
        } catch (e: ResourceNotFoundException) {
            call.respondText("Task not found.", status = HttpStatusCode.NotFound)
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
                    inputValues = mapOf(
                        "title" to taskView.task.title,
                        "additionalNotes" to (taskView.task.additionalNotes ?: "")
                    ),
                    filesToBeDeleted = mapOf("deletedFiles" to taskView.attachments)
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
        val group = try {
            groupRepository.getGroup(groupId)
        } catch (e: ResourceNotFoundException) {
            call.respondText("Group not found.", status = HttpStatusCode.NotFound)
            return@post
        }

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

fun Route.patchTaskEditing(taskRepository: TaskRepository, taskEditingForm: Form) {
    patch {
        val objectIds = validateRequiredObjectIds(call, "taskId") ?: return@patch
        val taskId = objectIds["taskId"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        val formSubmissionData: FormSubmissionData = taskEditingForm.validateSubmission(call) ?: return@patch
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!
        val deletedFiles = formSubmissionData.fields["deletedFiles"]!!

        val filesToDelete = parseObjectIdList(deletedFiles.dropLast(1).split(";"))

        if (filesToDelete == null) {
            call.respondText("Invalid value passed for the deletedFiles argument")
            return@patch
        }

        val previousTask = try {
            taskRepository.getTask(taskId)
        } catch (e: ResourceNotFoundException) {
            call.respondText("Task not found.", status = HttpStatusCode.NotFound)
            return@patch
        }

        if (previousTask.task.authorId != userId) {
            call.respondText("Resource Modification Restricted - Ownership Required", status = HttpStatusCode.Forbidden)
            return@patch
        }

        val taskUpdates = TaskUpdates(
            title = title,
            additionalNotes = additionalNotes,
            newFiles = formSubmissionData.files,
            filesToDelete = filesToDelete
        )

        val updatedTask = try {
            taskRepository.updateTask(taskId, taskUpdates)
        } catch (e: ResourceNotFoundException) {
            call.respondText("Task not found.", status = HttpStatusCode.NotFound)
            return@patch
        } finally {
            formSubmissionData.cleanup()
        }

        call.respondHtml(HttpStatusCode.OK) {
            body {
                taskTemplate(updatedTask, AccessLevel.EDIT)
            }
        }
    }
}

fun Route.deleteTask(taskRepository: TaskRepository, groupRepository: GroupRepository) {
    delete {
        val objectIds = validateRequiredObjectIds(call, "taskId") ?: return@delete
        val taskId = objectIds["taskId"]!!

        val task = try {
            taskRepository.getTask(taskId).task
        } catch (e: ResourceNotFoundException) {
            call.respondText("Task not found.", status = HttpStatusCode.NotFound)
            return@delete
        }

        val authorId = task.authorId
        val groupId = task.groupId
        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)

        if (authorId == userId) {
            val deletedTask = try {
                taskRepository.deleteTask(taskId)
            } catch (e: ResourceNotFoundException) {
                call.respondText("Task not found.", status = HttpStatusCode.NotFound)
                return@delete
            }
            if (!taskRepository.doesCategoryExist(groupId = groupId, category = deletedTask.category)) {
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