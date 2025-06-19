import com.google.gson.JsonObject
import quest.toybox.template.Constants
import quest.toybox.template.task.JsonProcessingReader

// Signifies a Project that will be released to users, e.g. Fabric and NeoForge sub projects.

plugins {
    id("template-child")
}

tasks.processResources {
    filesMatching(listOf("**/*.json", "**/*.mcmeta")) {
        val processor: (JsonObject.() -> Unit)? = when (name) {
            "fabric.mod.json" -> { ->
                val authors = getAsJsonArray("authors")
                val contributors = getAsJsonArray("contributors")

                for ((contributor, role) in Constants.CONTRIBUTORS) {
                    if (role == "Project Owner") {
                        authors.add(contributor)
                    } else {
                        contributors.add(contributor)
                    }
                }
            }
            else -> null
        }

        filter(mapOf("processor" to processor), JsonProcessingReader::class.java)
    }
}
