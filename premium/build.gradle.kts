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
    implementation(project(":free"))

    compileOnly(project(":api"))
    compileOnly(project(":common"))

    compileOnly("com.google.guava:guava:30.1.1-jre")

    compileOnly("us.ajg0702:ajUtils:1.1.6")

    compileOnly("net.kyori:adventure-api:4.8.1")

    compileOnly("net.luckperms:api:5.0")
}

tasks.shadowJar {
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