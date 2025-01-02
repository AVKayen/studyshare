package com.physman

import com.physman.image.CloudImageRepository
import com.physman.task.InMemoryTaskRepository
import com.physman.authentication.configureSecurity
import io.ktor.server.application.*

import com.mongodb.kotlin.client.coroutine.MongoClient

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

val mongodbConnectionString = System.getenv("MONGODB_CONNECTION_STRING")
val mongodbClient = MongoClient.create(mongodbConnectionString)
val database = mongodbClient.getDatabase("studyshare")
val taskRepository = InMemoryTaskRepository()
val imageRepository = CloudImageRepository("skillful-fx-446014-k1", "studyshare-files", database)

fun Application.module() {
    configureSecurity()
    configureRouting(
        taskRepository = taskRepository,
        imageRepository = imageRepository,
    )
}
