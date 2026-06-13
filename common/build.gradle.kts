plugins {
    `multiloader-common`
    alias(libs.plugins.neoforged.moddev)
}

neoForge {
    neoFormVersion = libs.versions.neoforged.neoform.get()
    // Automatically enable AccessTransformers if the file exists
    val at = file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
}

dependencies {
    compileOnly(libs.mixin)
    // fabric and neoforge both bundle mixinextras, so it is safe to use it in common
    compileOnly(libs.mixinextra)
    annotationProcessor(libs.mixinextra)
}

configurations {
    register("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("commonResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    val mainSourceSet = sourceSets.main.get()
    add("commonJava", mainSourceSet.java.sourceDirectories.singleFile)
    add("commonResources", mainSourceSet.resources.sourceDirectories.singleFile)
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements").forEach { variant ->
    configurations.named(variant) {
        attributes {
            attribute(loaderAttribute, "common")
        }
    }
}

sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName).forEach { variant ->
        configurations.named(variant) {
            attributes {
                attribute(loaderAttribute, "common")
            }
        }
    }
}
