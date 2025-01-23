package com.physman.utils

import org.bson.types.ObjectId
import java.text.SimpleDateFormat

fun className(any: Any): String {
    val str = any.javaClass.toString()
    val lastDotIndex = str.lastIndexOf('.')
    return if (lastDotIndex != -1) {
        str.substring(lastDotIndex + 1)
    } else {
        str
    }
}

fun objectIdToSimpleDateString(objectId: ObjectId): String {
    val date = objectId.date
    val dateTimeFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
    return dateTimeFormat.format(date)
}