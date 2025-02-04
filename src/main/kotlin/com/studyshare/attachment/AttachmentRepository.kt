package com.studyshare.attachment

import com.studyshare.forms.UploadFileData
import org.bson.types.ObjectId

interface AttachmentRepository {
    suspend fun createAttachments(files: List<UploadFileData>): List<AttachmentView>
    suspend fun createAttachment(file: UploadFileData): AttachmentView
    suspend fun deleteAttachments(attachmentIds: List<ObjectId>)
    suspend fun deleteAttachment(attachmentId: ObjectId)
    suspend fun getAttachments(attachmentIds: List<ObjectId>): List<AttachmentView>
    suspend fun getAttachment(attachmentId: ObjectId): AttachmentView?
}