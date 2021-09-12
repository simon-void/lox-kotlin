import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    application
}

group = "me.stephan"
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

tasks {
    withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = "11"
    }

    test {
        useTestNG()
    }
}

