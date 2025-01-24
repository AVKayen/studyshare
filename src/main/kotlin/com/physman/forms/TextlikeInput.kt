package com.physman.forms

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TextlikeInput(
    private val inputLabel: String,
    override val inputName: String,
    private val type : InputType,
    val validate : ((String) -> String?)?,
    private val validationDelay: Int = 400,
    private val inputDescription: String? = null,
    private val validateOnInput: Boolean = true,
    private val clearAfterSubmit: Boolean = false,
) : ControlledInput {

    private val errorTagId = "$inputName-error"

    init {
        if (inputName != URLEncoder.encode(inputName, StandardCharsets.UTF_8.toString())) {
            throw IllegalArgumentException("Invalid inputName. $inputName is not url-safe.")
        }
    }

    fun render(
        flowContent: FlowContent,
        validationUrl: String
    ) {
        val inputScript = """
            on input
                me.removeAttribute("aria-invalid")
                set #$errorTagId's innerHTML to ""
            end
                
            on htmx:afterRequest
                if event.srcElement is me
                    if event.detail.successful
                        me.setAttribute("aria-invalid", false)
                        set #$errorTagId's innerHTML to ""
                    else
                        me.setAttribute("aria-invalid", true)
                    end
                end
            end
            
            on clearInput
                set { value: "" } on me
            end
        """.trimIndent()

        flowContent.div {
            label {
                attributes["for"] = inputName
                +inputLabel
            }

            input(type = type, name = inputName) {
                attributes["id"] = inputName
                attributes["_"] = inputScript

                if (clearAfterSubmit) {
                    classes = setOf("clear-after-submit")
                }

                if (validateOnInput) {
                    attributes["hx-post"] = "${validationUrl}/${inputName}"

                    attributes["hx-trigger"] = "keyup changed delay:${validationDelay}ms"
                    attributes["hx-sync"] = "closest form:abort"

                }
                small {
                    attributes["id"] = errorTagId
                }
                if (inputDescription != null) {
                    small {
                        classes = setOf("input-info")
                        +inputDescription
                    }
                }
            }
        }
    }

    suspend fun respondInputError(call: RoutingCall, error: String) {
        call.respondHtml(status = HttpStatusCode.UnprocessableEntity) {
            body {
                small {
                    attributes["id"] = errorTagId
                    attributes["hx-swap-oob"] = "true"
                    attributes["_"] = """
                        on load
                            me.previousElementSibling.setAttribute("aria-invalid", true)
                    """.trimIndent()

                    +error
                }
            }
        }
    }
}