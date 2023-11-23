buildscript {

    val properties = java.util.Properties()
    properties.load(java.io.FileInputStream(project.rootProject.file("local.properties")))

    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = properties.getProperty("MAPBOX_TOKEN")
            }
        }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
    }
}

val appVersionCode: Int by extra { 14 }
val appVersionName: String by extra { "1.7.0" }

//
//plugins {
//    id 'com.android.application' version '7.3.1' apply false
//    id 'com.android.library' version '7.3.1' apply false
//    id 'org.jetbrains.kotlin.android' version '1.6.10' apply false
//    id 'com.google.dagger.hilt.android' version '2.44' apply false
//}