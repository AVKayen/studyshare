package com.studyshare.environment

abstract class Environment {
    abstract val production: Boolean
    abstract val mongodbConnectionString: String
    abstract val databaseName: String
    abstract val bucketName: String
    abstract val staticBucketName: String
}
