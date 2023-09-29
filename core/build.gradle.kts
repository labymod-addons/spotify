plugins {
    id("java-library")
}

dependencies {
    api(project(":api"))

    maven("https://jitpack.io/", "com.github.LabyStudio:java-spotify-api:1.1.16")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.DEFAULT
}

tasks.compileJava {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}