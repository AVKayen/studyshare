package com.physman.routes

import com.physman.UserSession
import com.physman.templates.*
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

// Served at "/"
fun Route.homeRouter() {
    route("/") {
        get {
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