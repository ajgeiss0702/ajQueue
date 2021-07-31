plugins {
    java
    id("com.github.johnrengelman.shadow").version("6.1.0")
    `maven-publish`
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.ajg0702.us") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

allprojects {
    version = "2.0.0-pre3"
    group = "us.ajg0702"

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()

        ignoreFailures = false
        failFast = true
        maxParallelForks = (Runtime.getRuntime().availableProcessors() - 1).takeIf { it > 0 } ?: 1

        reports.html.isEnabled = false
        reports.junitXml.isEnabled = false
    }


}


dependencies {
    testImplementation("junit:junit:4.12")

    implementation(project(":free"))
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
