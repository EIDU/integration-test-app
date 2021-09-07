import extensions.getLocalProperty

plugins {
    id("com.github.ben-manes.versions") version "0.39.0"
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val compose_version by extra("1.0.1")
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.gradle}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt}")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/EIDU/content-test-app")
            credentials {
                username = System.getenv("READPACKAGES_GITHUB_USER")
                    ?: System.getenv("GITHUB_READPACKAGES_USER")
                            ?: getLocalProperty("githubReadPackagesUser")
                password = System.getenv("READPACKAGES_GITHUB_TOKEN")
                    ?: System.getenv("GITHUB_READPACKAGES_TOKEN")
                            ?: getLocalProperty("githubReadPackagesToken")
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.register("installGitHooks", Exec::class) {
    commandLine("git", "config", "--local", "core.hooksPath", "git-hooks")
}

tasks.getByPath(":content-test-app:preBuild").dependsOn(":installGitHooks")
