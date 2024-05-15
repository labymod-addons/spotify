plugins {
    id("java-library")
}

dependencies {
    api(project(":api"))

    maven("https://jitpack.io/", "com.github.LabyStudio:java-spotify-api:1.2.0")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.DEFAULT
}

tasks.compileJava {
    sourceCompatibility = JavaVersion.VERSION_21.toString()
    targetCompatibility = JavaVersion.VERSION_21.toString()
}