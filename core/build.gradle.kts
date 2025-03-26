import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

dependencies {
    labyProcessor()
    api(project(":api"))

    addonMavenDependency("com.github.LabyStudio:java-spotify-api:1.2.1") {
        exclude("com.google.code.gson")
        exclude("net.java.dev.jna")
    }
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}