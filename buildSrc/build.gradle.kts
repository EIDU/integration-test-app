import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    `kotlin-dsl`
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
