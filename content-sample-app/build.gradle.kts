import extensions.getLocalPropertyOrNull
import utils.getAppVersion
import utils.toVersionCode

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("org.jlleitschuh.gradle.ktlint") version Versions.ktlintGradle
}

val version = getAppVersion()

android {
    compileSdk = Apps.compileSdk
    buildToolsVersion = Apps.buildToolsVersion

    defaultConfig {
        applicationId = Apps.sampleApplicationId
        minSdk = Apps.minSdk
        targetSdk = Apps.targetSdk
        versionCode = version.toVersionCode()
        versionName = version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        javaCompileOptions.annotationProcessorOptions.arguments(
            mapOf(
                "room.schemaLocation" to "$projectDir/schemas",
                "room.incremental" to "true"
            )
        )
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
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn"
        )
        useIR = true
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose
        kotlinCompilerVersion = "1.5.21"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // Compose
    implementation("androidx.compose.ui:ui:${Versions.compose}")
    implementation("androidx.compose.material:material:${Versions.compose}")
    implementation("androidx.compose.ui:ui-tooling-preview:${Versions.compose}")
    implementation("androidx.compose.runtime:runtime:${Versions.compose}")
    implementation("androidx.compose.runtime:runtime-livedata:${Versions.compose}")
    implementation("androidx.navigation:navigation-compose:${Versions.navigation}")
    implementation("androidx.compose.material:material-icons-extended:${Versions.compose}")
    implementation("androidx.activity:activity-compose:1.3.1")

    // Room DB
    implementation("androidx.room:room-runtime:${Versions.room}")
    annotationProcessor("androidx.room:room-compiler:${Versions.room}")
    kapt("androidx.room:room-compiler:${Versions.room}")
    implementation("androidx.room:room-ktx:${Versions.room}")

    // Hilt/Dagger DI
    implementation("com.google.dagger:hilt-android:${Versions.hilt}")
    kapt("com.google.dagger:hilt-android-compiler:${Versions.hilt}")

    // EIDU dependencies
    implementation("com.eidu:content-app-library:1.0.29")

    // Test
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.compose}")
    debugImplementation("androidx.compose.ui:ui-tooling:${Versions.compose}")
}

tasks.register<Jar>("sourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    classifier = "sources"
}

artifacts {
    archives(tasks["sourcesJar"])
}
