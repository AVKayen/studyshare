package com.physman.routes

import com.physman.forms.*
import com.physman.task.InMemoryTaskRepository
import com.physman.task.Task
import com.physman.templates.index
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import com.physman.templates.taskTemplate
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.html.InputType
import kotlinx.html.article
import kotlinx.html.body
import kotlinx.html.div

const val TITLE_MAX_LENGTH = 512
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

    val taskCreationForm = Form("Create a new task", "taskForm", "tasks", mapOf(
        "hx-target" to "#task-list",
        "hx-swap" to "beforeend"
    ))
    taskCreationForm.addInput(TextlikeInput("title", "title", InputType.text, titleValidator))
    taskCreationForm.addInput(TextlikeInput("additional notes", "additionalNotes", InputType.text, additionalNotesValidator))

    globalFormRouter.routeForm(taskCreationForm)

    get {
        val tasks = InMemoryTaskRepository.getAllTasks()
        call.respondHtml {
            index("Tasks") {
                div {
                    attributes["id"] = "task-list"
                    for (task in tasks) {
                        taskTemplate(task)
                    }
                }
                article {
                    taskCreationForm.render(this)
                }
            }
        }
    }

    post {
        val formParameters = call.receiveParameters()
        val title = formParameters["title"].toString()
        val additionalNotes = formParameters["additionalNotes"].toString()

        val error: String? = titleValidator(title) ?: additionalNotesValidator(additionalNotes)
        if(error != null) {
            call.respondText(error, status = HttpStatusCode.BadRequest)
        }
        println(title)
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
                call.response.status(HttpStatusCode.NotFound)
                return@get
            }

            call.respondHtml(HttpStatusCode.OK) {
                index("Task") {
                    taskTemplate(task)
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
                call.response.status(HttpStatusCode.NotFound)
                return@delete
            }
            call.response.status(HttpStatusCode.NoContent)
        }


        route("/solutions") {
//            solutionRouter()
        }
    }
}