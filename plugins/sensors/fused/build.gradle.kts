@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    android {
        namespace = "com.ksensor.plugins.sensors.fused"
        compileSdk = 36
        minSdk = 24
    }

    val xcfName = "fusedSensorKit"

    iosX64 { binaries.framework { baseName = xcfName } }
    iosArm64 { binaries.framework { baseName = xcfName } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName } }
    jvm()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            api(project(":plugins:sensors:fused:activity"))
            api(project(":plugins:sensors:fused:posture"))
            api(project(":plugins:sensors:fused:orientationconfidence"))
            api(project(":plugins:sensors:fused:context"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
