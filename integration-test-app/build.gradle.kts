import com.github.jk1.license.render.ReportRenderer
import extensions.getLocalPropertyOrNull
import licenses.FullJsonReportRenderer
import org.gradle.configurationcache.extensions.capitalized
import utils.getAppVersion
import utils.toVersionCode

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("io.objectbox")
    id("dagger.hilt.android.plugin")
    kotlin("plugin.serialization") version Versions.kotlin
    id("org.jetbrains.kotlin.plugin.compose") version Versions.kotlin
    id("com.github.jk1.dependency-license-report")
}

val version = getAppVersion()

android {
    namespace = "com.eidu.integration.test.app"
    compileSdk = Apps.compileSdk
    buildToolsVersion = Apps.buildToolsVersion

    defaultConfig {
        applicationId = Apps.testApplicationId
        minSdk = Apps.minSdk
        targetSdk = Apps.targetSdk
        versionCode = version.toVersionCode()
        versionName = version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create(BuildType.RELEASE) {
            storeFile = file(
                getLocalPropertyOrNull("sign.keystore.path")
                    ?: System.getenv("ANDROID_KEYSTORE_PATH") ?: "release.keystore"
            )
            storePassword =
                getLocalPropertyOrNull("sign.keystore.password") ?: System.getenv("ANDROID_KEYSTORE_PASSWORD")
            keyAlias = getLocalPropertyOrNull("sign.key.alias") ?: System.getenv("ANDROID_KEY_ALIAS")
            keyPassword = getLocalPropertyOrNull("sign.key.password") ?: System.getenv("ANDROID_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName(BuildType.RELEASE) {
            signingConfig = signingConfigs.getByName(BuildType.RELEASE)

            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // copy license report to resources
    applicationVariants.all {
        val variant = this

        tasks.register<Copy>("copyLicenseReport${variant.name.capitalized()}") {
            dependsOn("generateLicenseReport")
            from("$buildDir/reports/dependency-license")
            include("dependencies.json")
            into(file("${variant.mergeAssetsProvider.get().outputDir.get()}"))
        }

        tasks.named("merge${variant.name.capitalized()}Assets") {
            dependsOn("copyLicenseReport${variant.name.capitalized()}")
        }
    }
}

licenseReport {
    renderers = arrayOf<ReportRenderer>(FullJsonReportRenderer())
    allowedLicensesFile = File("$projectDir/allowed-licenses.json")
}

tasks.named("checkLicense") {
    // The checkLicense task does not declare this input itself, so we do it here. This ensures
    // that a modification of the file causes the checkLicense task to be re-evaluated.
    inputs.file("$projectDir/allowed-licenses.json")
}

tasks.named("check") {
    dependsOn("checkLicense")
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // Compose
    implementation("androidx.compose.ui:ui:${Versions.compose}")
    implementation("androidx.compose.material:material:${Versions.compose}")
    implementation("androidx.compose.ui:ui-tooling-preview:${Versions.compose}")
    implementation("androidx.compose.runtime:runtime:${Versions.compose}")
    implementation("androidx.compose.runtime:runtime-livedata:${Versions.compose}")
    implementation("androidx.navigation:navigation-compose:${Versions.navigation}")
    implementation("androidx.compose.material:material-icons-extended:${Versions.compose}")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Hilt/Dagger DI
    implementation("com.google.dagger:hilt-android:${Versions.hilt}")
    kapt("com.google.dagger:hilt-android-compiler:${Versions.hilt}")

    // EIDU dependencies
    implementation("com.eidu:integration-library:1.8.0")
    implementation("com.eidu:learning-packages:2.0.0")
}
