package com.studyshare

import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.Entity

class Environment(val production: Boolean) {
    private val datastore: Datastore = DatastoreOptions.newBuilder()
        .setProjectId("skillful-fx-446014-k1")
        .setDatabaseId("environment")
        .build().service
    private val key = datastore.newKeyFactory().setKind("EnvironmentVariables").newKey(5644004762845184L)
    private val entity: Entity? = datastore.get(key)

    val MONGODB_CONNECTION_STRING: String =
        if (production && entity != null) {
            entity.getString("MONGODB_CONNECTION_STRING") ?: throw Exception("No connection string found")
        } else {
            println("No datastore entity found. Defaulting to environment variables. Unimportant for local development.")
            System.getenv("MONGODB_CONNECTION_STRING")
        }
}