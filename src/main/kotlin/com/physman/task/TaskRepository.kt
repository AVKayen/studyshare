package com.physman.task

import com.physman.forms.UploadFileData
import org.bson.types.ObjectId

// TODO Add update method
interface TaskRepository {
    suspend fun getTasks(): List<TaskView>
    suspend fun createTask(task: Task, files: List<UploadFileData>)
    suspend fun getTask(id: ObjectId): TaskView?
//    suspend fun getTaskBySolution(solutionId: ObjectId): TaskView?
    suspend fun deleteTask(id: ObjectId)
//    suspend fun updateTask(id: ObjectId, taskUpdate: TaskUpdate): Task?

    suspend fun updateCommentAmount(taskId: ObjectId, amount: Int): Int
}