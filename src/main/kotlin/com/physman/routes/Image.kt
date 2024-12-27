package com.physman.routes

import com.physman.image.ImageRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.imageRouter(imageRepository: ImageRepository) {

    get("/{id}") {
        try {
            val id = call.parameters["id"]

            val image = imageRepository.getFile(id!!)
            if (image == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@get
            }

            val file = File(image.filename)
            file.writeBytes(image.content.data)

            call.respondFile(file)

        } catch (e: IllegalArgumentException) {
            call.response.status(HttpStatusCode.BadRequest)
            return@get
        }
    }
}