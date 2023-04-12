group = "me.xemor"
version = "1.0-ALPHA"
description = "quantumdungeons"
java.sourceCompatibility = JavaVersion.VERSION_17

plugins {
    java
    `maven-publish`
    `kotlin-dsl`
    id("com.github.johnrengelman.shadow") version("7.1.2")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public") }
    maven { url = uri("https://github.com/deanveloper/SkullCreator/raw/mvn-repo/") }
    maven { url = uri("https://repo.minebench.de/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/")}
}

dependencies {
    shadow("me.xemor:configurationdata:1.19.2-SNAPSHOT")
    shadow("org.jetbrains:annotations:20.1.0")
    shadow("net.kyori:adventure-platform-bukkit:4.1.0")
    shadow("net.kyori:adventure-api:4.10.1")
    shadow("io.papermc:paperlib:1.0.7")
    compileOnly("org.spigotmc:spigot-api:1.19-R0.1-SNAPSHOT")
    compileOnly("me.xemor:enchantedcombat:1.0")
}

java {
    configurations.shadow.get().dependencies.remove(dependencies.gradleApi())
}

tasks.shadowJar {
    minimize()
    relocate("net.kyori", "me.xemor.superheroes.kyori")
    relocate("me.xemor.configurationdata", "me.xemor.superheroes.configurationdata")
    relocate("de.themoep", "me.xemor.superheroes.de.themoep")
    relocate("org.jetbrains", "me.xemor.superheroes.org.jetbrains")
    relocate("mysql", "me.xemor.superheroes.mysql")
    relocate("com.zaxxer", "me.xemor.superheroes.com.zaxxer")
    relocate("org.apache.commons", "me.xemor.superheroes.org.apache.commons")
    relocate("org.bstats", "me.xemor.superheroes.org.bstats")
    relocate("dev.bassett", "me.xemor.superheroes.dev.bassett")
    configurations = listOf(project.configurations.shadow.get())
    val folder = System.getenv("pluginFolder")
    destinationDirectory.set(file(folder))
}

//Auto generated from gradle init, not entirely sure what it does
publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
//End of auto generated

// Handles version variables etc
tasks.processResources {
    inputs.property("version", rootProject.version)
    filesMatching("plugin.yml") {
        expand("version" to rootProject.version)
    }
}
