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

            val image = imageRepository.getImageLink(id!!)

            call.respondRedirect(image!!)
        } catch (e: IllegalArgumentException) {
            call.response.status(HttpStatusCode.BadRequest)
            return@get
        }
    }
}