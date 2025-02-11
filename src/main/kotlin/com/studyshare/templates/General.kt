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

fun FlowContent.deletionButton(getUrl: String) {
    button(classes = "btn secondary outline") {
        attributes["hx-get"] = getUrl
        attributes["hx-target"] = "body"
        attributes["hx-swap"] = "beforeend"

        span(classes = "material-symbols-rounded") {
            +"delete"
        }
    }
}

fun FlowContent.editButton(getUrl: String) {
    button(classes = "btn secondary outline") {
        attributes["hx-get"] = getUrl
        attributes["hx-target"] = "body"
        attributes["hx-swap"] = "beforeend"

        span(classes = "material-symbols-rounded") {
            +"edit"
        }
    }
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

        +buttonText
    }
}