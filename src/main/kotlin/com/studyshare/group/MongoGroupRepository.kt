package com.studyshare.group

import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.studyshare.attachment.AttachmentRepository
import com.studyshare.authentication.user.UserRepository
import com.studyshare.forms.UploadFileData
import com.studyshare.task.TaskRepository
import com.studyshare.utils.ResourceModificationRestrictedException
import com.studyshare.utils.ResourceNotFoundException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class MongoGroupRepository(
    database: MongoDatabase,
    private val taskRepository: TaskRepository,
    private val attachmentRepository: AttachmentRepository,
    private val userRepository: UserRepository
) : GroupRepository {

    private val groupCollection = database.getCollection<Group>("groups")

    private suspend fun getGroup(groupId: ObjectId): Group {
        val filter = Filters.eq("_id", groupId)
        return groupCollection.find(filter).firstOrNull() ?: throw ResourceNotFoundException()
    }

    private suspend fun createGroupView(group: Group): GroupView = GroupView(
            group = group,
            thumbnail = group.thumbnailId?.let { attachmentRepository.getAttachment(it) }
        )

    override suspend fun createGroup(group: Group, groupThumbnailFile: UploadFileData?): GroupView {
        userRepository.addGroupToUser(group.leaderId, group.id)
        if (groupThumbnailFile == null) {
            groupCollection.insertOne(group)
            return GroupView(
                group = group,
                thumbnail = null
            )
        }
        val groupThumbnailAttachment = attachmentRepository.createAttachment(groupThumbnailFile)
        val groupWithThumbnail = group.copy( thumbnailId = groupThumbnailAttachment.attachment.id )
        groupCollection.insertOne(groupWithThumbnail)
        return GroupView(
            group = groupWithThumbnail,
            thumbnail = groupWithThumbnail.thumbnailId?.let { attachmentRepository.getAttachment(it) }
        )
    }

    override suspend fun addUser(groupId: ObjectId, userId: ObjectId) {
        val filter = Filters.eq("_id", groupId)
        val updates = Updates.addToSet(Group::memberIds.name, userId)
        userRepository.addGroupToUser(userId, groupId)
        groupCollection.updateOne(filter, updates)
    }

    override suspend fun deleteGroup(groupId: ObjectId, userId: ObjectId) {
        val group = groupCollection.findOneAndDelete(Filters.eq("_id", groupId)) ?: throw ResourceNotFoundException()
        if (group.leaderId != userId) {
            throw ResourceModificationRestrictedException()
        }
        taskRepository.deleteTasks(group.id)
    }

    override suspend fun deleteUser(groupId: ObjectId, userId: ObjectId, targetUserId: ObjectId) {
        val group = getGroup(groupId)

        if (!group.canUserKick(userId)) {
            throw ResourceModificationRestrictedException()
        }

        val filter = Filters.eq("_id", groupId)
        val updates = Updates.pull(Group::memberIds.name, targetUserId)
        userRepository.removeGroupFromUser(targetUserId, groupId)
        groupCollection.updateOne(filter, updates)
    }

    override suspend fun isUserMember(groupId: ObjectId, userId: ObjectId): Boolean {
        val filter = Filters.eq("_id", groupId)
        val group = groupCollection.find(filter).firstOrNull() ?: return false
        return group.memberIds.contains(userId)
    }

    override suspend fun isUserGroupLeader(groupId: ObjectId, userId: ObjectId): Boolean {
        val filter = Filters.eq("_id", groupId)
        val group = groupCollection.find(filter).firstOrNull() ?: return false
        return group.leaderId == userId
    }

    override suspend fun getGroupView(groupId: ObjectId): GroupView {
        val group = getGroup(groupId)
        return createGroupView(group)
    }

    override suspend fun getGroupViews(groupIds: List<ObjectId>): List<GroupView> {
        val filter = Filters.`in`("_id", groupIds)
        val sort = Sorts.descending("_id")
        return groupCollection.find(filter).sort(sort).toList().map { group: Group ->
            GroupView(
                group = group,
                thumbnail = group.thumbnailId?.let { attachmentRepository.getAttachment(it) }
            )
        }
    }

    override suspend fun addTaskCategory(groupId: ObjectId, taskCategory: String) {
        val filter = Filters.eq("_id", groupId)
        val updates = Updates.addToSet(Group::taskCategories.name, taskCategory)
        groupCollection.updateOne(filter, updates)
    }

    override suspend fun removeTaskCategory(groupId: ObjectId, taskCategory: String) {
        val filter = Filters.eq("_id", groupId)
        val updates = Updates.pull(Group::taskCategories.name, taskCategory)
        groupCollection.updateOne(filter, updates)
    }

    override suspend fun editGroup(groupId: ObjectId, userId: ObjectId, groupUpdates: GroupUpdates): GroupView {
        val group = getGroup(groupId)

        if (group.leaderId != userId) {
            throw ResourceModificationRestrictedException()
        }

        val newThumbnailAttachment = groupUpdates.newThumbnail?.let {
            attachmentRepository.createAttachment(it).attachment
        }

        val filter = Filters.eq("_id", groupId)
        val updates = Updates.combine(
            listOfNotNull(
                Updates.set(Group::title.name, groupUpdates.title),
                Updates.set(Group::description.name, groupUpdates.description),
                newThumbnailAttachment?.let {
                    Updates.set(Group::thumbnailId.name, it.id)
                }
            )
        )

        val options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        val updatedGroup = groupCollection.findOneAndUpdate(filter, updates, options) ?: throw ResourceNotFoundException()
        return createGroupView(updatedGroup)
    }
}