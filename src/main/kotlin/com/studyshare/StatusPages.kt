package com.studyshare

import com.studyshare.templates.index
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.statuspages.*
import kotlinx.html.h1
import kotlinx.html.p
import org.slf4j.Logger

fun Application.configureStatusPages(logger: Logger) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Internal server error", cause)
            call.respondHtml(HttpStatusCode.InternalServerError) {
                index("Error") {
                    h1 {
                        +"A server error occurred: ${cause.localizedMessage}."
                    }
                    p {
                        +"It's not your fault, it's ours. Please try again later."
                    }
                }
            }
        }
        status(HttpStatusCode.NotFound) { call, _ -> // cause is redundant to know (it's always page not found)
            call.respondHtml(HttpStatusCode.NotFound) {
                index("Not Found") {
                    h1 {
                        +"Page not found"
                    }
                }
            }
        }
    }
}