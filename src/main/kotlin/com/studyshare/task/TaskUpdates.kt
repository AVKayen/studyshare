package com.studyshare.task

import com.studyshare.forms.UploadFileData
import org.bson.types.ObjectId

class TaskUpdates(
    val title: String? = null,
    val additionalNotes: String? = null,
    val filesToDelete: List<ObjectId> = emptyList(),
    val newFiles: List<UploadFileData> = emptyList()
)