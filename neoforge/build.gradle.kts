plugins {
    `multiloader-loader`
    alias(libs.plugins.neoforged.moddev)
}

val mod_id: String by project

neoForge {
    version = libs.versions.neoforge.get()
    // Automatically enable neoforge AccessTransformers if the file exists
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
            ideName = "NeoForge ${name.replaceFirstChar(Char::titlecase)} (${path})"
        }
        register("client") {
            client()
            gameDirectory.set(file("runs/client"))
        }
        register("data") {
            clientData()
            gameDirectory.set(file("runs/data"))
            // DataGen can be run by - "./gradlew :neoforge:runData" in Terminal.
            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            programArguments.addAll(
                "--mod", mod_id,
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
        }
        register("server") {
            server()
            file("runs/server").parentFile.mkdirs()
            gameDirectory.set(file("runs/server"))
        }
    }
    mods {
        register(mod_id) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets.named("main") {
    resources.srcDir("src/generated/resources")
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)

listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements").forEach { variant ->
    configurations.named(variant) {
        attributes {
            attribute(loaderAttribute, "neoforge")
        }
    }
}
sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName, getTaskName(null, "jarJar")).forEach { variant ->
        configurations.named(variant) {
            attributes {
                attribute(loaderAttribute, "neoforge")
            }
        }
    }
}
