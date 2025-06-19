plugins {
    id("template-shared")
}

evaluationDependsOn(":common")

configurations {
    create("commonJava") { isCanBeResolved = true }
    create("commonKotlin") { isCanBeResolved = true }
    create("commonResources") { isCanBeResolved = true }
}

dependencies {
    compileOnly(project(":common"))

    "commonJava"(project(path = ":common", configuration = "commonJava"))
    "commonKotlin"(project(path = ":common", configuration = "commonKotlin"))
    "commonResources"(project(path = ":common", configuration = "commonResources"))
}

tasks {
    compileJava {
        dependsOn(configurations["commonJava"])
        source(configurations["commonJava"])
    }

    compileKotlin {
        dependsOn(configurations["commonKotlin"])
        source(configurations["commonKotlin"])
    }

    processResources {
        dependsOn(configurations["commonResources"])
        from(configurations["commonResources"])
    }
}
