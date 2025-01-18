package com.physman.utils

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

interface Id {
    @get:BsonId
    val id: ObjectId
}