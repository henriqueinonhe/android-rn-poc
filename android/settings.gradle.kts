import groovy.lang.Closure

apply(from = file("../node_modules/@react-native-community/cli-platform-android/native_modules.gradle"))
(extra["applyNativeModulesSettingsGradle"] as Closure<*>).call(settings)

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "PoC"
include(":app")
includeBuild("../node_modules/@react-native/gradle-plugin")
 