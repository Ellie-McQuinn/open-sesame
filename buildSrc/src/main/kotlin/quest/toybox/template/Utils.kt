package quest.toybox.template

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import quest.toybox.template.extension.TemplateExtension

fun Project.templateExt() = extensions.getByName<TemplateExtension>("template")

fun Project.templateExt(action: TemplateExtension.() -> Unit) {
    extensions.configure<TemplateExtension>("template") {
        action(this)
    }
}
