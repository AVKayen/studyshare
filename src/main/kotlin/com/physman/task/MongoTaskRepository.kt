package com.physman.task

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.physman.forms.UploadFileData
import com.physman.attachment.AttachmentRepository
import com.physman.comment.CommentRepository
import com.physman.solution.Solution
import com.physman.solution.SolutionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

 // TODO: error handling
class MongoTaskRepository(
     mongoDatabase: MongoDatabase,
     private val attachmentRepository: AttachmentRepository,
     private val commentRepository: CommentRepository,
     private val solutionRepository: SolutionRepository
) : TaskRepository {
    private val taskCollection = mongoDatabase.getCollection<Task>("tasks")

    override suspend fun createTask(task: Task, files: List<UploadFileData>) {

        val attachments = attachmentRepository.createAttachments(files)

        val taskWithAttachments = task.copy(
            attachmentIds = attachments.map { it.id }
        )

        taskCollection.insertOne(taskWithAttachments)
    }

    override suspend fun getTasks(): List<TaskView> {
        return taskCollection.find().toList().map { task: Task ->
            TaskView(
                task =  task,
                attachments = attachmentRepository.getAttachments(task.attachmentIds)
            )
        }
    }

    override suspend fun getTask(id: ObjectId): TaskView? {
        val filter = Filters.eq("_id", id)
        val task = taskCollection.find(filter).firstOrNull() ?: return null
        return TaskView(
            task = task,
            attachments = attachmentRepository.getAttachments(task.attachmentIds)
        )
    }
     override suspend fun deleteTask(id: ObjectId) {
         val filter = Filters.eq("_id", id)
         val task = taskCollection.findOneAndDelete(filter) ?: return

         commentRepository.deleteComments(id)
        solutionRepository.deleteSolutions(taskId = task.id)
        attachmentRepository.deleteAttachments(task.attachmentIds)
    }

     override suspend fun updateCommentAmount(taskId: ObjectId, amount: Int): Int {
         val filter = Filters.eq("_id", taskId)
         val updates = Updates.inc(Solution::commentAmount.name, amount)

         val solution = taskCollection.findOneAndUpdate(filter, updates) ?: return 0

         return solution.commentAmount + amount
     }
 }