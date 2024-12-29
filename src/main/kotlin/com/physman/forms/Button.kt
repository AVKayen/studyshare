package com.physman.forms

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.html.*
import kotlinx.io.readByteArray
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.io.path.*


class Button(
    private val text: String,
    private val formAttributes: Map<String, String>? = null
) {
//    var validatorsRoute: String? = null
//    var inputs: List<ControlledInput> = emptyList()
//
//
//    fun render(flowContent: FlowContent, callbackUrl: String) {
//        flowContent.button {
//            attributes["hx-patch"] = callbackUrl
//
//
//
//            if (this@Button.formAttributes != null) {
//                attributes.putAll(formAttributes)
//            }
//
//            h1 { +this@Button.text }
//
//
//
//            div {
//                attributes["id"] = "${text}Error"
//            }
//            button {
//                +"Submit"
//            }
//        }
//    }


    fun boberButton(flowContent: FlowContent, callbackUrl: String) {
        flowContent.button(type = ButtonType.button) {
            attributes["hx-patch"] = callbackUrl
            attributes["data-my-button"] = "true"
            +"Upvote" // Add button text

        }
    }


    // TODO: Add some styling to the error (or even entire form)
    private suspend fun respondFormError(call: RoutingCall, error: String) {
        call.respondHtml {
            body {
                div {
                    attributes["id"] = "${text}Error"
                    attributes["hx-swap-oob"] = "true"

                    +error
                }
            }
        }
    }


}

