import quest.toybox.template.Constants

plugins {
    id("template-parent")
    id("net.neoforged.moddev")
}

neoForge {
    neoFormVersion = Constants.NEOFORM_VERSION

    parchment {
        minecraftVersion = Constants.PARCHMENT_MINECRAFT
        mappingsVersion = Constants.PARCHMENT_RELEASE
    }
}

dependencies {
    compileOnly(group = "net.fabricmc", name = "sponge-mixin", version = Constants.MIXIN_VERSION)
    annotationProcessor(compileOnly(group = "io.github.llamalad7", name = "mixinextras-common", version = Constants.MIXIN_EXTRAS_VERSION))

    compileOnly(group = "thedarkcolour", name = "kotlinforforge-neoforge", version = Constants.NEOFORGE_KOTLIN_VERSION)
}

sourceSets.main {
    resources.srcDirs("src/generated/resources")
}
