package com.studyshare.attachment

data class AttachmentView(
    val attachment: Attachment,
    val url: String,
    val thumbnailUrl: String?
)