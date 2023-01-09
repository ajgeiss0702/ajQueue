plugins {
    `java-library`
    `maven-publish`
}

group = "us.ajg0702.queue.api"

repositories {
    //mavenLocal()

    maven { url = uri("https://repo.ajg0702.us/releases/") }

    mavenCentral()
}

dependencies {
    implementation("net.kyori:adventure-api:4.12.0")
    implementation("net.kyori:adventure-text-serializer-plain:4.12.0")
    compileOnly("com.google.guava:guava:31.1-jre")

    compileOnly("us.ajg0702:ajUtils:1.2.10")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
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