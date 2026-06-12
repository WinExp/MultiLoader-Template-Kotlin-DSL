val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

plugins {
    java
}

pluginManager.apply("multiloader-common")

val minecraft_version = libs.findLibrary("minecraft").get().get().version

val commonJava by configurations.creating {
    isCanBeResolved = true
}
val commonResources by configurations.creating {
    isCanBeResolved = true
}

dependencies {
    compileOnly(project(":common")) {
        val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
        attributes {
            attribute(loaderAttribute, "common")
        }
    }
    commonJava(project(path = ":common", configuration = "commonJava"))
    commonResources(project(path = ":common", configuration = "commonResources"))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(commonJava)
    source(commonJava)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(commonResources)
    from(commonResources)
}

tasks.named<Javadoc>("javadoc") {
    dependsOn(commonJava)
    source(commonJava)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(commonJava)
    from(commonJava)
    dependsOn(commonResources)
    from(commonResources)
}
