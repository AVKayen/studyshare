package com.physman.group

import com.physman.forms.UploadFileData
import org.bson.types.ObjectId

interface GroupRepository {
    suspend fun createGroup(group: Group, groupThumbnailFile: UploadFileData?): GroupView
    suspend fun deleteGroup(groupId: ObjectId)
    suspend fun addUser(groupId: ObjectId, userId: ObjectId)
    suspend fun isUserMember(groupId: ObjectId, userId: ObjectId): Boolean
    suspend fun deleteUser(groupId: ObjectId, userId: ObjectId)
    suspend fun getGroup(groupId: ObjectId): GroupView?
}