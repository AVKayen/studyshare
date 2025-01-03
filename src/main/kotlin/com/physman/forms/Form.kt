package com.physman.forms

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.html.*
import java.net.URLEncoder
import java.nio.channels.WritableByteChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.io.path.*
import java.nio.file.StandardOpenOption.*


// TODO: This values should be read from some config, also the upload sizes must be changed in some Ktor config
// (default max upload file size in Ktor is 50MB)
const val MAX_UPLOAD_SIZE: Long = 250 // MB
const val MAX_UPLOAD_SIZE_BYTES: Long = MAX_UPLOAD_SIZE * 1024 * 1024

const val MAX_FILE_SIZE: Long = 48 // MB
const val MAX_FILE_SIZE_BYTES: Long = MAX_FILE_SIZE * 1024 * 1024


data class UploadFileData(
    val filePath: java.nio.file.Path,
    val originalName: String
)

data class FormSubmissionData(
    val fields: Map<String, String>,
    val files: List<UploadFileData>?
)

class Form(
    private val formTitle: String,
    val formName: String,
    private val formAttributes: Map<String, String>? = null
) {
    var validatorsRoute: String? = null
    var inputs : List<ControlledInput> = emptyList()
    private var isMultipart = false

    init {
        if (formName != URLEncoder.encode(formName, StandardCharsets.UTF_8.toString())) {
            throw IllegalArgumentException("Invalid formName. $formName is not url-safe.")
        }
    }

    fun addInput(input: ControlledInput) {
        if (input is FileInput) {
            isMultipart = true
        }
        inputs = inputs.plus(input)
    }

    fun render(flowContent: FlowContent, callbackUrl: String) {
        flowContent.form {
            attributes["hx-post"] = callbackUrl

            if (isMultipart) {
                attributes["hx-encoding"] = "multipart/form-data"
            }

            if(this@Form.formAttributes != null) {
                attributes.putAll(formAttributes)
            }

            h1 { + this@Form.formTitle }

            for (input in this@Form.inputs) {
                if (input is TextlikeInput) {
                    if (validatorsRoute != null) {
                        input.render(flowContent, validationUrl = this@Form.validatorsRoute!!)
                    } else {
                        // TODO: What error type to throw??
                        throw UninitializedPropertyAccessException("Form ${this@Form.formName} is not routed")
                    }
                }
                if (input is FileInput) {
                    input.render(flowContent)
                }
            }

            div {
                attributes["id"] = "${formName}Error"
            }
            button {
                +"Submit"
            }
        }
    }

    // TODO: Add some styling to the error (or even entire form)
    private suspend fun respondFormError(call: RoutingCall, error: String) {
        call.respondHtml {
            body {
                div {
                    attributes["id"] = "${formName}Error"
                    attributes["hx-swap-oob"] = "true"

                    +error
                }
            }
        }
    }

    private suspend fun validateFields(call: RoutingCall, fields: Map<String, List<String>>): Map<String, String>? {
        val validatedFields = mutableMapOf<String, String>()

        for (input in inputs) {
            if (input !is TextlikeInput) {
                continue
            }
            val inputValue: String = fields[input.inputName]?.first() ?: ""
            val error = input.validate?.invoke(inputValue)
            if (error != null) {
                call.respondHtml {
                    body {
                        input.render(this, inputValue, validatorsRoute!!)
                    }
                }
                return null
            }
            validatedFields[input.inputName] = inputValue
        }
        return validatedFields
    }

    
    private suspend fun validateFormParameters(call: RoutingCall): Map<String, String>? {
        val formParameters = call.receiveParameters()
        val unvalidatedFields: Map<String, List<String>> = formParameters.entries().associate { it.key to it.value }
        return validateFields(call, unvalidatedFields)
    }

    private suspend fun validateMultipartData(call: RoutingCall): FormSubmissionData? {
        val multipartData = call.receiveMultipart()
        val unvalidatedFields = mutableMapOf<String, List<String>>()
        val files = mutableListOf<UploadFileData>()
        var totalUploadSize: Long = 0
        var formError: String? = null

        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    if (unvalidatedFields[part.name!!] != null) {
                        unvalidatedFields[part.name!!] = unvalidatedFields[part.name!!]!! + part.value
                    } else {
                        unvalidatedFields[part.name!!] = listOf(part.value)
                    }
                }

                is PartData.FileItem -> {

                    val tempFilePath = createTempFile()
                    val byteChannel: WritableByteChannel = Files.newByteChannel(tempFilePath, WRITE)

                    val fileSize = part.provider().copyTo(channel = byteChannel, limit = MAX_FILE_SIZE_BYTES)
                    byteChannel.close()

                    if (fileSize >= MAX_FILE_SIZE_BYTES) {
                        formError = "File(s) too large. Max file size is ${MAX_FILE_SIZE}MB."
                    }

                    totalUploadSize += fileSize
                    if (totalUploadSize > MAX_UPLOAD_SIZE_BYTES) {
                        formError = "Upload too large. Max size is ${MAX_UPLOAD_SIZE}MB."
                    }

                    files.add(UploadFileData(
                        filePath = tempFilePath,
                        originalName = part.originalFileName ?: "unknown"
                    ))
                }

                else -> {}
            }
            part.dispose()

            if (formError != null) {
                return@forEachPart
            }
        }

        formError?.let {
            files.forEach { uploadFileData ->
                uploadFileData.filePath.deleteIfExists()
            }
            respondFormError(call, it)
            return null
        }

        val validatedFields = validateFields(call, unvalidatedFields) ?: return null

        return FormSubmissionData(
            fields = validatedFields,
            files = files
        )
    }

    suspend fun validateSubmission(call: RoutingCall): FormSubmissionData? {

        val contentLength = call.request.header(HttpHeaders.ContentLength)?.toIntOrNull()
        if (contentLength == null || contentLength > MAX_UPLOAD_SIZE_BYTES) {
            respondFormError(call, "Upload too large. Max size is ${MAX_UPLOAD_SIZE}MB.")
            return null
        }

        if (!isMultipart) {
            val fields = validateFormParameters(call) ?: return null
            return FormSubmissionData(
                fields = fields,
                files = null
            )
        }
        return validateMultipartData(call)
    }
}
