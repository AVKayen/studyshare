package com.physman.authentication

import com.physman.authentication.user.UserRepository
import com.physman.authentication.user.UserSession
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

fun Application.configureSecurity(userRepository: UserRepository) {

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
                if (session.name != "")
                    session
                else
                    null
            }
            challenge {
                call.respondRedirect("/auth/login")
            }
        }
    }

}