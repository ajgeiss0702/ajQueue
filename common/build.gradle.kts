plugins {
    `java-library`
    `maven-publish`
}

group = "us.ajg0702.queue.common"

repositories {
    //mavenLocal()
    maven { url = uri("https://repo.ajg0702.us/releases/") }
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.12")

    compileOnly("net.kyori:adventure-api:4.15.0")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.15.0")

    compileOnly("com.google.guava:guava:30.1.1-jre")
    compileOnly("us.ajg0702:ajUtils:1.2.28")

    compileOnly("org.slf4j:slf4j-log4j12:1.7.29")

    compileOnly("org.spongepowered:configurate-yaml:4.0.0")

    implementation(project(":api"))
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