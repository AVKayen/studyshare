package com.physman.utils

import org.bson.types.ObjectId
import java.text.SimpleDateFormat

fun objectIdToSimpleDateString(objectId: ObjectId): String {
    val date = objectId.date
    val dateTimeFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
    return dateTimeFormat.format(date)
}