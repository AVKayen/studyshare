package com.physman.task

import com.physman.forms.UploadFileData
import org.bson.types.ObjectId

interface TaskRepository {
    suspend fun getTasks(): List<TaskView>
    suspend fun createTask(task: Task, files: List<UploadFileData>)
    suspend fun getTask(id: ObjectId): TaskView?
    suspend fun deleteTask(id: ObjectId)
//    suspend fun deleteTasks(groupId: ObjectId) TODO: Uncomment this

    suspend fun updateCommentAmount(taskId: ObjectId, amount: Int): Int
}