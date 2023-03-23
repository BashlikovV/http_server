plugins {
    kotlin("jvm") version "1.8.0"
}

group = "by.bashlikovv"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.xerial:sqlite-jdbc:3.41.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}