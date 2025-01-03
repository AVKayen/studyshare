package com.physman.task

data class TaskUpdate(
    val title: String? = null,
    val additionalNotes: String? = null
) {
    data class SolutionUpdate(
        val title: String? = null,
        val additionalNotes: String? = null
    )
}