package com.physman.routes

import com.physman.task.InMemoryTaskRepository
import com.physman.task.Task
import com.physman.templates.index
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import com.physman.templates.taskTemplate
import io.ktor.server.request.*
import kotlinx.html.body
import kotlin.random.Random

fun Route.taskRouter() {

    post {
        val formParameters = call.receiveParameters()
        val title = formParameters["title"].toString()
        val additionalNotes = formParameters["additionalNotes"].toString()

        if(title.isEmpty() || title.length > 48) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        }

        val newTask = Task(id = Random.nextInt(99999), title = title, additionalNotes = additionalNotes)

        val task = InMemoryTaskRepository.createTask(newTask)

        call.respondHtml(HttpStatusCode.OK) {
            body {
                taskTemplate(task)
            }
        }
    }

    route("/{id}") {
        get {
            val taskId = call.parameters["id"]?.toIntOrNull()
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
    }
}