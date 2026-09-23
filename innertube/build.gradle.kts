@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.music.innertube"
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
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.client.encoding)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.brotli)
                implementation(libs.newpipeextractor)
                implementation(libs.pipepipe.extractor)
                implementation("com.github.TeamNewPipe:nanojson:c7a6c1c08d16b6d5ecded34758e6415e07be2166")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.brotli)
                implementation(libs.newpipeextractor)
                implementation(libs.pipepipe.extractor)
                implementation("com.github.TeamNewPipe:nanojson:c7a6c1c08d16b6d5ecded34758e6415e07be2166")
            }
        }
        val jsMain by getting {
            dependencies {
                
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugaring)
}


