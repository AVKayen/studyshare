package com.physman.authentication

import com.physman.authentication.user.UserSession
import com.physman.utils.smartRedirect
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.sessions.*
import java.security.SecureRandom

val validateUserSession: suspend ApplicationCall.(UserSession) -> UserSession? = { session ->
    if (session.name.isNotEmpty()) session else null
}

fun Application.configureSecurity() {
    install(Sessions) {
        val sessionEncryptKey: ByteArray = SecureRandom().generateSeed(16)
        val sessionSignKey: ByteArray = SecureRandom().generateSeed(16)
        cookie<UserSession>("SESSION", SessionStorageMemory()) {
            cookie.extensions["SameSite"] = "lax"
            cookie.httpOnly = true
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60 * 60 * 24 * 7 // a week
            transform(SessionTransportTransformerEncrypt(sessionEncryptKey, sessionSignKey))
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