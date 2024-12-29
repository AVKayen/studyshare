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


    fun boberButton(flowContent: FlowContent, callbackUrl: String) {
        flowContent.button(type = ButtonType.button) {
            attributes["hx-patch"] = callbackUrl
            attributes["hx-swap"] = "outerHtml"
            attributes["hx-target"] = "closest h2"

            +text // Add button text
        }
    }




}

