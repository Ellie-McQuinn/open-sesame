plugins {
    id("template-shared")
}

configurations {
    consumable("commonJava")
    consumable("commonKotlin")
    consumable("commonResources")
}

afterEvaluate {
    with(sourceSets.main.get()) {
        artifacts {
            java.sourceDirectories.forEach { add("commonJava", it) }
            kotlin.sourceDirectories.forEach { add("commonKotlin", it) }
            resources.sourceDirectories.forEach { add("commonResources", it) }
        }
    }
}
