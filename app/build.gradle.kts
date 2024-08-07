import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

fun getSecretKeys(): Properties {
    val keyFile = project.rootProject.file("local.properties")
    val secretKeys = Properties()
    secretKeys.load(FileInputStream(keyFile))
    return secretKeys
}

android {
    namespace = "ge.transitgeorgia"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = file("keystore_file.jks")
            storePassword = getSecretKeys()["STORE_PASSWORD"] as? String
            keyPassword = getSecretKeys()["KEY_PASSWORD"] as? String
            keyAlias = getSecretKeys()["KEY_ALIAS"] as? String
        }
    }

    defaultConfig {
        val appVersionCode: Int by rootProject.extra
        val appVersionName: String by rootProject.extra

        applicationId = "ge.transitgeorgia"
        minSdk = 21
        targetSdk = 34
        buildToolsVersion = "34.0.0"
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val mapboxToken = getSecretKeys()["MAPBOX_TOKEN"]

        buildConfigField("String", "MAPBOX_TOKEN", "\"${mapboxToken}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.compose.runtime:runtime:1.6.8")
    api(project(path = ":presentation"))
    api(project(path = ":domain"))
    api(project(path = ":common"))
    api(project(path = ":data"))

    // Dependency Injection (Dagger Hilt)
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
}