package com.studyshare.task

import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.studyshare.forms.UploadFileData
import com.studyshare.attachment.AttachmentRepository
import com.studyshare.comment.CommentRepository
import com.studyshare.solution.Solution
import com.studyshare.solution.SolutionRepository
import com.studyshare.utils.ResourceModificationRestrictedException
import com.studyshare.utils.ResourceNotFoundException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class MongoTaskRepository(
     mongoDatabase: MongoDatabase,
     private val attachmentRepository: AttachmentRepository,
     private val commentRepository: CommentRepository,
     private val solutionRepository: SolutionRepository
) : TaskRepository {
    private val taskCollection = mongoDatabase.getCollection<Task>("tasks")

    private suspend fun createTaskView(task: Task): TaskView = TaskView(
        task = task,
        attachments = attachmentRepository.getAttachments(task.attachmentIds)
    )

    override suspend fun createTask(task: Task, files: List<UploadFileData>): TaskView {

        val attachments = attachmentRepository.createAttachments(files)

        val taskWithAttachments = task.copy(
            attachmentIds = attachments.map { it.attachment.id }
        )

        taskCollection.insertOne(taskWithAttachments)

        return TaskView(
            task = taskWithAttachments,
            attachments = attachments
        )
    }

    override suspend fun getTasks(groupId: ObjectId, category: String?, resultCount: Int, lastId: ObjectId?): List<Task> {
        val filter = Filters.and(
            listOfNotNull(
                Filters.eq(Task::groupId.name, groupId),
                category?.let { Filters.eq(Task::category.name, it) },
                lastId?.let { Filters.lt("_id", it) }
            )
        )

        val sort = Sorts.descending("_id")
        return taskCollection.find(filter).sort(sort).limit(resultCount).toList()
    }

    override suspend fun getTask(id: ObjectId): Task {
        val filter = Filters.eq("_id", id)
        return taskCollection.find(filter).firstOrNull() ?: throw ResourceNotFoundException()
    }

    override suspend fun getTaskView(id: ObjectId): TaskView {
        val task = getTask(id)
        return createTaskView(task)
    }

    override suspend fun updateTask(id: ObjectId, userId: ObjectId, taskUpdates: TaskUpdates): TaskView {

        attachmentRepository.deleteAttachments(taskUpdates.filesToDelete)
        val newAttachments = attachmentRepository.createAttachments(taskUpdates.newFiles)

        val filter = Filters.eq("_id", id)
        val task = getTask(id)

        if (task.authorId != userId) {
            throw ResourceModificationRestrictedException()
        }

        val updatedAttachments = task.attachmentIds + newAttachments.map { it.attachment.id } - taskUpdates.filesToDelete.toSet()

        val updates = Updates.combine(
            Updates.set(Solution::title.name, taskUpdates.title),
            Updates.set(Solution::additionalNotes.name, taskUpdates.additionalNotes),
            Updates.set(Solution::attachmentIds.name, updatedAttachments)
        )

        val options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        val updatedTask = taskCollection.findOneAndUpdate(filter, updates, options) ?: throw ResourceNotFoundException()

        return createTaskView(updatedTask)
    }

    override suspend fun deleteTask(id: ObjectId, userId: ObjectId): Task {
        val task = getTask(id)

        if (task.authorId != userId) {
            throw ResourceModificationRestrictedException()
        }

        val filter = Filters.eq("_id", id)

        taskCollection.deleteOne(filter)
        commentRepository.deleteComments(id)
        solutionRepository.deleteSolutions(taskId = task.id)
        attachmentRepository.deleteAttachments(task.attachmentIds)

        return task
    }

    override suspend fun deleteTasks(groupId: ObjectId) {
        val filter = Filters.eq(Task::groupId.name, groupId)
        taskCollection.find(filter).collect { task: Task ->
            solutionRepository.deleteSolutions(taskId = task.id)
            attachmentRepository.deleteAttachments(task.attachmentIds)
            commentRepository.deleteComments(task.id)
        }
        taskCollection.deleteMany(filter)
    }

     override suspend fun updateCommentAmount(taskId: ObjectId, amount: Int): Int {
         val filter = Filters.eq("_id", taskId)
         val updates = Updates.inc(Solution::commentAmount.name, amount)

         val solution = taskCollection.findOneAndUpdate(filter, updates) ?: return 0

         return solution.commentAmount + amount
     }

    override suspend fun doesCategoryExist(groupId: ObjectId, category: String): Boolean {
        val filter = Filters.and(
            Filters.eq("_id", groupId),
            Filters.eq(Task::category.name, category)
        )
        return taskCollection.find(filter).firstOrNull() != null
    }
 }