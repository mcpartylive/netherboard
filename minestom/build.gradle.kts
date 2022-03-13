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