package com.physman

import com.physman.routes.*
import com.physman.forms.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
    routing {
        staticFiles("/static", File("static"))

        get("/status") {
            call.respondText("Working!")
        }

        route("/auth") {
            authRouter()
        }
        route("/forms") {
            configureForms(globalFormRouter)
        }
        route("/form") {
            formRouter()
        }

        route("/tasks") {
            taskRouter()
        }

        route("/images") {
            imageRouter()
        }

        authenticate("USER") {
            route("/") {
                homeRouter()
            }
        }
    }
}

