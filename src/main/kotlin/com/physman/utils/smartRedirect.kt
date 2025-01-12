package com.physman.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

suspend fun RoutingCall.smartRedirect(redirectUrl: String) {
    if (this.request.headers["HX-Request"] != null) {
        this.response.headers.append("HX-Redirect", redirectUrl)
        this.response.status(HttpStatusCode.NoContent)
        return
    }
    this.respondRedirect(redirectUrl)
}

suspend fun ApplicationCall.smartRedirect(redirectUrl: String) {
    if (this.request.headers["HX-Request"] != null) {
        this.response.headers.append("HX-Redirect", redirectUrl)
        this.response.status(HttpStatusCode.NoContent)
        return
    }
    this.respondRedirect(redirectUrl)
}
