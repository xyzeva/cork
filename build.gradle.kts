plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("de.eldoria.plugin-yml.bukkit") version "0.9.0"
    id("io.freefair.lombok") version "9.2.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "ac.eva"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

    // cloud
    listOf(
        "org.incendo:cloud-core",
        "org.incendo:cloud-annotations",
    ).forEach {
        bukkitLibrary("$it:2.0.0")
    }
    bukkitLibrary("org.incendo:cloud-paper:2.0.0-beta.15")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

bukkit {
    description = "plugin manager supporting paper plugins"
    authors = listOf("xyzeva")
    main = "ac.eva.cork.CorkPlugin"
    foliaSupported = true
    apiVersion = "1.21"
}
