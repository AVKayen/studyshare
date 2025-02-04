package com.studyshare.solution

import com.studyshare.attachment.AttachmentView

data class SolutionView(
    val solution: Solution,
    val attachments: List<AttachmentView>,
    val isUpvoted: Boolean,
    val isDownvoted: Boolean
)