plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "me.stephans"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("MainKt")
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    // uses org.gradle.java.installations.auto-download=false in gradle.properties to disable auto provisioning of JDK
    jvmToolchain(17)
}

tasks {
    test {
        useTestNG()
    }
}

