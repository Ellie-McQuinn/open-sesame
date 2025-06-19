import quest.toybox.template.Constants

plugins {
    id("template-platform")
    id("net.neoforged.moddev")
}

neoForge {
    version = Constants.NEOFORGE_VERSION

    accessTransformers.from(project(":common").neoForge.accessTransformers.files)

    parchment {
        minecraftVersion = Constants.PARCHMENT_MINECRAFT
        mappingsVersion = Constants.PARCHMENT_RELEASE
    }

    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", Constants.MOD_ID)
        }

        create("client") {
            client()
            ideName = "NeoForge Client (:neoforge)"
        }

        val common = findProject(":common")!!

        create("commonData") {
            clientData()
            ideName = "Common Data (:neoforge)"

            programArguments.addAll(
                "--mod", Constants.MOD_ID,
                "--output", common.file("src/generated/resources").absolutePath,
                "--existing", common.file("src/main/resources").absolutePath,
                "--all"
            )

            systemProperty("template.datagen.common", "true")
        }

        create("data") {
            clientData()
            ideName = "NeoForge Data (:neoforge)"

            programArguments.addAll(
                "--mod", Constants.MOD_ID,
                "--output", file("src/generated/resources").absolutePath,
                "--existing", common.file("src/main/resources").absolutePath,
                "--all"
            )
        }

        create("server") {
            server()
            ideName = "NeoForge Server (:neoforge)"
        }
    }

    mods {
        create(Constants.MOD_ID) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets.main {
    resources.srcDirs("src/generated/resources")
}

tasks.processResources {
    exclude("*.accesswidener")
}
