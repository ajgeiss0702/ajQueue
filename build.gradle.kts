plugins {
    java
    id("com.github.johnrengelman.shadow").version("6.1.0")
}

group = "us.ajg0702"
version = "1.9.0"

repositories {
    mavenCentral()

    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://gitlab.com/api/v4/projects/19978391/packages/maven") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("http://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }

}

dependencies {
    compileOnly("com.github.MyzelYam:PremiumVanishAPI:2.0.3")
    compileOnly("net.md-5:bungeecord-api:1.14-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.16.4-RO.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.10.4")

    implementation("us.ajg0702:ajUtils:1.0.0")
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
    archiveFileName.set("${baseName}-${version}.${extension}")
}
