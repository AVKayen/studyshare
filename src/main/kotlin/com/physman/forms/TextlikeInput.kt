package com.physman.forms

import kotlinx.html.*

class TextlikeInput(
    private val parameterName: String,
    private val type : InputType,
    private val validate : ((String) -> String?)?
) : ControlledInput {

    override val routePath = parameterName.lowercase().replace(" ", "_")

    fun render(flowContent: FlowContent, inputtedString: String? = null, url: String) {
        val error: String? = if(inputtedString != null) this.validate?.invoke(inputtedString) else null
        flowContent.div {

            attributes["hx-target"] = "this"
            attributes["hx-swap"] = "outerHTML"
            label {
                attributes["for"] = routePath
                +parameterName
            }
            input(type = type, name = routePath) {
                attributes["id"] = routePath

                if (error != null) {
                    attributes["aria-invalid"] = "true"
                }

                attributes["hx-post"] = "${url}/${routePath}"
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