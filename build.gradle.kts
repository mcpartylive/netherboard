plugins {
    `maven-publish`
    `java-library`
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

subprojects {
    group = "live.mcparty"
    version = "2.2.1"

    apply {
        plugin("com.github.johnrengelman.shadow")
        plugin("java")
        plugin("java-library")
        plugin("maven-publish")
    }

    repositories {
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
    }

    dependencies {
        api("org.slf4j:slf4j-api:2.0.0-alpha6")
        api("org.slf4j:slf4j-simple:2.0.0-alpha6")
    }

    tasks.withType(JavaCompile::class) {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    publishing {
        repositories {
            maven {
                url = uri("https://repo.mcparty.live/packages/")
                credentials {
                    username = project.findProperty("mcp.user") as? String ?: System.getenv("REPO_USERNAME")
                    password = project.findProperty("mcp.key") as? String ?: System.getenv("REPO_TOKEN")
                }
            }
        }

        publications {
            create<MavenPublication>(project.name) {
                groupId = "live.mcparty"
                artifactId = "netherboard-${project.name}"

                from(components["java"])
            }
        }
    }

    configurations.named("api").isCanBeResolved = true
    configurations.named("api").extendsFrom(configurations.named("shadow").get())
}

project(":core") {
    tasks.jar {
        archiveFileName.set("Netherboard-API-" + project.version + ".jar")
        archiveBaseName.set("netherboard-core")
    }
}