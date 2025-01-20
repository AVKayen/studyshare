package com.physman.attachment

data class AttachmentView(
    val attachment: Attachment,
    val url: String,
    val thumbnailUrl: String?
)