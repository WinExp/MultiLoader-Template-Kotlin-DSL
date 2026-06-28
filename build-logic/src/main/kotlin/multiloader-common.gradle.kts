val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

plugins {
    java
    `maven-publish`
}

val modId = property("mod_id") as String
val modName = property("mod_name") as String
val modAuthor = property("mod_author") as String
val minecraftVersion = libs.findLibrary("minecraft").get().get().version
val javaVersion = property("java_version") as String
val minecraftVersionRange = property("minecraft_version_range") as String
val license = property("license") as String
val neoforgeLoaderVersionRange = property("neoforge_loader_version_range") as String
val credits = property("credits") as String

base {
    archivesName = "${modId}-${project.name}-${minecraftVersion}"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
    withJavadocJar()
}

tasks.withType(Jar::class).configureEach {
    from(rootProject.file("LICENSE"))
}

tasks.jar {
    manifest {
        attributes(mapOf(
                "Specification-Title"    to modName,
                "Specification-Vendor"   to modAuthor,
                "Specification-Version"  to archiveVersion.get(),
                "Implementation-Title"   to project.name,
                "Implementation-Version" to archiveVersion.get(),
                "Implementation-Vendor"  to modAuthor,
                "Built-On-Minecraft"     to minecraftVersion
        ))
    }
}

tasks.processResources {
    var expandProps = mapOf(
            "version"                       to project.version.toString(),
            "group"                         to project.group.toString(),
            "minecraft_version"             to minecraftVersion,
            "minecraft_version_range"       to minecraftVersionRange,
            "fabric_version"                to libs.findLibrary("fabric-api").get().get().version,
            "fabric_loader_version"         to libs.findLibrary("fabric-loader").get().get().version,
            "mod_name"                      to modName,
            "mod_author"                    to modAuthor,
            "mod_id"                        to modId,
            "license"                       to license,
            "description"                   to project.description,
            "neoforge_version"              to libs.findVersion("neoforge").get().requiredVersion,
            "neoforge_loader_version_range" to neoforgeLoaderVersionRange,
            "credits"                       to credits,
            "java_version"                  to javaVersion
    )

    val jsonExpandProps = expandProps.mapValues { (_, value) ->
        if (!value.isNullOrBlank()) value.replace("\n", "\\\\n") else value
    }

    filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        expand(expandProps)
    }

    filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) {
        expand(jsonExpandProps)
    }

    inputs.properties(expandProps)
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
    repositories {
        val localMaven = System.getenv("local_maven_url")
        if (!localMaven.isNullOrBlank()) {
            maven {
                url = uri(localMaven)
            }
        }
    }
}
