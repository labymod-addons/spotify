import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

dependencies {
    labyProcessor()
    api(project(":api"))

    addonMavenDependency("com.github.LabyStudio:java-spotify-api:1.2.0")
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}