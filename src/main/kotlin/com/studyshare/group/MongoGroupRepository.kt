package com.studyshare.group

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.studyshare.attachment.AttachmentRepository
import com.studyshare.authentication.user.UserRepository
import com.studyshare.forms.UploadFileData
import com.studyshare.task.TaskRepository
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

    override suspend fun deleteGroup(groupId: ObjectId) {
        val group = groupCollection.findOneAndDelete(Filters.eq("_id", groupId)) ?: return
        taskRepository.deleteTasks(group.id)
    }

    override suspend fun deleteUser(groupId: ObjectId, userId: ObjectId) {
        val filter = Filters.eq("_id", groupId)
        val updates = Updates.pull(Group::memberIds.name, userId)
        userRepository.removeGroupFromUser(userId, groupId)
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
        val group = groupCollection.find(Filters.eq("_id", groupId)).firstOrNull() ?: throw ResourceNotFoundException()
        return GroupView(
            group = group,
            thumbnail = group.thumbnailId?.let { attachmentRepository.getAttachment(it) }
        )
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
        val filters = Filters.eq("_id", groupId)
        val updates = Updates.addToSet(Group::taskCategories.name, taskCategory)
        groupCollection.updateOne(filters, updates)
    }

    override suspend fun removeTaskCategory(groupId: ObjectId, taskCategory: String) {
        val filters = Filters.eq("_id", groupId)
        val updates = Updates.pull(Group::taskCategories.name, taskCategory)
        groupCollection.updateOne(filters, updates)
    }
}