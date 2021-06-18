plugins {
    java
    id("com.github.johnrengelman.shadow").version("6.1.0")
    `maven-publish`
}

group = "us.ajg0702"
version = "1.9.7"

repositories {
    mavenCentral()
    mavenLocal()

    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://gitlab.com/api/v4/projects/19978391/packages/maven") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://repo.codemc.io/repository/nms/") }

}

dependencies {
    compileOnly("com.github.MyzelYam:PremiumVanishAPI:2.0.3")
    compileOnly("net.md-5:bungeecord-api:1.14-SNAPSHOT")
    compileOnly(group = "org.spigotmc", name = "spigot", version = "1.16.5-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.10.4")

    implementation("us.ajg0702:ajUtils:1.0.4")
    implementation("org.bstats:bstats-bungeecord:2.2.1")
}


tasks.withType<ProcessResources> {
    include("**/*.yml")
    filter<org.apache.tools.ant.filters.ReplaceTokens>(
            "tokens" to mapOf(
                    "VERSION" to project.version.toString()
            )
    )
}

tasks.shadowJar {
    relocate("us.ajg0702.utils", "us.ajg0702.queue.utils")
    relocate("org.bstats", "us.ajg0702.bstats")
    relocate("net.kyori", "us.ajg0702.queue.kyori")
    archiveFileName.set("${baseName}-${version}.${extension}")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["jar"])
        }
    }

    repositories {

        val mavenUrl = "https://repo.ajg0702.us/releases"

        if(!System.getenv("REPO_TOKEN").isNullOrEmpty()) {
            maven {
                url = uri(mavenUrl)
                name = "ajRepo"

                credentials {
                    username = "plugins"
                    password = System.getenv("REPO_TOKEN")
                }
            }
        }
    }
}
