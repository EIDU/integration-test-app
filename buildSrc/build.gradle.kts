import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "2.0.0"
}

repositories {
    google()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation("com.github.jk1:gradle-license-report:2.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
}
