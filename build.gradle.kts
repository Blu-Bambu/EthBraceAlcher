import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    application
}

group = "org.blubambu"
version = "1.0-SNAPSHOT"

repositories {
    google()
    maven {
        url = uri("https://repo.powbot.org/releases")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation("org.powbot:client-sdk:1.+")
    implementation("org.powbot:client-sdk-loader:1.+")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
}