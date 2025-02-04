package com.studyshare.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.smartRedirect(redirectUrl: String) {
    if (this.response.isCommitted) return
    if (this.request.headers["HX-Request"] != null) {
        this.response.headers.append("HX-Redirect", redirectUrl)
        this.response.status(HttpStatusCode.NoContent)
        return
    }
    this.respondRedirect(redirectUrl)
}