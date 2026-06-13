val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

plugins {
    java
    `maven-publish`
}

val mod_id: String by project
val mod_name: String by project
val mod_author: String by project
val minecraft_version = libs.findLibrary("minecraft").get().get().version
val java_version: String by project
val minecraft_version_range: String by project
val license: String by project
val neoforge_loader_version_range: String by project
val credits: String by project

base {
    archivesName = "${mod_id}-${project.name}-${minecraft_version}"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(java_version)
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    // https://docs.gradle.org/current/userguide/declaring_repositories.html#declaring_content_exclusively_found_in_one_repository
    exclusiveContent {
        forRepository {
            maven {
                name = "Sponge"
                url = uri("https://repo.spongepowered.org/repository/maven-public")
            }
        }
        filter { includeGroupAndSubgroups("org.spongepowered") }
    }
    maven {
        name = "BlameJared"
        url = uri("https://maven.blamejared.com")
    }
}

tasks.named<Jar>("sourcesJar") {
    from(rootProject.file("LICENSE"))
}

tasks.jar {
    from(rootProject.file("LICENSE"))

    manifest {
        attributes(mapOf(
                "Specification-Title"    to mod_name,
                "Specification-Vendor"   to mod_author,
                "Specification-Version"  to archiveVersion.get(),
                "Implementation-Title"   to project.name,
                "Implementation-Version" to archiveVersion.get(),
                "Implementation-Vendor"  to mod_author,
                "Built-On-Minecraft"     to minecraft_version
        ))
    }
}

tasks.processResources {
    var expandProps = mapOf(
            "version"                       to project.version.toString(),
            "group"                         to project.group.toString(),
            "minecraft_version"             to minecraft_version,
            "minecraft_version_range"       to minecraft_version_range,
            "fabric_version"                to libs.findLibrary("fabric-api").get().get().version,
            "fabric_loader_version"         to libs.findLibrary("fabric-loader").get().get().version,
            "mod_name"                      to mod_name,
            "mod_author"                    to mod_author,
            "mod_id"                        to mod_id,
            "license"                       to license,
            "description"                   to project.description,
            "neoforge_version"              to libs.findVersion("neoforge").get().requiredVersion,
            "neoforge_loader_version_range" to neoforge_loader_version_range,
            "credits"                       to credits,
            "java_version"                  to java_version
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
