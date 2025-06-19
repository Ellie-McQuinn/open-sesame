import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import quest.toybox.template.Constants
import quest.toybox.template.extension.DependencyType
import quest.toybox.template.extension.RepositoryExclusions
import quest.toybox.template.extension.TemplateExtension
import java.net.URI

plugins {
    `java-library`
    kotlin("jvm")
}

group = Constants.GROUP
version = Constants.MOD_VERSION

base.archivesName = "${Constants.MOD_ID}-${project.name}-${Constants.MINECRAFT_VERSION}"

java.toolchain {
    languageVersion = Constants.JAVA_VERSION
    vendor = JvmVendorSpec.MICROSOFT
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        languageVersion = KotlinVersion.KOTLIN_2_1
    }
}

dependencies {
    compileOnly(group = "org.jetbrains", name = "annotations", version = Constants.JETBRAIN_ANNOTATIONS_VERSION)
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = Constants.JAVA_VERSION.asInt()
        options.encoding = "UTF-8"
    }

    jar {
        archiveVersion = Constants.getModVersion()
    }
}

// region Shared Repositories
repositories {
    mavenCentral()

    exclusiveContent {
        forRepository {
            maven {
                name = "Sponge"
                url = uri("https://repo.spongepowered.org/repository/maven-public/")
            }
        }
        filter { includeGroupAndSubgroups("org.spongepowered") }
    }

    exclusiveContent {
        forRepositories(
            maven {
                name = "ParchmentMC"
                url = uri("https://maven.parchmentmc.org/")
            },
            maven {
                name = "NeoForge"
                url = uri("https://maven.neoforged.net/releases/")
            }
        )
        filter { includeGroup("org.parchmentmc.data") }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Unofficial CurseForge Maven"
                url = uri("https://cursemaven.com/")
            }
        }
        filter {
            includeGroup("curse.maven")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth Maven"
                url = uri("https://api.modrinth.com/maven/")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Kotlin for Forge Maven"
                url = uri("https://thedarkcolour.github.io/KotlinForForge/")
            }
        }
        filter {
            includeGroup("thedarkcolour")
        }
    }
}
// endregion

// region Add Information to Jar
tasks.jar {
    manifest {
        attributes(mapOf(
            "Specification-Title" to Constants.MOD_NAME,
            "Specification-Vendor" to Constants.CONTRIBUTORS.firstEntry().key,
            "Specification-Version" to archiveVersion,
            "Implementation-Title" to project.name,
            "Implementation-Version" to archiveVersion,
            "Implementation-Vendor" to Constants.CONTRIBUTORS.firstEntry().key,
            "Built-On-Minecraft" to Constants.MINECRAFT_VERSION
        ))
    }

    exclude("**/datagen/**")
    exclude(".cache/**")

    rootDir.resolve("LICENSE").also { if (it.exists()) from(it) }
}

tasks.processResources {
    val replacements = mutableMapOf(
        "version" to version,
        "group" to Constants.GROUP,
        "mod_name" to Constants.MOD_NAME,
        "mod_id" to Constants.MOD_ID,
        "license" to Constants.LICENSE,
        "description" to Constants.DESCRIPTION.trimIndent().trim().replace("\n", "\\n"),

        "nf_authors" to Constants.CONTRIBUTORS.keys.joinToString(","),

        "credits" to Constants.CREDITS.map { "${it.key} - ${it.value}" }.joinToString(",\n"),

        "homepage" to Constants.HOMEPAGE,
        "issue_tracker" to Constants.ISSUE_TRACKER,
        "sources_url" to Constants.SOURCES_URL,

        "java_version" to Constants.JAVA_VERSION.asInt(),
        "minecraft_version" to Constants.MINECRAFT_VERSION,
        "fl_minecraft_constraint" to Constants.FL_MINECRAFT_CONSTRAINT,
        "nf_minecraft_constraint" to Constants.NF_MINECRAFT_CONSTRAINT,

        "fabric_loader_version" to Constants.FABRIC_LOADER_VERSION,
        "fabric_api_version" to Constants.FABRIC_API_VERSION,
        "fabric_kotlin_version" to Constants.FABRIC_KOTLIN_VERSION.substringBefore('+'),

        "fml_version_constraint" to Constants.FML_CONSTRAINT,
        "neoforge_version" to Constants.NEOFORGE_VERSION,
        "neoforge_kotlin_version" to Constants.NEOFORGE_KOTLIN_VERSION
    )
    replacements.putAll(Constants.EXTRA_MOD_INFO_REPLACEMENTS)

    inputs.properties(replacements)

    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json", "*.mcmeta")) {
        expand(replacements)
    }
}
// endregion

// region Template Extension + Dep Management
val templateExtension = extensions.create("template", TemplateExtension::class)

project.afterEvaluate {
    val repositories: MutableMap<URI, RepositoryExclusions> = mutableMapOf()

    for (mod in templateExtension.mods) {
        for (repository in mod.getRepositories()) {
            if (repository.key in repositories) {
                repositories[repository.key]!!.groups.addAll(repository.value.groups)
            } else {
                repositories[repository.key] = repository.value
            }
        }
    }

    repositories {
        for (repository in repositories) {
            if (repository.value.groups.isEmpty()) {
                maven {
                    name = repository.value.name
                    url = repository.key
                }
            } else {
                exclusiveContent {
                    forRepositories(maven {
                        name = repository.value.name
                        url = repository.key
                    })

                    filter {
                        for (group in repository.value.groups) {
                            if (group.endsWith(".*")) {
                                includeGroupAndSubgroups(group.substringBeforeLast(".*"))
                            } else {
                                includeGroup(group)
                            }
                        }
                    }
                }
            }
        }
    }

    dependencies {
        for (mod in templateExtension.mods) {
            mod.getArtifacts().forEach {
                it.invoke(this, mod.type.get() == DependencyType.REQUIRED || mod.enabledAtRuntime.get())
            }
        }
    }

    sourceSets.main.configure {
        val enabledMods = templateExtension.mods.filter { it.type.get() != DependencyType.DISABLED }

        java.srcDirs(enabledMods.map { it.javaDirectory }.filter { project.file(it).exists() })
        kotlin.srcDirs(enabledMods.map { it.kotlinDirectory }.filter { project.file(it).exists() })
    }
}
// endregion
