plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    android {
        namespace = "com.ksensor.bundles.network"
        compileSdk = 37
        minSdk = 24
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.plugins.states.network)
            }
        }
    }
}
