package com.physman.image

import java.util.UUID

data class Image(
    val filename: String,
    val content: ByteArray,
    val id: UUID = UUID.randomUUID(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (filename != other.filename) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}