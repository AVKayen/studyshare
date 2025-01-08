package com.physman.task

import com.physman.image.ImageView

data class TaskView(
    val task: Task,
    val images: List<ImageView> = emptyList(),
)