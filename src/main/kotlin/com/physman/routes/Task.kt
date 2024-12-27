package com.physman.routes

import com.physman.forms.*
import com.physman.task.InMemoryTaskRepository
import com.physman.task.Task
import com.physman.templates.index
import com.physman.templates.solutionTemplate
import com.physman.templates.taskPreviewTemplate
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import com.physman.templates.taskTemplate
import io.ktor.server.response.*
import kotlinx.html.InputType
import kotlinx.html.body

const val TITLE_MAX_LENGTH = 5
const val ADDITIONAL_NOTES_MAX_LENGTH = 512

val titleValidator = fun(title: String): String? {
     if(title.isEmpty()) {
        return "Title must not be empty"
     }
     if (title.length > TITLE_MAX_LENGTH) {
        return "Title too long (max length $TITLE_MAX_LENGTH)"
     }
     return null
}

val additionalNotesValidator = fun(additionalNotes: String): String? {
    if(additionalNotes.length > ADDITIONAL_NOTES_MAX_LENGTH) {
        return "Additional notes too long (max length $ADDITIONAL_NOTES_MAX_LENGTH)"
    }
    return null
}

fun Route.taskRouter() {

    val taskCreationForm = Form("Create a new task", "taskForm", mapOf(
//        "hx-target" to "#task-list",
//        "hx-swap" to "beforeend"
          "hx-swap" to "none" // because the form is on an empty page now
    ))
    taskCreationForm.addInput(TextlikeInput("title", "title", InputType.text, titleValidator))
    taskCreationForm.addInput(TextlikeInput("additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
    taskCreationForm.addInput(FileInput("files", "files", acceptedTypes = listOf("image/*")))

    globalFormRouter.routeFormValidators(taskCreationForm)

    get {
        val tasks = InMemoryTaskRepository.getAllTasks()
        call.respondHtml {
            body {
                for (task in tasks) {
                    taskPreviewTemplate(task)
                }
            }
        }
    }

    get("/creation-form") {
        call.respondHtml {
//            body {
//                taskCreationForm.render(this, "/tasks")
//            }

            // index because of lack of htmx needed for testing (htmx is served with index page only)
            index("This won't be index") {
                taskCreationForm.render(this, "/tasks")
            }
        }
    }

    post {
        val formSubmissionData: FormSubmissionData = taskCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!
//        val files = formSubmissionData.files!!

        val newTask = Task(title = title, additionalNotes = additionalNotes)

        val task = InMemoryTaskRepository.createTask(newTask)

        call.respondHtml(HttpStatusCode.OK) {
            body {
                taskTemplate(task)
            }
        }
    }

    route("/{id}") {
        get {
            val taskId = call.parameters["id"]
            if(taskId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@get
            }

            val task = InMemoryTaskRepository.getTask(taskId)
            if(task == null) {
                call.respondText(text = "Task not found.", status = HttpStatusCode.NotFound)
                return@get
            }

            call.respondHtml(HttpStatusCode.OK) {
                index("Task") {
                    taskTemplate(task)
                    for (solution in task.solutions){
                        solutionTemplate(solution)
                    }
                }
            }
        }

        delete {
            val taskId = call.parameters["id"]
            if(taskId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@delete
            }

            val deletedTask = InMemoryTaskRepository.deleteTask(taskId)
            if(deletedTask == null) {
                call.respondText(text = "Task not found.", status = HttpStatusCode.NotFound)
                return@delete
            }
            call.response.status(HttpStatusCode.NoContent)
        }


        route("/solutions") {
            solutionRouter()
        }
    }
}