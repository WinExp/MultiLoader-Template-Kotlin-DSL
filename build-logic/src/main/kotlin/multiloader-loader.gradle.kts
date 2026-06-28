val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

plugins {
    java
    id("multiloader-common")
}

val minecraftVersion = libs.findLibrary("minecraft").get().get().version

val commonJava = configurations.create("commonJava") {
    isCanBeResolved = true
}
val commonResources = configurations.create("commonResources") {
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

tasks.compileJava {
    dependsOn(commonJava)
    source(commonJava)
}

tasks.processResources {
    dependsOn(commonResources)
    from(commonResources)
}

tasks.javadoc {
    dependsOn(commonJava)
    source(commonJava)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(commonJava)
    from(commonJava)
    dependsOn(commonResources)
    from(commonResources)
}
