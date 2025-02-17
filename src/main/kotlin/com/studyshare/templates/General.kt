package com.studyshare.templates

import com.studyshare.attachment.AttachmentView
import kotlinx.html.*
import org.bson.types.ObjectId

fun FlowContent.nonImageAttachmentTemplate(nonImageAttachments: List<AttachmentView>) {
    if (nonImageAttachments.isNotEmpty()) {
        section {
            classes = setOf("attachments")
            nonImageAttachments.forEach { attachmentView: AttachmentView ->
                a(href = attachmentView.url) {
                    +attachmentView.attachment.originalFilename
                }
            }
        }
    }
}

fun FlowContent.galleryTemplate(galleryId: String, images: List<AttachmentView>) {
    if (images.isNotEmpty()) {
        section {
            classes = setOf("gallery")
            images.forEach { attachmentView: AttachmentView ->
                a(href = attachmentView.url) {
                    attributes["data-fancybox"] = galleryId
                    img(src = attachmentView.thumbnailUrl, alt = attachmentView.attachment.originalFilename)
                }
            }
        }
    }
}

fun FlowContent.fancyboxSetupScript() {
    script {
        unsafe {
            +"""
                Fancybox.bind("[data-fancybox]", {
                    Toolbar: {
                        display: {
                            left: ["infobar"],
                            middle: [
                                "zoomIn",
                                "zoomOut",
                                "rotateCCW",
                            ],
                            right: ["close"],
                        },
                    },
                });
            """.trimIndent()
        }
    }
}

fun FlowContent.loadingIndicator() {
    div(classes = "loading-indicator") {
        attributes["aria-busy"] = "true"
    }
}

fun FlowContent.contentLoadTemplate(url: String) {
    div(classes = "content-load") {
        attributes["hx-get"] = url
        attributes["hx-trigger"] = "load"
        attributes["hx-swap"] = "outerHTML"

        loadingIndicator()
    }
}

enum class ButtonType {
    EDIT,
    DELETE
}

fun FlowContent.iconButton(type: ButtonType, getUrl: String) {
    val script = """
        on click
            toggle @disabled on me
            me.setAttribute("aria-busy", true)
            toggle the *display of the first <span/> in me
        end
        on htmx:afterRequest
            log event
            toggle @disabled on me
            me.removeAttribute("aria-busy")
            toggle the *display of the first <span/> in me
        end
    """.trimIndent()

    button(classes = "icon-btn btn secondary outline") {
        attributes["hx-get"] = getUrl
        attributes["hx-target"] = "body"
        attributes["hx-swap"] = "beforeend"

        attributes["_"] = script

        span(classes = "material-symbols-rounded") {
            +when (type) {
                ButtonType.EDIT -> "edit"
                else -> "delete"
            }
        }
    }
}

fun FlowContent.deletionButton(getUrl: String) {
    iconButton(ButtonType.DELETE, getUrl)
}

fun FlowContent.editButton(getUrl: String) {
    iconButton(ButtonType.EDIT, getUrl)
}

fun FlowContent.localDateSpan(objectId: ObjectId) {
    val script = """
        on load 1
            put convertUTCDateToLocalDate(me.dataset.timestamp) into me
        end
    """.trimIndent()
    span {
        attributes["_"] = script
        attributes["data-timestamp"] = objectId.timestamp.toString()
    }
}

fun FlowContent.wideButton(buttonText: String, url: String, additionalButtonAttributes: Map<String, String>? = null) {
    button(classes = "btn primary outline wide-button") {
        attributes["hx-get"] = url
        if (additionalButtonAttributes != null) {
            attributes.putAll(additionalButtonAttributes)
        }

        span(classes = "htmx-indicator") {
            attributes["aria-busy"] = "true"
        }

        +buttonText
    }
}