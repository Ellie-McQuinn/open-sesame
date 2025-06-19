plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

buildscript {
    dependencies {
        classpath(group = "com.google.code.gson", name = "gson", version = "2.13.1")
    }
}

rootProject.name = "open-sesame"

include(
    "common", "fabric", "neoforge"
)

fun ProjectDescriptor.makeProjectDirectories() {
    if (!projectDir.exists()) {
        projectDir.mkdirs()
    }

    for (descriptor in children) {
        descriptor.makeProjectDirectories()
    }
}

rootProject.makeProjectDirectories()
