package quest.toybox.template

abstract class GithubProperties {
    abstract val repo: String
    open val uploadToken: String = "GITHUB_TOKEN"
    open val commitish: String = "main-${Constants.MINECRAFT_VERSION}"
    open val tag: String = "${Constants.getModVersion()}+${Constants.MINECRAFT_VERSION}"
}
