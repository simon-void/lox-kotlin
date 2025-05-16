plugins {
    kotlin("jvm") version "2.1.21"
    application
}

group = "me.stephans"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass = "net.posteo.simonvoid.klox.MainKt"
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    // uses org.gradle.java.installations.auto-download=false in gradle.properties to disable auto provisioning of JDK
    jvmToolchain(21)
}

tasks {
    test {
        useTestNG()
    }
}

