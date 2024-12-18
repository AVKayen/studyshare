package com.physman.models

data class Task(
    val id: Int,
    val title: String,
    val additionalNotes: String?
)

// No idea if this is the proper way of handling optional model for task updates (any less repetitive solution?)
data class TaskOptional(
    val id: Int,
    val title: String?,
    val additionalNotes: String?
)