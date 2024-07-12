buildscript {

    val properties = java.util.Properties()
    properties.load(java.io.FileInputStream(project.rootProject.file("local.properties")))

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.5.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.50")
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.9.23-1.0.19")
    }
}

val appVersionCode: Int by extra { 37 }
val appVersionName: String by extra { "1.11.0" }