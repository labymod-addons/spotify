plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version ("7.0.0")
}

dependencies {
    api(project(":api"))

    //maven("https://jitpack.io/", "com.github.LabyStudio:java-spotify-api:1.1.16")
    shade(files("../libs/java-spotify-api-1.2.0.jar"))
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.DEFAULT
}

tasks.compileJava {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}

tasks {
    shadowJar {
        archiveBaseName.set("core")

        dependencyFilter.exclude {
            !it.moduleGroup.equals("net.labymod.addons.spotify")
        }
    }

    getByName("jar").finalizedBy("shadowJar")
}
