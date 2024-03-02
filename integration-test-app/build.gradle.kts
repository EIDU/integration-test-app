import extensions.getLocalPropertyOrNull
import utils.getAppVersion
import utils.toVersionCode

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("io.objectbox")
    id("dagger.hilt.android.plugin")
    id("org.jlleitschuh.gradle.ktlint") version Versions.ktlintGradle
    kotlin("plugin.serialization") version "1.7.0"
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    // Compose
    implementation("androidx.compose.ui:ui:${Versions.compose}")
    implementation("androidx.compose.material:material:${Versions.compose}")
    implementation("androidx.compose.ui:ui-tooling-preview:${Versions.compose}")
    implementation("androidx.compose.runtime:runtime:${Versions.compose}")
    implementation("androidx.compose.runtime:runtime-livedata:${Versions.compose}")
    implementation("androidx.navigation:navigation-compose:${Versions.navigation}")
    implementation("androidx.compose.material:material-icons-extended:${Versions.compose}")
    implementation("androidx.activity:activity-compose:1.5.0")

    // Hilt/Dagger DI
    implementation("com.google.dagger:hilt-android:${Versions.hilt}")
    kapt("com.google.dagger:hilt-android-compiler:${Versions.hilt}")

    // EIDU dependencies
    implementation("com.eidu:integration-library:1.8.0")
    implementation("com.eidu:learning-packages:1.0.7")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.compose}")
    debugImplementation("androidx.compose.ui:ui-tooling:${Versions.compose}")
}
