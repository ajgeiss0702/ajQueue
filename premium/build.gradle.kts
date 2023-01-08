plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
    `maven-publish`
}

group = "us.ajg0702.queue"

repositories {
    //mavenLocal()
    maven { url = uri("https://repo.ajg0702.us/releases/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    mavenCentral()
}

dependencies {
    implementation(project(":free"))

    compileOnly(project(":api"))
    compileOnly(project(":common"))

    compileOnly("com.google.guava:guava:30.1.1-jre")

    compileOnly("me.TechsCode:FakeUltraPerms:1.0.2")

    compileOnly("us.ajg0702:ajUtils:1.2.10")

    compileOnly("net.kyori:adventure-api:4.9.3")

    compileOnly(fileTree(mapOf("dir" to "../libs/private", "include" to listOf("*.jar"))))
    compileOnly(fileTree(mapOf("dir" to "../libs/public", "include" to listOf("*.jar"))))

    compileOnly("net.luckperms:api:5.4")
}

tasks.shadowJar {
    relocate("us.ajg0702.utils", "us.ajg0702.queue.libs.utils")
    relocate("org.bstats", "us.ajg0702.queue.libs.bstats")
    relocate("io.leangen.geantyref", "us.ajg0702.queue.libs.geantyref")
    relocate("org.spongepowered", "us.ajg0702.queue.libs.sponge")
    relocate("org.yaml", "us.ajg0702.queue.libs.yaml")
    archiveBaseName.set("ajQueuePlus")
    archiveClassifier.set("")
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