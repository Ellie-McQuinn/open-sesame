package quest.toybox.template

abstract class ModrinthProperties {
    abstract val projectId: String
    open val uploadToken: String = "MODRINTH_TOKEN"
    open val supportedMinecraftVersions = listOf(Constants.MINECRAFT_VERSION)
}
