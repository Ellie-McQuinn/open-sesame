pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

buildscript {
    dependencies {
        classpath(group = "com.google.code.gson", name = "gson", version = "2.11.0")
    }
}

rootProject.name = "open-sesame"

include("common", "thread", "fabric", "quilt", "neoforge")
