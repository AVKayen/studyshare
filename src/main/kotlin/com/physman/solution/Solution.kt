package com.physman.solution

import java.util.*

data class Solution(
    val id: Int,
    val title: String,
    val additionalNotes: String? = null,
    val images: List<UUID> = emptyList(),
)
