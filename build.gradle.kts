plugins {
    java
    id("com.github.johnrengelman.shadow").version("7.1.2")
    `maven-publish`
}

repositories {
    maven { url = uri("https://repo.ajg0702.us") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }

    mavenCentral()
}

allprojects {
    version = "2.2.9"
    group = "us.ajg0702"

    plugins.apply("java")
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()

        ignoreFailures = false
        failFast = true
        maxParallelForks = (Runtime.getRuntime().availableProcessors() - 1).takeIf { it > 0 } ?: 1

        reports.html.required.set(false)
        reports.junitXml.required.set(false)
    }


}


dependencies {
    testImplementation("junit:junit:4.13.1")

    implementation(project(":free"))
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["shadowJar"])
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
