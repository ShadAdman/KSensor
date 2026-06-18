plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    androidLibrary {
        namespace = "com.ksensor.bundles.all"
        compileSdk = 37
        minSdk = 24
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.plugins.sensors.motion)
                api(projects.plugins.sensors.environment)
                api(projects.plugins.sensors.positioning)
                api(projects.plugins.sensors.interaction)
                api(projects.plugins.states.network)
                api(projects.plugins.states.system)
                api(projects.plugins.states.bluetooth)
                api(projects.plugins.states.lifecycle)
            }
        }
    }
}
