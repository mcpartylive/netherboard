plugins {
    `maven-publish`
    `java`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

subprojects {
    group = "live.mcparty"
    version = "2.0.0"

    apply {
        plugin("com.github.johnrengelman.shadow")
        plugin("java")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.0-alpha6")
        implementation("org.slf4j:slf4j-simple:2.0.0-alpha6")
    }

    tasks.withType(JavaCompile::class) {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

}

project(":core") {
    tasks.jar {
        archiveFileName.set("Netherboard-API-" + project.version + ".jar")
        archiveBaseName.set("netherboard-core")
    }
}

project(":bukkit") {
    tasks.jar {
        archiveFileName.set("Netherboard-Bukkit-" + project.version + ".jar")
        archiveBaseName.set("netherboard-bukkit")
    }

    repositories {
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
        implementation(project(":core"))
    }
}

project(":minestom") {
    tasks.jar {
        archiveFileName.set("Netherboard-minestom-" + project.version + ".jar")
        archiveBaseName.set("netherboard-minestom")
    }

    repositories {
        maven("https://repo.spongepowered.org/maven")
        maven("https://jitpack.io")
    }

    dependencies {
        compileOnly("com.github.Minestom:Minestom:4ab2f43eed")
        implementation(project(":core"))
    }
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
        create<MavenPublication>("mcp") {
            from(components["java"])
        }
    }
}
