package com.physman.task

import com.physman.attachment.AttachmentView

data class TaskView(
    val task: Task,
    val attachments: List<AttachmentView> = emptyList(),
)