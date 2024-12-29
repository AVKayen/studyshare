package com.physman

import com.physman.image.InMemoryImageRepository
import com.physman.task.InMemoryTaskRepository
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val taskRepository = InMemoryTaskRepository
    val imageRepository = InMemoryImageRepository

    configureSecurity()
    configureRouting(
        taskRepository = taskRepository,
        imageRepository = imageRepository,
    )
}
