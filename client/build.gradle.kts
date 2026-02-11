plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
    application
    id("com.gradleup.shadow") version "8.3.6"
}

group = "com.softwaremill"
version = "unspecified"

application {
    mainClass = "com.softwaremill.MainKt"
}

tasks.getByName("run", JavaExec::class) {
    standardInput = System.`in`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:8.3.6")
    testImplementation(kotlin("test"))
    implementation("org.signal:libsignal-client:0.66.1")
    implementation("io.arrow-kt:arrow-core:2.0.1")
    implementation("io.ktor:ktor-client-core:3.1.0")
    implementation("io.ktor:ktor-client-cio:3.1.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}