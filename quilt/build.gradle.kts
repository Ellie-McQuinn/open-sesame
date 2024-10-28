import dev.compasses.multiloader.Constants

plugins {
    id("multiloader-threadlike")
}

multiloader {
    mods {
        create("qsl") {
            required()

            artifacts {
                modImplementation(group = "org.quiltmc", name = "quilt-loader", version = Constants.QUILT_LOADER_VERSION)
                modImplementation(group = "net.fabricmc.fabric-api", name = "fabric-api", version = Constants.FABRIC_API_VERSION)
            }
        }
    }
}
