plugins {
    id("java-library")
    id("maven-publish")
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    api("javax.annotation:javax.annotation-api:1.3.2")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("com.google.guava:guava:33.4.8-jre")
    compileOnly("net.kyori:adventure-api:4.14.0")
    compileOnly("org.jetbrains:annotations:26.0.2")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven {
            name = "xreatlabsRepo"
            url = uri(
                "https://repo.xreatlabs.xyz/" + (if (project.version.toString()
                        .contains("SNAPSHOT")
                ) "snapshots" else "releases") + "/"
            )
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
