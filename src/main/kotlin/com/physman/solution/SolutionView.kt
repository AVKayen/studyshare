package com.physman.solution

import com.physman.attachment.AttachmentView

data class SolutionView(
    val solution: Solution,
    val attachments: List<AttachmentView>
)