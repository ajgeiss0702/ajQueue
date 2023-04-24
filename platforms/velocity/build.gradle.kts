plugins {
    `java-library`
    `maven-publish`
}

group = "us.ajg0702.queue.platforms.velocity"

repositories {
    //mavenLocal()
    maven { url = uri("https://repo.ajg0702.us/releases/") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://repo.viaversion.com/") }
    mavenCentral()
}

dependencies {
    compileOnly("net.kyori:adventure-api:4.13.1")
    compileOnly("com.google.guava:guava:30.1.1-jre")
    compileOnly("us.ajg0702:ajUtils:1.2.10")

    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
    implementation("net.kyori:adventure-text-minimessage:4.10.0")

    compileOnly("com.viaversion:viaversion-api:4.3.1")

    implementation("org.bstats:bstats-velocity:3.0.0")

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

tasks.compileJava {
    source = tasks.getByName("processResources").outputs.files.asFileTree
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