package com.physman.routes

import com.physman.task.InMemoryTaskRepository
import com.physman.templates.index
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import com.physman.templates.taskTemplate

fun Route.taskRouter() {
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