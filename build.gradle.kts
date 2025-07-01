buildscript {

    val properties = java.util.Properties()
    properties.load(java.io.FileInputStream(project.rootProject.file("local.properties")))

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.11.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.56.2")
        classpath("com.google.gms:google-services:4.4.3")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.4")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.2.0-2.0.2")
    }
}

val appVersionCode: Int by extra { 39 }
val appVersionName: String by extra { "1.12.1" }