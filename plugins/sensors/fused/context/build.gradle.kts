@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    android {
        namespace = "com.ksensor.plugins.sensors.fused.context"
        compileSdk = 36
        minSdk = 24
    }

    val xcfName = "fusedContextKit"

    iosX64 { binaries.framework { baseName = xcfName } }
    iosArm64 { binaries.framework { baseName = xcfName } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName } }
    jvm()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(project(":plugins:sensors:motion:motiondetector"))
            implementation(project(":plugins:sensors:positioning:location"))
            implementation(project(":plugins:sensors:environment:light"))
            implementation(project(":plugins:sensors:environment:barometer"))
            implementation(project(":plugins:sensors:environment:noise"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
