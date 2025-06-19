import me.modmuss50.mpp.PublishModTask
import me.modmuss50.mpp.PublishResult
import me.modmuss50.mpp.ReleaseType
import org.codehaus.groovy.runtime.ProcessGroovyMethods
import quest.toybox.template.templateExt
import quest.toybox.template.Constants
import quest.toybox.template.extension.DependencyType
import quest.toybox.template.extension.UploadTarget
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

plugins {
    idea
    `java-library`

    id("me.modmuss50.mod-publish-plugin")
}

// Find checksums here: https://gradle.org/release-checksums/
// Run gradlew :wrapper a couple of times to update.
tasks.wrapper {
    gradleVersion = "8.14.2"
    distributionSha256Sum = "7197a12f450794931532469d4ff21a59ea2c1cd59a3ec3f89c035c3c420a6999"
    distributionType = Wrapper.DistributionType.BIN
}

gradle.taskGraph.whenReady {
    if (!boolean(providers.environmentVariable("MULTILOADER_DRY_RUN")).getOrElse(false)){
        if (hasTask(":publishMods")) {
            if (!providers.environmentVariable("CI").isPresent) {
                throw IllegalStateException("Cannot publish mods locally, please run the release workflow on GitHub.")
            }

            val branch = ProcessGroovyMethods.getText(ProcessGroovyMethods.execute("git branch --show-current")).trim()

            Constants.githubProperties?.also {
                if (it.commitish != branch) {
                    throw IllegalStateException("Cannot publish mods as you are trying to publish from the wrong branch, try again from: ${it.commitish}")
                }
            }
        }
    }
}

fun getReleaseType(version: String): ReleaseType =
    if ("alpha" in version) { ReleaseType.ALPHA }
    else if ("beta" in version) { ReleaseType.BETA }
    else { ReleaseType.STABLE }

fun boolean(provider: Provider<String>): Provider<Boolean> {
    return provider.map { it.equals("true", true) }
}

evaluationDependsOnChildren()

val fabricTemplate = project(":fabric").templateExt()
val neoforgeTemplate = project(":neoforge").templateExt()

publishMods {
    changelog = Constants.getChangelog(project)
    type = getReleaseType(Constants.MOD_VERSION)
    dryRun = boolean(providers.environmentVariable("MULTILOADER_DRY_RUN"))
    version = Constants.MOD_VERSION

    Constants.curseforgeProperties?.also { props ->
        val curseProps = curseforgeOptions {
            accessToken = providers.environmentVariable(props.uploadToken)
            projectId = props.projectId
            projectSlug = props.projectSlug
            clientRequired = props.clientSideRequired
            serverRequired = props.serverSideRequired
            javaVersions = props.supportedJavaVersions
            minecraftVersions = props.supportedMinecraftVersions
        }

        curseforge("curseForgeFabric") {
            from(curseProps)

            displayName = "Fabric ${Constants.getModVersion()}+${Constants.MINECRAFT_VERSION}"
            modLoaders.add("fabric")
            file(project(":fabric"))

            dependencies {
                requires(*fabricTemplate.getDependencyIds(UploadTarget.CURSEFORGE, DependencyType.REQUIRED).toTypedArray())
                optional(*fabricTemplate.getDependencyIds(UploadTarget.CURSEFORGE, DependencyType.OPTIONAL).toTypedArray())
            }
        }
        curseforge("curseForgeNeoForge") {
            from(curseProps)

            displayName = "NeoForge ${Constants.getModVersion()}+${Constants.MINECRAFT_VERSION}"
            modLoaders.add("neoforge")
            file(project(":neoforge"))

            dependencies {
                requires(*neoforgeTemplate.getDependencyIds(UploadTarget.CURSEFORGE, DependencyType.REQUIRED).toTypedArray())
                optional(*neoforgeTemplate.getDependencyIds(UploadTarget.CURSEFORGE, DependencyType.OPTIONAL).toTypedArray())
            }
        }
    }

    Constants.modrinthProperties?.also { props ->
        val modrinthProps = modrinthOptions {
            accessToken = providers.environmentVariable(props.uploadToken)
            projectId = props.projectId
            minecraftVersions = props.supportedMinecraftVersions
        }

        modrinth("modrinthFabric") {
            from(modrinthProps)

            displayName = "Fabric ${Constants.getModVersion()}+${Constants.MINECRAFT_VERSION}"
            modLoaders.add("fabric")
            file(project(":fabric"))

            dependencies {
                requires(*fabricTemplate.getDependencyIds(UploadTarget.MODRINTH, DependencyType.REQUIRED).toTypedArray())
                optional(*fabricTemplate.getDependencyIds(UploadTarget.MODRINTH, DependencyType.OPTIONAL).toTypedArray())
            }
        }
        modrinth("modrinthNeoForge") {
            from(modrinthProps)

            displayName = "NeoForge ${Constants.getModVersion()}+${Constants.MINECRAFT_VERSION}"
            modLoaders.add("neoforge")
            file(project(":neoforge"))

            dependencies {
                requires(*neoforgeTemplate.getDependencyIds(UploadTarget.MODRINTH, DependencyType.REQUIRED).toTypedArray())
                optional(*neoforgeTemplate.getDependencyIds(UploadTarget.MODRINTH, DependencyType.OPTIONAL).toTypedArray())
            }
        }
    }

    Constants.githubProperties?.also { props ->
        github {
            accessToken = providers.environmentVariable(props.uploadToken)
            repository = props.repo
            commitish = props.commitish
            tagName = props.tag

            displayName = "${Constants.MOD_NAME} ${Constants.getModVersion()}+${Constants.MINECRAFT_VERSION}"

            file(project(":fabric"))
            additionalFile(project(":neoforge"))
        }
    }
}

tasks.publishMods {
    doLast {
        val environmentVariable = providers.environmentVariable(Constants.PUBLISH_WEBHOOK_VARIABLE)

        if (!environmentVariable.isPresent) {
            return@doLast
        }

        val webhookUrl = uri(environmentVariable.get())

        val links = buildMap {
            if (Constants.curseforgeProperties != null) {
                put(UploadTarget.CURSEFORGE, setOf("publishCurseForgeFabric", "publishCurseForgeNeoForge"))
            }

            if (Constants.modrinthProperties != null) {
                put(UploadTarget.MODRINTH, setOf("publishModrinthFabric", "publishModrinthNeoForge"))
            }

            if (Constants.githubProperties != null) {
                put(UploadTarget.GITHUB, setOf("publishGithub"))
            }
        }

        val publishResults = links.mapValues { (target, publishTasks) ->
            publishTasks.map {
                val key = if (it == "publishGithub") {
                    "All Releases"
                } else {
                    it.replace("publishModrinth", "").replace("publishCurseForge", "")
                }

                val link = PublishResult.fromJson(tasks.getByName<PublishModTask>(it).result.get().asFile.readText()).link

                "[${key}](<${link}>)"
            }
        }


        val body = buildString {
            if (publishMods.dryRun.get()) {
                append(
                    """
                    |:warning: :warning: :warning:
                    |**DRY RUN**
                    |:warning: :warning: :warning:
                    """
                )
            }

            append(
                """
                |**${Constants.MOD_NAME} ${Constants.getModVersion()}** for **${Constants.MINECRAFT_VERSION}**
                |${Constants.getChangelog(project).get()}
                """
            )

            publishResults[UploadTarget.CURSEFORGE]?.also {
                appendLine(
                    "|:curseforge: ${it.joinToString(" | ")}"
                )
            }

            publishResults[UploadTarget.MODRINTH]?.also {
                appendLine(
                    "|:modrinth: ${it.joinToString(" | ")}"
                )
            }

            publishResults[UploadTarget.GITHUB]?.also {
                appendLine(
                    "|:github: ${it.joinToString(" | ")}"
                )
            }
        }.trimMargin().trim().replace("\n", "\\n")

        val request = HttpRequest.newBuilder(webhookUrl).header("Content-Type", "application/json").POST(BodyPublishers.ofString(
            """{"content": "$body"}"""
        )).build()
        val response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            project.logger.error("Failed to publish release notes to webhook:\n${response.body()}")
        }
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
