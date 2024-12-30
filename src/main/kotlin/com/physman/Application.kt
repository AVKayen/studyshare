package com.physman

import com.physman.image.CloudImageRepository
import com.physman.task.InMemoryTaskRepository
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

val mongodbConnectionString = System.getenv("MONGODB_CONNECTION_STRING")

val taskRepository = InMemoryTaskRepository()
val imageRepository = CloudImageRepository("skillful-fx-446014-k1", "studyshare-files", mongodbConnectionString)

fun Application.module() {
    configureSecurity()
    configureRouting(
        taskRepository = taskRepository,
        imageRepository = imageRepository,
    )
}
