@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    android {
        namespace = "com.ksensor.plugins.sensors.motion.accelerometer"
        compileSdk = 36
        minSdk = 24
    }

    val xcfName = "accelerometerKit"

    iosX64 { binaries.framework { baseName = xcfName } }
    iosArm64 { binaries.framework { baseName = xcfName } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName } }
    jvm()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
