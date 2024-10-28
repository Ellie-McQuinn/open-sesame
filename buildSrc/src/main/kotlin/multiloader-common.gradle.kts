import dev.compasses.multiloader.Constants

plugins {
    id("multiloader-parent")
    id("net.neoforged.moddev")
}

neoForge {
    neoFormVersion = Constants.NEOFORM_VERSION

    parchment {
        minecraftVersion = Constants.PARCHMENT_MINECRAFT
        mappingsVersion = Constants.PARCHMENT_RELEASE
    }
}

repositories {
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

dependencies {
    compileOnly(group = "org.spongepowered", name = "mixin", version = Constants.MIXIN_VERSION)
    annotationProcessor(compileOnly(group = "io.github.llamalad7", name = "mixinextras-common", version = Constants.MIXIN_EXTRAS_VERSION))

    implementation(group = "net.fabricmc", name = "fabric-language-kotlin", version = Constants.FABRIC_KOTLIN_VERSION) {
        exclude(group = "net.fabricmc", module = "fabric-loader")
    }
}
