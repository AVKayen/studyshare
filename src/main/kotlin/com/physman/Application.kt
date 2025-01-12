package com.physman

import com.physman.attachment.MongoGCloudAttachmentRepository
import com.physman.authentication.user.MongoUserRepository
import com.physman.authentication.configureSecurity
import io.ktor.server.application.*

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.physman.solution.MongoSolutionRepository
import com.physman.task.MongoTaskRepository

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongodbClient = MongoClient.create(System.getenv("MONGODB_CONNECTION_STRING"))
    val database = mongodbClient.getDatabase("studyshare")

    val imageRepository = MongoGCloudAttachmentRepository(bucketName = "studyshare-files", database = database)
    val solutionRepository = MongoSolutionRepository(database, imageRepository)
    val taskRepository = MongoTaskRepository(database, imageRepository, solutionRepository)
    val userRepository = MongoUserRepository(database)

    configureSecurity()
    configureRouting(
        solutionRepository = solutionRepository,
        taskRepository = taskRepository,
        userRepository = userRepository,
    )
}
