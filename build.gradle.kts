import com.diffplug.spotless.LineEnding
import net.labymod.labygradle.common.extension.model.labymod.ReleaseChannels

plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
    id("com.diffplug.spotless") version ("8.0.0")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "org.example"
version = providers.environmentVariable("VERSION").getOrElse("1.0.0")

labyMod {
    defaultPackageName = "net.labymod.addons.spotify" //change this to your main package name (used by all modules)

    minecraft {
        registerVersion(versions.toTypedArray()) {
            runs {
                getByName("client") {
                    devLogin = true
                }
            }
        }
    }

    addonInfo {
        namespace = "spotify"
        displayName = "Spotify"
        author = "LabyMedia GmbH"
        minecraftVersion = "*"
        version = rootProject.version.toString()
        releaseChannel = ReleaseChannels.SNAPSHOT
    }
}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")
    plugins.apply("com.diffplug.spotless")

    group = rootProject.group
    version = rootProject.version

    repositories {
        maven { url = uri("https://jitpack.io") }
    }

    spotless {
        lineEndings = LineEnding.UNIX

        java {
            licenseHeaderFile(rootProject.file("gradle/LICENSE-HEADER.txt"))
        }
    }
}