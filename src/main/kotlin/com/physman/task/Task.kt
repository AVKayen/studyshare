package com.physman.task

import com.physman.solution.Solution
import java.util.*

data class Task(
    val id: Int,
    val title: String,
    val additionalNotes: String? = null,
    val images: List<UUID> = emptyList(),

    val solutions: MutableList<Solution> = mutableListOf<Solution>()
)