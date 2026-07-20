pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "sanwo-android"
include(":sanwo")
include(":paystack")
include(":flutterwave")

include(":interswitch")
include(":monnify")
include(":paypal")
include(":razorpay")
include(":stripe")