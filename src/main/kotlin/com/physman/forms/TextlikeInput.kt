package com.physman.forms

import kotlinx.html.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TextlikeInput(
    private val inputLabel: String,
    override val inputName: String,
    private val type : InputType,
    private val validate : ((String) -> String?)?
) : ControlledInput {

    init {
        if (inputName != URLEncoder.encode(inputName, StandardCharsets.UTF_8.toString())) {
            throw IllegalArgumentException("Invalid inputName. $inputName is not url-safe.")
        }
    }

    fun render(flowContent: FlowContent, inputtedString: String? = null, validationUrl: String) {
        val error: String? = if(inputtedString != null) this.validate?.invoke(inputtedString) else null
        flowContent.div {

            attributes["hx-target"] = "this"
            attributes["hx-swap"] = "outerHTML"
            label {
                attributes["for"] = inputName
                +inputLabel
            }
            input(type = type, name = inputName) {
                attributes["id"] = inputName

                if (error != null) {
                    attributes["aria-invalid"] = "true"
                }

                attributes["hx-post"] = "${validationUrl}/${inputName}"
                attributes["hx-trigger"] = "keyup changed delay:500ms"
                attributes["hx-sync"] = "closest form:abort"

                if (inputtedString != null) {
                    value = inputtedString
                }
            }
            if (error != null) {
                small {
                    +error
                }
            }
        }
    }
}