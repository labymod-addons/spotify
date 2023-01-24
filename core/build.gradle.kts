plugins {
    id("java-library")
}

dependencies {
    api(project(":api"))

    maven("https://jitpack.io/", "com.github.LabyStudio:java-spotify-api:1.1.5:all")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.DEFAULT
}

tasks.compileJava {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}