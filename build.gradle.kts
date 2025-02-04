import com.google.cloud.tools.gradle.appengine.appyaml.AppEngineAppYamlExtension

val kotlin_version: String by project
val logback_version: String by project
plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.0.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.1"
    id("com.google.cloud.tools.appengine") version "2.8.0"
}

group = "com.studyshare"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-sessions-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-status-pages:3.0.2")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml-jvm")
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("org.mongodb:bson-kotlinx:5.2.1")
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.2.1")

    implementation(platform("com.google.cloud:libraries-bom:26.51.0")) // ignore this (or don't)
    implementation("com.google.cloud:google-cloud-storage:2.46.0")
    implementation("com.google.cloud:google-cloud-datastore")

    implementation("net.coobird:thumbnailator:[0.4,0.5)")
    implementation("com.twelvemonkeys.imageio:imageio-webp:3.12.0") // ImageIO plugin for handling .webp

    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

configure<AppEngineAppYamlExtension> {
    stage {
        setArtifact("build/libs/${project.name}-all.jar")
    }
    deploy {
        version = "GCLOUD_CONFIG"
        projectId = "GCLOUD_CONFIG"
    }
}