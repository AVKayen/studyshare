package com.physman.authentication

import com.physman.authentication.user.UserSession
import com.physman.utils.smartRedirect
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.sessions.*

val validateUserSession: suspend ApplicationCall.(UserSession) -> UserSession? = { session ->
    if (session.name.isNotEmpty()) session else null
}

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
            validate {
                validateUserSession
            }
            challenge {
                val redirectAfterLoginUrl = call.request.path()
                val redirectUrl = "/auth/login?redirectUrl=$redirectAfterLoginUrl"
                call.smartRedirect(redirectUrl)
            }
        }
    }

}