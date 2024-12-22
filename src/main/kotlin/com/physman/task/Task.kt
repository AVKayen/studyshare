package com.physman.task

import java.util.*

data class Task(
    val id: Int,
    val title: String,
    val additionalNotes: String? = null,
    val images: List<UUID> = emptyList(),
)