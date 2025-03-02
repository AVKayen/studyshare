package com.studyshare.task

import com.studyshare.forms.UploadFileData
import org.bson.types.ObjectId

interface TaskRepository {
    suspend fun getTasks(groupId: ObjectId, category: String?, resultCount: Int, lastId: ObjectId?): List<Task>
    suspend fun createTask(task: Task, files: List<UploadFileData>): TaskView
    suspend fun getTaskView(id: ObjectId): TaskView
    suspend fun updateTask(id: ObjectId, taskUpdates: TaskUpdates): TaskView
    suspend fun deleteTask(id: ObjectId): Task
    suspend fun deleteTasks(groupId: ObjectId)
    suspend fun doesCategoryExist(groupId: ObjectId, category: String): Boolean

    suspend fun updateCommentAmount(taskId: ObjectId, amount: Int): Int
}