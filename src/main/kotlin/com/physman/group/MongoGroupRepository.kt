package com.physman.group

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.physman.attachment.AttachmentRepository
import com.physman.authentication.user.UserRepository
import com.physman.forms.UploadFileData
import com.physman.task.TaskRepository
import kotlinx.coroutines.flow.firstOrNull
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
        groupCollection.updateOne(filter, updates)
    }

    override suspend fun isUserMember(groupId: ObjectId, userId: ObjectId): Boolean {
        val filters = Filters.eq("_id", groupId)
        val group = groupCollection.find(filters).firstOrNull() ?: return false
        return group.memberIds.contains(userId)
    }

    override suspend fun getGroup(groupId: ObjectId): GroupView? {
        val group = groupCollection.find(Filters.eq("_id", groupId)).firstOrNull() ?: return null
        return GroupView(
            group = group,
            thumbnail = group.thumbnailId?.let { attachmentRepository.getAttachment(it) }
        )
    }
}