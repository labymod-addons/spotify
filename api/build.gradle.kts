plugins {
    id("java-library")
}

repositories {
    mavenLocal()
}

dependencies {
    labyApi("api")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.INTERFACE
}

tasks.compileJava {
    sourceCompatibility = JavaVersion.VERSION_21.toString()
    targetCompatibility = JavaVersion.VERSION_21.toString()
}