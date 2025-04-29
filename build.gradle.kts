plugins {
  kotlin("jvm") version libs.versions.kotlin
  kotlin("kapt") version libs.versions.kotlin
  alias(libs.plugins.ktlint)
}

subprojects {
  apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

allprojects {
  repositories {
    mavenCentral()
  }
}
