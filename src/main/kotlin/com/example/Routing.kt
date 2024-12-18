package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.h1
import java.io.File

fun Application.configureRouting() {
    routing {
        staticFiles("/static", File("static"))

        get("/status") {
            call.respondText("Working!")
        }


        authenticate("USER") {
            get("/") {
                val userSession = call.sessions.get<UserSession>()!!
                call.respondHtml(HttpStatusCode.OK) {
                    index(title = "String") {
                        h1 {
                            +userSession.username
                        }
                    }
                }
            }
        }
    }
}

