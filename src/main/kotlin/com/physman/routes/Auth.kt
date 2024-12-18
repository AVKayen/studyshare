package com.physman.routes

import com.physman.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*


fun Route.authRouter() {
    route("/login") {
        get {
            val session = call.sessions.get<UserSession>() ?: UserSession("Whoa")
            call.sessions.set(session)
            call.respondRedirect("/")
        }
    }
}