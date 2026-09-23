@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidLibrary {
        namespace = "echo.music.iad1tya.shared"
        compileSdk = 36
        minSdk = 26
    }
    jvm("desktop")
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.serialization.json)
                implementation(libs.multiplatform.settings.core)
                implementation(libs.multiplatform.settings.coroutines)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:android-driver:2.0.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
                
                val jfxVersion = "19.0.2"
                val osNames = listOf("win", "mac", "linux")
                for (os in osNames) {
                    implementation("org.openjfx:javafx-base:$jfxVersion:$os")
                    implementation("org.openjfx:javafx-graphics:$jfxVersion:$os")
                    implementation("org.openjfx:javafx-media:$jfxVersion:$os")
                }
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:web-worker-driver:2.0.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
            }
        }
    }
}

sqldelight {
    databases {
        create("MusicDatabase") {
            packageName.set("echo.music.iad1tya.db")
        }
    }
}

