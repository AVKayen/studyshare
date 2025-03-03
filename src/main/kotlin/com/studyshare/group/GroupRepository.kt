package com.studyshare.group

import com.studyshare.forms.UploadFileData
import org.bson.types.ObjectId

interface GroupRepository {
    suspend fun createGroup(group: Group, groupThumbnailFile: UploadFileData?): GroupView
    suspend fun deleteGroup(groupId: ObjectId, userId: ObjectId)
    suspend fun addUser(groupId: ObjectId, userId: ObjectId)
    suspend fun isUserMember(groupId: ObjectId, userId: ObjectId): Boolean
    suspend fun isUserGroupLeader(groupId: ObjectId, userId: ObjectId): Boolean
    suspend fun deleteUser(groupId: ObjectId, userId: ObjectId, targetUserId: ObjectId)
    suspend fun getGroupView(groupId: ObjectId): GroupView
    suspend fun getGroupViews(groupIds: List<ObjectId>): List<GroupView>
    suspend fun addTaskCategory(groupId: ObjectId, taskCategory: String)
    suspend fun removeTaskCategory(groupId: ObjectId, taskCategory: String)
    suspend fun editGroup(groupId: ObjectId, userId: ObjectId, groupUpdates: GroupUpdates): GroupView
}