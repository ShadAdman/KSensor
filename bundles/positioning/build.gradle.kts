plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    android {
        namespace = "com.ksensor.bundles.positioning"
        compileSdk = 37
        minSdk = 24
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.plugins.sensors.positioning)
            }
        }
    }
}
