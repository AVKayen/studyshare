package com.studyshare.forms

import com.studyshare.attachment.AttachmentView
import kotlinx.html.FlowContent
import kotlinx.html.*
import org.bson.types.ObjectId

class FileDeletionInput(
    val inputName: String,
    private val inputId: String,
) : ControlledInput {

    private fun FlowContent.deletionButton(attachmentId: ObjectId, deletedCountId: String) {
        button(classes = "file-deletion-btn btn secondary danger", type = ButtonType.button) {
            attributes["_"] = """
                on click
                    set input to the next <input/>
                    call appendValue(input, "$attachmentId")
                    remove closest parent <div/>
                    set deletedCount to #$deletedCountId
                    call incContent(deletedCount)
            """.trimIndent()

            span(classes = "material-symbols-rounded") {
                +"delete"
            }
        }
    }

    private fun FlowContent.nonImageFileToBeDeleted(attachmentView: AttachmentView, deletedCountId: String) {
        div(classes = "non-img-to-be-deleted") {
            span {
                +attachmentView.attachment.originalFilename
            }
            deletionButton(attachmentView.attachment.id, deletedCountId)
        }
    }

    private fun FlowContent.imageFileToBeDeleted(attachmentView: AttachmentView, inputName: String, deletedCountId: String) {
        div(classes = "img-to-be-deleted") {
            a(href = attachmentView.url) {
                attributes["data-fancybox"] = inputName
                img(
                    src = attachmentView.thumbnailUrl,
                    alt = attachmentView.attachment.originalFilename
                )
            }
            deletionButton(attachmentView.attachment.id, deletedCountId)
        }

    }

    fun render(flowContent: FlowContent, filesToBeDeleted: List<AttachmentView>?) {
        if (filesToBeDeleted.isNullOrEmpty()) {
            return
        }

        val imageAttachments = filesToBeDeleted.filter { it.attachment.isImage }
        val nonImageAttachments = filesToBeDeleted.filter { !it.attachment.isImage}

        val deletedCountId = "$inputId-deleted-count"

        flowContent.div {
            section(classes = "gallery") {
                imageAttachments.forEach {
                    imageFileToBeDeleted(it, inputName, deletedCountId)
                }
            }

            section {
                nonImageAttachments.forEach {
                    nonImageFileToBeDeleted(it, deletedCountId)
                }
            }

            p(classes = "deleted-count-text") {
                +"Deleted "
                span {
                    attributes["id"] = deletedCountId
                    +"0"
                }
                +" attachment(s)"
            }

            input(type = InputType.hidden) {
                attributes["name"] = inputName
                attributes["id"]
            }
       }
    }
}