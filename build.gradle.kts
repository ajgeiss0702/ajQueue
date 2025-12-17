plugins {
    java
    id("com.gradleup.shadow").version("9.3.0")
    `maven-publish`
}

repositories {
    maven { url = uri("https://repo.ajg0702.us/releases/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }

    mavenCentral()
}

allprojects {
    version = "2.8.0"
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
    testImplementation("junit:junit:4.12")

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
