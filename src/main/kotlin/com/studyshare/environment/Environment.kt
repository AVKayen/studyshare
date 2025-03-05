package com.studyshare.environment

abstract class Environment(open val production: Boolean) {
    abstract val mongodbConnectionString: String
    abstract val databaseName: String
    abstract val bucketName: String
    abstract val staticBucketName: String
}
