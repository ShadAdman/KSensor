@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    android {
        namespace = "com.ksensor.plugins.sensors.motion"
        compileSdk = 36
        minSdk = 24
    }

    val xcfName = "motionKit"

    iosX64 { binaries.framework { baseName = xcfName } }
    iosArm64 { binaries.framework { baseName = xcfName } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName } }
    jvm()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            api(project(":plugins:sensors:motion:accelerometer"))
            api(project(":plugins:sensors:motion:gyroscope"))
            api(project(":plugins:sensors:motion:stepcounter"))
            api(project(":plugins:sensors:motion:motiondetector"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
