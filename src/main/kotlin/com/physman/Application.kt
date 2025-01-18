package com.physman

import com.physman.attachment.MongoGCloudAttachmentRepository
import com.physman.authentication.user.MongoUserRepository
import com.physman.authentication.configureSecurity
import io.ktor.server.application.*

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.physman.comment.MongoCommentRepository
import com.physman.solution.MongoSolutionRepository
import com.physman.task.MongoTaskRepository
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val environment: Environment = Environment(true)
    val mongodbClient = MongoClient.create(environment.MONGODB_CONNECTION_STRING)
    val database = mongodbClient.getDatabase("studyshare")

    val imageRepository = MongoGCloudAttachmentRepository(bucketName = "studyshare-files", database = database)
    val commentRepository = MongoCommentRepository(database)
    val solutionRepository = MongoSolutionRepository(database, commentRepository, imageRepository)
    val taskRepository = MongoTaskRepository(database, imageRepository, commentRepository, solutionRepository)
    val userRepository = MongoUserRepository(database)

    configureSecurity()
    configureRouting(
        solutionRepository = solutionRepository,
        taskRepository = taskRepository,
        userRepository = userRepository,
        commentRepository = commentRepository,
    )
}
