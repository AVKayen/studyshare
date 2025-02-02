package com.physman.task

import com.physman.forms.UploadFileData
import com.physman.solution.SolutionView
import org.bson.types.ObjectId

interface TaskRepository {
    suspend fun getTasks(groupId: ObjectId, category: String?, resultCount: Int, lastId: ObjectId?): List<Task>
    suspend fun createTask(task: Task, files: List<UploadFileData>): TaskView
    suspend fun getTask(id: ObjectId): TaskView?
    suspend fun updateTask(id: ObjectId, newTask: Task)
    suspend fun deleteTask(id: ObjectId): Task?
    suspend fun deleteTasks(groupId: ObjectId)
    suspend fun doesCategoryExist(groupId: ObjectId, category: String): Boolean

    suspend fun updateCommentAmount(taskId: ObjectId, amount: Int): Int
}