package com.physman.attachment

import com.physman.forms.UploadFileData
import org.bson.types.ObjectId

interface AttachmentRepository {
    suspend fun createAttachments(files: List<UploadFileData>): List<Attachment>
    suspend fun deleteAttachments(attachmentIds: List<ObjectId>)
    suspend fun getAttachments(attachmentIds: List<ObjectId>): List<AttachmentView>
}