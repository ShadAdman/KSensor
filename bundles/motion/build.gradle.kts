import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktechPublish)
}

mavenPublishing {
    configure(KotlinMultiplatform())
}

kotlin {
    android {
        namespace = "com.ksensor.bundles.motion"
        compileSdk = 37
        minSdk = 24
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.plugins.sensors.motion)
            }
        }
    }
}
