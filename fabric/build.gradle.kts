plugins {
    id("multiloader-loader")
    alias(libs.plugins.fabric.loom)
}

val mod_id: String by project

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
}

loom {
    val aw = project(":common").file("src/main/resources/${mod_id}.accesswidener")
    if (aw.exists()) {
        accessWidenerPath.set(aw)
    }
    runs {
        listOf("client" to "Fabric Client", "server" to "Fabric Server").forEach { (runType, displayNameStr) ->
                named(runType) {
                    if (runType == "client") client() else server()
                    displayName = displayNameStr
                    generateRunConfig = true
                    runDirectory.set(file("runs/$runType"))
                }
        }
    }
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements", "modCompileClasspath").forEach { variant ->
    configurations.named(variant) {
        attributes {
            attribute(loaderAttribute, "fabric")
        }
    }
}

sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName).forEach { variant ->
        configurations.named(variant) {
            attributes {
                attribute(loaderAttribute, "fabric")
            }
        }
    }
}
