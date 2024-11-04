plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("kapt") version libs.versions.kotlin
}

allprojects {
    repositories {
        mavenCentral()
    }
}