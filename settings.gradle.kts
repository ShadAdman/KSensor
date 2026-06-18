rootProject.name = "KSensorLib"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":ksensor")
include(":core")

include(":plugins:sensors:motion")
include(":plugins:sensors:environment")
include(":plugins:sensors:positioning")
include(":plugins:sensors:interaction")
include(":plugins:states:lifecycle")
include(":plugins:states:network")
include(":plugins:states:system")
include(":plugins:states:bluetooth")
