plugins {
    `java-library`
    `maven-publish`
}

group = "us.ajg0702.queue.platforms.bungeecord"

repositories {
    //mavenLocal()
    maven { url = uri("https://repo.ajg0702.us/releases/") }
    maven { url = uri("https://repo.viaversion.com/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    mavenCentral()
}

dependencies {
    compileOnly("net.kyori:adventure-api:4.15.0")
    compileOnly("com.google.guava:guava:30.1.1-jre")
    compileOnly("us.ajg0702:ajUtils:1.2.37")

    compileOnly("net.md-5:bungeecord-api:1.21-R0.3")

    implementation("net.kyori:adventure-text-minimessage:4.15.0")

    implementation("net.kyori:adventure-platform-bungeecord:4.3.3")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.15.0")

    compileOnly("com.viaversion:viaversion-api:4.3.1")

    implementation("org.bstats:bstats-bungeecord:3.0.0")

    implementation(project(":common"))
    implementation(project(":api"))
}


tasks.withType<ProcessResources> {
    from(sourceSets.main.get().java.srcDirs)
    filter<org.apache.tools.ant.filters.ReplaceTokens>(
        "tokens" to mapOf(
            "VERSION" to project.version.toString()
        )
    ).into("${layout.buildDirectory.asFile.get()}/src")
}

tasks.jar {
    exclude("**/*.java")
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