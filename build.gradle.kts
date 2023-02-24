import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

group = "com.personoid"
version = "1.0.0"
description = "A fork from Personoid"

// Configure plugin.yml generation
bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "com.personoid.PersonoidPlugin"
    apiVersion = "1.14"
    authors = listOf("DefineDoddy", "notnotnotswipez")

    commands {
        register("personoid") {
            description = "Accesses Personoid's configurations"
            usage = "/personoid <sub-command>"
            permission = "personoid.*"
        }
    }
}

plugins {
    `java-library`
    id("maven-publish")
    id("io.papermc.paperweight.userdev") version "1.3.5"
    id("xyz.jpenilla.run-paper") version "1.0.6" // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1" // Generates plugin.yml
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    //paper and development
    //paperDevBundle("1.19.3-R0.1-SNAPSHOT")
    paperDevBundle("1.18.2-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    compileOnly("org.jetbrains:annotations:23.1.0")

    implementation("net.bytebuddy:byte-buddy:1.12.21")
    implementation("org.apache.httpcomponents:httpmime:4.5.14")
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        options.release.set(17)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }

}

//java {
//    withJavadocJar()
//    withSourcesJar()
//}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String?
            artifactId = project.name
            version = project.version as String?

            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "ScharkIO"
            url = uri("https://repo.schark.io/private")
            credentials{
                username = project.properties["reposilite.username"] as String?
                password = project.properties["reposilite.token"] as String?
            }
        }
    }
}