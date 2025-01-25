package com.physman.forms

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TextlikeInput(
    private val inputLabel: String,
    val inputName: String,
    private val inputType : InputType = InputType.text,
    val validate : ((String) -> String?)?,
    private val validationDelay: Int = 400,
    private val inputDescription: String? = null,
    private val validateOnInput: Boolean = true,
    private val clearAfterSubmit: Boolean = false,
    private val confirmationInputLabel: String? = null,
    private val confirmationMissmatchError: String? = null,
) : ControlledInput {

    init {
        if (inputName != URLEncoder.encode(inputName, StandardCharsets.UTF_8.toString())) {
            throw IllegalArgumentException("Invalid inputName. $inputName is not url-safe.")
        }

        if ((confirmationInputLabel != null && confirmationMissmatchError == null) || (confirmationInputLabel == null && confirmationMissmatchError != null)) {
            throw IllegalArgumentException("Arguments confirmationInputLabel and confirmationMissmatchError must both have values or be null.")
        }
    }

    private val inputId = inputName
    private val errorTagId = "$inputName-error"
    private val confirmationInputId = "confirmation-$inputName"
    private val confirmationErrorTagId = "confirmation-$inputName-error"

    private fun renderConfirmationInput(flowContent: FlowContent) {
        flowContent.div {
            val confirmationInputScript = """
                def confirm()
                    if #$inputId's value == my value
                        if my value == ""
                            me.removeAttribute("aria-invalid")
                            set #$confirmationErrorTagId's innerHTML to ""
                        else
                            me.setAttribute("aria-invalid", false)
                            set #$confirmationErrorTagId's innerHTML to ""
                        end
                    else
                        me.setAttribute("aria-invalid", true)
                        set #$confirmationErrorTagId's innerHTML to "$confirmationMissmatchError"
                    end
                end
                on confirmInput
                    call confirm()
                end
                on change
                    call confirm()
                end
                on clearInput
                    set { value: "" } on me
                    me.removeAttribute("aria-invalid")
                end
            """.trimIndent()

            flowContent.div {
                label {
                    attributes["for"] = confirmationInputId
                    +confirmationInputLabel!!
                }
                input(type = inputType) {
                    attributes["id"] = confirmationInputId
                    attributes["_"] = confirmationInputScript
                    classes = setOf("confirmation-input")

                    if (clearAfterSubmit) {
                        classes = classes + "clear-after-submit"
                    }
                }
                small {
                    attributes["id"] = confirmationErrorTagId
                }
            }
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

        val confirmationInputScript = "on change send confirmInput to #$confirmationInputId"

        flowContent.div {
            label {
                attributes["for"] = inputId
                +inputLabel
            }

            input(type = inputType, name = inputName) {
                attributes["id"] = inputId
                attributes["_"] = if (confirmationInputLabel != null) "$inputScript $confirmationInputScript" else inputScript

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

                if (confirmationInputLabel != null) {
                    renderConfirmationInput(flowContent)
                }

                if (inputDescription != null) {
                    small(classes = "input-info") {
                        span(classes = "material-symbols-rounded") {
                            +"info"
                        }
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