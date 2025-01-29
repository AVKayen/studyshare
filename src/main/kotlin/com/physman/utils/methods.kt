package com.physman.utils

import com.physman.group.GroupRepository
import org.bson.types.ObjectId

fun className(any: Any): String {
    val str = any.javaClass.toString().lowercase()
    val lastDotIndex = str.lastIndexOf('.')
    return if (lastDotIndex != -1) {
        str.substring(lastDotIndex + 1)
    } else {
        str
    }
}
