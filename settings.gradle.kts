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

include(":core")

include(":plugins:sensors:motion")
include(":plugins:sensors:motion:accelerometer")
include(":plugins:sensors:motion:gyroscope")
include(":plugins:sensors:motion:stepcounter")
include(":plugins:sensors:motion:motiondetector")

include(":plugins:sensors:environment")
include(":plugins:sensors:environment:barometer")
include(":plugins:sensors:environment:light")
include(":plugins:sensors:environment:proximity")
include(":plugins:sensors:environment:noise")
include(":plugins:sensors:positioning")
include(":plugins:sensors:positioning:location")
include(":plugins:sensors:positioning:magnetometer")
include(":plugins:sensors:positioning:orientation")
include(":plugins:sensors:positioning:heading")
include(":plugins:sensors:positioning:locationstatus")
include(":plugins:sensors:interaction")
include(":plugins:sensors:interaction:touch")
include(":plugins:sensors:interaction:scroll")

include(":plugins:sensors:health")
include(":plugins:sensors:health:heart")
include(":plugins:states:lifecycle")
include(":plugins:states:lifecycle:appvisibility")
include(":plugins:states:network")
include(":plugins:states:network:connectivity")
include(":plugins:states:network:activenetwork")
include(":plugins:states:network:callstate")
include(":plugins:states:system")
include(":plugins:states:system:battery")
include(":plugins:states:system:volume")
include(":plugins:states:system:locale")
include(":plugins:states:system:screen")
include(":plugins:states:system:brightness")
include(":plugins:states:system:lock")
include(":plugins:states:system:powersave")
include(":plugins:states:system:storage")
include(":plugins:states:system:resources")
include(":plugins:states:bluetooth")
include(":plugins:states:bluetooth:connections")
include(":plugins:states:bluetooth:discoveries")
include(":plugins:states:bluetooth:audiodevices")

//include(":bundles:motion")
//include(":bundles:environment")
//include(":bundles:positioning")
//include(":bundles:interaction")
//include(":bundles:network")
//include(":bundles:system")
//include(":bundles:bluetooth")
//include(":bundles:lifecycle")
//include(":bundles:all")

include(":sample")
include(":doc")
