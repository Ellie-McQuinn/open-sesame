package quest.toybox.template

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

object Constants {
    const val GROUP = "quest.toybox.open_sesame"
    const val MOD_ID = "open_sesame"
    const val MOD_NAME = "Open Sesame"
    const val MOD_VERSION = "2106.1.0.2"
    const val LICENSE = "MIT"
    const val DESCRIPTION = """
        Open doors together; Open Sesame!
    """

    const val HOMEPAGE = "https://www.curseforge.com/minecraft/mc-mods/open-sesame"
    const val ISSUE_TRACKER = "https://github.com/Ellie-McQuinn/open-sesame/issues"
    const val SOURCES_URL = "https://github.com/Ellie-McQuinn/open-sesame"

    val curseforgeProperties: CurseForgeProperties? = object : CurseForgeProperties() {
        override val projectId = "1282518"
        override val projectSlug = "open-sesame"
    }

    val modrinthProperties: ModrinthProperties? = object : ModrinthProperties() {
        override val projectId: String = "u40fRXlK"
    }

    val githubProperties: GithubProperties? = object : GithubProperties() {
        override val repo: String = "Ellie-McQuinn/open-sesame"
    }

    const val PUBLISH_WEBHOOK_VARIABLE = "PUBLISH_WEBHOOK"

    const val COMPARE_URL = "https://github.com/Ellie-McQuinn/open-sesame/compare/"

    val CONTRIBUTORS = linkedMapOf(
        "Ellie McQuinn / Toybox System" to "Project Owner"
    )

    val CREDITS = linkedMapOf<String, String>(

    )

    val EXTRA_MOD_INFO_REPLACEMENTS = mapOf<String, String>(

    )

    val JAVA_VERSION = JavaLanguageVersion.of(21)
    const val JETBRAIN_ANNOTATIONS_VERSION = "26.0.2"

    const val MIXIN_VERSION = "0.15.2+mixin.0.8.7"
    const val MIXIN_EXTRAS_VERSION = "0.4.1"

    const val MINECRAFT_VERSION = "1.21.6"
    const val FL_MINECRAFT_CONSTRAINT = "1.21.6"
    const val NF_MINECRAFT_CONSTRAINT = "[1.21.6]"

    // https://parchmentmc.org/docs/getting-started#choose-a-version/
    const val PARCHMENT_MINECRAFT = "1.21.5"
    const val PARCHMENT_RELEASE = "2025.06.15"

    // https://fabricmc.net/develop/
    const val FABRIC_API_VERSION = "0.127.1+1.21.6"
    const val FABRIC_KOTLIN_VERSION = "1.13.2+kotlin.2.1.20"
    const val FABRIC_LOADER_VERSION = "0.16.14"

    const val NEOFORM_VERSION = "1.21.6-20250617.151856" // // https://projects.neoforged.net/neoforged/neoform/
    const val NEOFORGE_VERSION = "21.6.4-beta" // https://projects.neoforged.net/neoforged/neoforge/
    const val NEOFORGE_KOTLIN_VERSION = "5.8.0"
    const val FML_CONSTRAINT = "[4,)" // https://projects.neoforged.net/neoforged/fancymodloader/

    fun getProjectName(project: Project): String {
        return project.name.uppercaseFirstChar()
    }

    fun getModVersion(): String {
        return if (MOD_VERSION.count { it == '.' } == 3) {
            MOD_VERSION.substringAfter('.')
        } else {
            MOD_VERSION
        }
    }

    fun getChangelog(project: Project): Provider<String> {
        return project.providers.provider {
            val compareTag = ProcessGroovyMethods.getText(ProcessGroovyMethods.execute("git describe --tags --abbrev=0")).trim()
            val commitHash = ProcessGroovyMethods.getText(ProcessGroovyMethods.execute("git rev-parse HEAD")).trim()

            buildString {
                appendLine(project.rootDir.resolve("changelog.md").readText(Charsets.UTF_8).trimEnd())

                if (compareTag.isNotBlank()) {
                    appendLine()
                    val url = "${COMPARE_URL}${compareTag.replace("+", "%2B")}...${commitHash}"
                    appendLine("A detailed changelog can be found [here](<$url>).")
                }
            }
        }
    }
}
