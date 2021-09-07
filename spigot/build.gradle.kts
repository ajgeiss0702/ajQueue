plugins {
    `java-library`
    `maven-publish`
}

group = "us.ajg0702.queue.spigot"

repositories {
    //mavenLocal()


    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }

    maven { url = uri("https://repo.codemc.io/repository/nms/") }

    maven { url = uri("https://repo.ajg0702.us") }

    mavenCentral()
}

dependencies {
    implementation("net.kyori:adventure-api:4.8.1")
    compileOnly("com.google.guava:guava:30.1.1-jre")

    compileOnly("org.spongepowered:configurate-yaml:4.0.0")

    compileOnly("us.ajg0702:ajUtils:1.1.16")

    compileOnly(group = "org.spigotmc", name = "spigot", version = "1.16.5-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.10.4")
}

tasks.withType<ProcessResources> {
    include("**/*.yml")
    filter<org.apache.tools.ant.filters.ReplaceTokens>(
        "tokens" to mapOf(
            "VERSION" to project.version.toString()
        )
    )
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