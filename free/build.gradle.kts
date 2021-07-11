plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
    `maven-publish`
}

group = "us.ajg0702.queue"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.ajg0702.us") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    compileOnly("net.kyori:adventure-api:4.8.1")
    compileOnly("com.google.guava:guava:30.1.1-jre")
    compileOnly("org.spongepowered:configurate-yaml:4.0.0")

    implementation("us.ajg0702:ajUtils:1.1.6")

    //implementation("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT")

    implementation(project(":platforms:velocity"))
}

tasks.shadowJar {
    relocate("us.ajg0702.utils", "us.ajg0702.queue.libs.utils")
    relocate("org.bstats", "us.ajg0702.queue.libs.bstats")
    //relocate("net.kyori", "us.ajg0702.queue.libs.kyori")
    relocate("io.leangen.geantyref", "us.ajg0702.queue.libs.geantyref")
    relocate("org.spongepowered", "us.ajg0702.queue.libs.sponge")
    relocate("org.yaml", "us.ajg0702.queue.libs.yaml")
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