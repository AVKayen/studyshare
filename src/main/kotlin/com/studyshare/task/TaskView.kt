package com.studyshare.task

import com.studyshare.attachment.AttachmentView

data class TaskView(
    val task: Task,
    val attachments: List<AttachmentView> = emptyList(),
)