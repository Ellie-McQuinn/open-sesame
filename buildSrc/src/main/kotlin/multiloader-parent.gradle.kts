plugins {
    id("multiloader-shared")
}

configurations {
    consumable("${project.name}Java")
    consumable("${project.name}Kotlin")
    consumable("${project.name}Resources")
}

afterEvaluate {
    with(sourceSets.main.get()) {
        artifacts {
            java.sourceDirectories.forEach { add("${project.name}Java", it) }
            kotlin.sourceDirectories.forEach { add("${project.name}Kotlin", it) }
            resources.sourceDirectories.forEach { add("${project.name}Resources", it) }
        }
    }
}
