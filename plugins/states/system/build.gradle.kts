@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    android {
        namespace = "com.ksensor.plugins.states.system"
        compileSdk = 36
        minSdk = 24
    }

    val xcfName = "systemStateKit"

    iosX64 { binaries.framework { baseName = xcfName } }
    iosArm64 { binaries.framework { baseName = xcfName } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName } }
    jvm()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            api(project(":plugins:states:system:battery"))
            api(project(":plugins:states:system:volume"))
            api(project(":plugins:states:system:locale"))
            api(project(":plugins:states:system:screen"))
            api(project(":plugins:states:system:brightness"))
            api(project(":plugins:states:system:lock"))
            api(project(":plugins:states:system:powersave"))
            api(project(":plugins:states:system:storage"))
            api(project(":plugins:states:system:resources"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
