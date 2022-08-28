plugins {
    `java-library`
    `maven-publish`
}

group = "us.ajg0702.queue.platforms.bungeecord"

repositories {
    //mavenLocal()
    maven { url = uri("https://repo.ajg0702.us/releases/") }
    maven { url = uri("https://repo.viaversion.com/") }
    mavenCentral()
}

dependencies {
    compileOnly("net.kyori:adventure-api:4.11.0")
    compileOnly("com.google.guava:guava:31.1-jre")
    compileOnly("us.ajg0702:ajUtils:1.2.10")

    compileOnly("net.md-5:bungeecord-api:1.16-R0.4")

    implementation("net.kyori:adventure-text-minimessage:4.11.0")

    implementation("net.kyori:adventure-platform-bungeecord:4.0.0")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.9.3")

    compileOnly("com.viaversion:viaversion-api:4.4.2")

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
    ).into("$buildDir/src")
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