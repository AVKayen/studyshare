package com.physman.group

import com.physman.attachment.AttachmentView

data class GroupView(
    val group: Group,
    val thumbnail: AttachmentView?
)