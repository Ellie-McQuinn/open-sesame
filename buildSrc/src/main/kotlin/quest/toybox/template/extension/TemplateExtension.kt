package quest.toybox.template.extension

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class TemplateExtension @Inject constructor(factory: ObjectFactory) {
    val mods: NamedDomainObjectContainer<ModDependency> = factory.domainObjectContainer(ModDependency::class.java)

    fun getDependencyIds(target: UploadTarget, type: DependencyType): Set<String> {
        return mods.filter { it.type.get() == type }.mapNotNull {
            when (target) {
                UploadTarget.CURSEFORGE -> it.curseforgeName.orNull
                UploadTarget.MODRINTH -> it.modrinthName.orNull
                UploadTarget.GITHUB -> null
            }
        }.toSet()
    }
}
