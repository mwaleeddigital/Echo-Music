plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvm("desktop") {
        mainRun {
            mainClass.set("com.music.echo.desktop.MainKt")
        }
    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(project(":shared"))
                implementation(project(":shared-ui"))
                implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.music.echo.desktop.MainKt"
        jvmArgs += listOf("-Dskiko.renderApi=SOFTWARE")
        buildTypes.release.proguard {
            isEnabled.set(false)
        }
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "EchoMusic"
            packageVersion = "1.3.0"
            description = "Echo Music Desktop Player"
            vendor = "Echo Music"
            includeAllModules = true
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            windows {
                menu = true
                shortcut = true
                dirChooser = true
                upgradeUuid = "6b4ef84c-35cf-42ec-a059-ff6d9338b812"
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
        }
    }
}
