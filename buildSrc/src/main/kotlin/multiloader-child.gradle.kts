import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("multiloader-shared")
}

var parents = if (project.name == "neoforge" || project.name == "thread") {
    listOf("common")
} else {
    listOf("common", "thread")
}

parents.forEach {
    evaluationDependsOn(":${it}")
}

configurations {
    parents.forEach {
        create("${it}Java") { isCanBeResolved = true }
        create("${it}Kotlin") { isCanBeResolved = true }
        create("${it}Resources") { isCanBeResolved = true }
    }
}

dependencies {
    parents.forEach {
        compileOnly(project(":${it}"))

        "${it}Java"(project(path = ":${it}", configuration = "${it}Java"))
        "${it}Kotlin"(project(path = ":${it}", configuration = "${it}Kotlin"))
        "${it}Resources"(project(path = ":${it}", configuration = "${it}Resources"))
    }
}

tasks {
    "compileJava"(JavaCompile::class) {
        parents.forEach {
            dependsOn(configurations.getByName("${it}Java"))
            source(configurations.getByName("${it}Java"))
        }
    }

    "compileKotlin"(KotlinCompile::class) {
        parents.forEach {
            dependsOn(configurations.getByName("${it}Kotlin"))
            source(configurations.getByName("${it}Kotlin"))
        }
    }

    processResources {
        parents.forEach {
            dependsOn(configurations.getByName("${it}Resources"))
            from(configurations.getByName("${it}Resources")) {
                exclude("fabric.mod.json")
            }
        }
    }
}