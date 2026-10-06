import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

dependencies {
    labyProcessor()
    api(project(":api"))

    // windowsmediacontrol.dll signed with our certificate, Smart App Control blocks the unsigned one
    addonMavenDependency("net.labymod.signed.com.github.LabyStudio:java-spotify-api:1.5.4") {
        exclude("com.google.code.gson")
        exclude("net.java.dev.jna")
    }
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}
