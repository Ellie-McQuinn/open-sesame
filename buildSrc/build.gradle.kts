import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()

    exclusiveContent {
        forRepository {
            maven {
                name = "FabricMC's Maven"
                url = uri("https://maven.fabricmc.net/")
            }
        }
        filter {
            includeGroup("net.fabricmc")
        }
    }
}

java.toolchain {
    languageVersion = JavaLanguageVersion.of(21)
    vendor = JvmVendorSpec.MICROSOFT
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

kotlin {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_1
        apiVersion = KotlinVersion.KOTLIN_2_1
    }
}

dependencies {
    // // https://projects.neoforged.net/neoforged/moddevgradle/
    implementation(group = "net.neoforged", name = "moddev-gradle", version = "2.0.95")
    // https://maven.fabricmc.net/net/fabricmc/fabric-loom/
    implementation(group = "net.fabricmc", name = "fabric-loom", version = "1.10.5")
    // https://plugins.gradle.org/plugin/me.modmuss50.mod-publish-plugin
    implementation(group = "me.modmuss50.mod-publish-plugin", name = "me.modmuss50.mod-publish-plugin.gradle.plugin", version = "0.8.4")
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation(group = "com.google.code.gson", name = "gson", version = "2.13.1")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-gradle-plugin
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version = "2.1.0")
}