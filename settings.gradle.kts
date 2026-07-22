pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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
include(":razorpay")
include(":example")