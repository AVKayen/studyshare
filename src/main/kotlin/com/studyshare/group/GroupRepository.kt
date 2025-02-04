package com.studyshare.group

import com.studyshare.forms.UploadFileData
import org.bson.types.ObjectId

interface GroupRepository {
    suspend fun createGroup(group: Group, groupThumbnailFile: UploadFileData?): GroupView
    suspend fun deleteGroup(groupId: ObjectId)
    suspend fun addUser(groupId: ObjectId, userId: ObjectId)
    suspend fun isUserMember(groupId: ObjectId, userId: ObjectId): Boolean
    suspend fun isUserGroupLeader(groupId: ObjectId, userId: ObjectId): Boolean
    suspend fun deleteUser(groupId: ObjectId, userId: ObjectId)
    suspend fun getGroup(groupId: ObjectId): GroupView?
    suspend fun getGroups(groupIds: List<ObjectId>): List<GroupView>
    suspend fun addTaskCategory(groupId: ObjectId, taskCategory: String)
    suspend fun removeTaskCategory(groupId: ObjectId, taskCategory: String)
}