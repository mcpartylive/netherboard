tasks.jar {
    archiveFileName.set("Netherboard-Bukkit-" + project.version + ".jar")
    archiveBaseName.set("netherboard-bukkit")
    archiveClassifier.set("")
}

repositories {
    mavenLocal()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    shadow(project(":core"))
}