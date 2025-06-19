package quest.toybox.template.extension

import org.gradle.api.Named
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Property
import java.net.URI

abstract class ModDependency(private val name: String) : Named {
    abstract val type: Property<DependencyType>
    abstract val curseforgeName: Property<String>
    abstract val modrinthName: Property<String>
    abstract val javaDirectory: Property<Any>
    abstract val kotlinDirectory: Property<Any>
    abstract val enabledAtRuntime: Property<Boolean>

    init {
        type.convention(DependencyType.OPTIONAL)
        curseforgeName.convention(name)
        modrinthName.convention(name)
        javaDirectory.convention("src/main/${name.replace("-", "_")}Java")
        kotlinDirectory.convention("src/main/${name.replace("-", "_")}Kotlin")
        enabledAtRuntime.convention(false)
    }

    private val repositories: MutableMap<URI, RepositoryExclusions> = mutableMapOf()
    private val artifacts: MutableList<DependencyHandler.(Boolean) -> Unit> = mutableListOf()

    fun required() {
        type.set(DependencyType.REQUIRED)
    }

    fun disabled() {
        type.set(DependencyType.DISABLED)
    }

    fun runtime() {
        enabledAtRuntime.set(true)
    }

    fun requiresRepo(name: String, url: String, groups: Set<String> = setOf()) {
        val uri = URI(url)

        if (uri in repositories) {
            repositories[uri]!!.groups.addAll(groups)
        } else {
            repositories[uri] = RepositoryExclusions(name, groups.toMutableSet())
        }
    }

    fun artifacts(function: DependencyHandler.(enabledAtRuntime: Boolean) -> Unit) {
        artifacts.add(function)
    }

    fun getRepositories() = repositories
    fun getArtifacts() = artifacts

    override fun getName(): String = name
}
