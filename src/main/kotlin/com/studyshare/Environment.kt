package com.studyshare

import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.Entity

data class Environment(val PRODUCTION: Boolean) {
    private val datastore: Datastore = DatastoreOptions.newBuilder()
        .setProjectId("skillful-fx-446014-k1")
        .setDatabaseId("environment")
        .build().service
    private val key = datastore.newKeyFactory().setKind("EnvironmentVariables").newKey(5644004762845184L)
    private val entity: Entity? = datastore.get(key)

    init {
        if (entity == null) {
            println("Defaulting to environment variables")
        }
    }


    val MONGODB_CONNECTION_STRING: String =
        if (entity != null) {
            entity.getString("MONGODB_CONNECTION_STRING") ?: throw Exception("No connection string found")
        } else {
            System.getenv("MONGODB_CONNECTION_STRING")
        }

    val DATABASE_NAME: String =
        if (entity != null) {
            entity.getString("DATABASE_NAME") ?: throw Exception("No database name found")
        } else {
            System.getenv("DATABASE_NAME")
        }

    val BUCKET_NAME: String =
        if (entity != null) {
            entity.getString("BUCKET_NAME") ?: throw Exception("No bucket name found")
        } else {
            System.getenv("BUCKET_NAME")
        }

    val STATIC_BUCKET_NAME: String =
        if (entity != null) {
            entity.getString("STATIC_BUCKET_NAME") ?: throw Exception("No static bucket name found")
        } else {
            System.getenv("STATIC_BUCKET_NAME")
        }
}