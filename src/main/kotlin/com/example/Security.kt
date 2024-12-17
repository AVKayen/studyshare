package com.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

fun Application.configureSecurity() {
    install(Sessions) {
        // Sessions are stored in the server's in-memory database
        cookie<UserSession>("SESSION", SessionStorageMemory()) {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    install(Authentication) {
        // Validate authentication, redirect to /login on fail
        session<UserSession>("USER") {
            validate { session ->
                if (session.username != "")
                    session
                else
                    null
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }

    // Security-related routing
    routing {
        get("/login") {
            val session = call.sessions.get<UserSession>() ?: UserSession("Whoa")
            call.sessions.set(session)
            call.respondRedirect("/")
        }
    }
}

@Serializable
data class UserSession(val username: String)
