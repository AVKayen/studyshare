package com.physman.routes

import com.physman.authentication.user.*
import com.physman.forms.Form
import com.physman.forms.TextlikeInput
import com.physman.forms.globalFormRouter
import com.physman.templates.index
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div


fun Route.authRouter(userRepository: UserRepository) {
    val loginForm = Form("Login", "loginForm", mapOf(
        "hx-swap" to "none"
    ))
switch    loginForm.addInput(TextlikeInput("Username", "name", InputType.text, usernameValidatorOnLogin))
    loginForm.addInput(TextlikeInput("Password", "password", InputType.password, passwordValidatorOnLogin))

    val registerForm = Form("Register", "registerForm", mapOf(
        "hx-swap" to "none"
    ))
    registerForm.addInput(TextlikeInput("Username", "name", InputType.text, usernameValidatorOnRegister))
    registerForm.addInput(TextlikeInput("Password", "password", InputType.password, passwordValidatorOnRegister))

    globalFormRouter.routeFormValidators(loginForm)
    globalFormRouter.routeFormValidators(registerForm)

    route("/login") {
        get {
            call.respondHtml {
                index("Login") {
                    loginForm.render(this, "/auth/login")
                    div {
                        a("/auth/register") {
                            +"Don't have an account? Register here"
                        }
                    }
                }
            }
        }
        post {
            val formSubmissionData = loginForm.validateSubmission(call) ?: return@post
            val username = formSubmissionData.fields["name"]!!
            val password = formSubmissionData.fields["password"]!!
            val session: UserSession? = userRepository.login(username, password)
            if (session == null) {
                loginForm.respondFormError(call, "Invalid username or password")
                return@post
            }
            call.sessions.set(session)
            call.response.headers.append("HX-Redirect", "/")
            call.respondText("Redirecting to home...")
        }
    }

    route ("/register") {
        get {
            call.respondHtml {
                index("Register") {
                    registerForm.render(this, "/auth/register")
                    div {
                        a("/auth/register") {
                            +"Don't have an account? Register here"
                        }
                    }
                }
            }
        }
        post {
            val formSubmissionData = registerForm.validateSubmission(call) ?: return@post
            val username = formSubmissionData.fields["name"]!!
            val password = formSubmissionData.fields["password"]!!
            val session: UserSession
            try {
                session = userRepository.register(username, password)
            } catch (e: Exception) {
                registerForm.respondFormError(call, e.toString())
                return@post
            }
            call.sessions.set(session)
            call.response.headers.append("HX-Redirect", "/")
            call.respondRedirect("/")
        }
    }

    route ("/logout") {
        get {
            call.sessions.clear<UserSession>()
            call.response.headers.append("HX-Redirect", "/")
            call.respondRedirect("/")
        }
    }

}