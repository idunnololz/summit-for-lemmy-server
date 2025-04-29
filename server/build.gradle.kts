plugins {
  kotlin("jvm") version libs.versions.kotlin
  alias(libs.plugins.ktor)
  alias(libs.plugins.serialization)
  kotlin("kapt") version libs.versions.kotlin
}

group = "com.idunnololz.summitForLemmy.server"
version = "0.0.1"

application {
  mainClass.set("com.idunnololz.summitForLemmy.server.ApplicationKt")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

dependencies {
  implementation("io.ktor:ktor-server-core-jvm")
  implementation("io.ktor:ktor-server-host-common-jvm")
  implementation("io.ktor:ktor-server-content-negotiation-jvm")
  implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
  implementation("io.ktor:ktor-server-netty-jvm")
  implementation("io.ktor:ktor-server-status-pages")
  implementation("io.ktor:ktor-server-config-yaml")
  implementation("io.ktor:ktor-server-rate-limit")
  implementation("io.ktor:ktor-server-auth")
  implementation("io.ktor:ktor-server-auth-jwt")

  implementation("io.ktor:ktor-client-core")
  implementation("io.ktor:ktor-client-cio")
  implementation("io.ktor:ktor-client-content-negotiation")

  implementation(libs.logback.classic)
  implementation(libs.postgresql)

  implementation(libs.dagger)
  kapt(libs.dagger.compiler)

  implementation(libs.exposed.core)
  implementation(libs.exposed.crypt)
  implementation(libs.exposed.dao)
  implementation(libs.exposed.jdbc)
  implementation(libs.exposed.kotlin.datetime)
  implementation(libs.exposed.json)
  implementation(libs.json)
  implementation(libs.krontab)

  implementation(libs.flyway.core)
  implementation(libs.flyway.database.postgresql)
  implementation(libs.hikaricp)

//    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-money:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")

  testImplementation("io.ktor:ktor-server-test-host-jvm")
  testImplementation(libs.kotlin.test.junit)
}
