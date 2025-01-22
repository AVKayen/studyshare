package com.physman.utils

fun className(any: Any): String {
    val str = any.javaClass.toString()
    val lastDotIndex = str.lastIndexOf('.')
    return if (lastDotIndex != -1) {
        str.substring(lastDotIndex + 1)
    } else {
        str
    }
}