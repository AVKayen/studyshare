package com.studyshare.group

import com.studyshare.attachment.AttachmentView

data class GroupView(
    val group: Group,
    val thumbnail: AttachmentView?
)