import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.8"
    id("net.kyori.blossom").version("1.3.1")
    id("java-library")
    id("xyz.kyngs.libby.plugin").version("1.2.1")
    id("xyz.kyngs.mcupload.plugin").version("0.3.4")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

mcupload {
    file = tasks.shadowJar
    swallowErrors = true
    platforms {
        modrinth {
            loaders = listOf("paper", "purpur", "velocity")
            projectId = "tL0SCXYq"
            gameVersions = listOf(
                "1.21.10", "1.21.9", "1.21.8", "1.21.7", "1.21.6", "1.21.5", "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21",
                "1.20.6", "1.20.5", "1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20",
                "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19",
                "1.18.2", "1.18.1", "1.18",
                "1.17.1", "1.17",
                "1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1", "1.16",
                "1.15.2", "1.15.1", "1.15",
                "1.14.4", "1.14.3", "1.14.2", "1.14.1", "1.14",
                "1.13.2", "1.13.1", "1.13",
            )
            token = System.getenv("MODRINTH_TOKEN")
        }
        polymart {
            apiKey = System.getenv("POLYMART_TOKEN")
            resourceId = "2179"
        }
        github {
            token = System.getenv("GITHUB_TOKEN")
            repository = "xreatlabs/NexAuth"
        }
        discord {
            webhookUrl = System.getenv("DISCORD_WEBHOOK_URL")
            configureEmbed {
                setColor(0x0398FC)
            }
        }
    }
    datasource {
        file {
            readmeFile = "README.md"
            changelogFile = "CHANGELOG.md"
        }
    }
}

repositories {
    // mavenLocal()
    maven { url = uri("https://repo.opencollab.dev/maven-snapshots/") }
    maven { url = uri("https://repo.opencollab.dev/maven-releases/") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/") }
    maven { url = uri("https://repo.kyngs.xyz/public/") }
    maven { url = uri("https://mvn.exceptionflug.de/repository/exceptionflug-public/") }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://repo.alessiodp.com/releases/") }
    maven { url = uri("https://jitpack.io/") }
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
}

blossom {
    replaceToken("@version@", version.toString())
}



tasks.withType<ShadowJar> {
    archiveFileName.set("NexAuth.jar")

    dependencies {
        exclude(dependency("org.slf4j:.*:.*"))
        exclude(dependency("org.checkerframework:.*:.*"))
        exclude(dependency("com.google.errorprone:.*:.*"))
        exclude(dependency("com.google.protobuf:.*:.*"))
    }

    relocate("co.aikar.acf", "xyz.xreatlabs.nexauth.lib.acf")
    relocate("com.github.benmanes.caffeine", "xyz.xreatlabs.nexauth.lib.caffeine")
    relocate("com.typesafe.config", "xyz.xreatlabs.nexauth.lib.hocon")
    relocate("com.zaxxer.hikari", "xyz.xreatlabs.nexauth.lib.hikari")
    relocate("org.mariadb", "xyz.xreatlabs.nexauth.lib.mariadb")
    relocate("org.bstats", "xyz.xreatlabs.nexauth.lib.metrics")
    relocate("org.intellij", "xyz.xreatlabs.nexauth.lib.intellij")
    relocate("org.jetbrains", "xyz.xreatlabs.nexauth.lib.jetbrains")
    relocate("io.leangen.geantyref", "xyz.xreatlabs.nexauth.lib.reflect")
    relocate("net.kyori.option", "xyz.xreatlabs.nexauth.lib.option")
    relocate("org.spongepowered.configurate", "xyz.xreatlabs.nexauth.lib.configurate")
    relocate("net.byteflux.libby", "xyz.xreatlabs.nexauth.lib.libby")
    relocate("org.postgresql", "xyz.xreatlabs.nexauth.lib.postgresql")
    relocate("com.github.retrooper.packetevents", "xyz.xreatlabs.nexauth.lib.packetevents.api")
    relocate("io.github.retrooper.packetevents", "xyz.xreatlabs.nexauth.lib.packetevents.platform")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Ensure normal builds produce the shaded jar and avoid confusion with a thin jar
tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named("assemble") {
    dependsOn(tasks.shadowJar)
}

tasks.named("build") {
    dependsOn(tasks.shadowJar)
}

tasks.withType<Jar> {
    from("../LICENSE.txt")
}

libby {
    excludeDependency("org.slf4j:.*:.*")
    excludeDependency("org.checkerframework:.*:.*")
    excludeDependency("com.google.errorprone:.*:.*")
    excludeDependency("com.google.protobuf:.*:.*")

    // Often redeploys the same version, so calculating checksum causes false flags
    noChecksumDependency("com.github.retrooper.packetevents:.*:.*")
}

configurations.all {
    // Apply only to packaged runtime configurations. Tests must use the same Adventure API selected
    // by the Paper/Velocity compile classpath or compiled main classes fail with linkage errors.
    if (name.contains("runtime", ignoreCase = true)
        && !name.contains("compile", ignoreCase = true)
        && !name.startsWith("test", ignoreCase = true)) {
        resolutionStrategy {
            force("net.kyori:adventure-text-minimessage:4.14.0")
            force("net.kyori:adventure-text-serializer-gson:4.14.0")
            force("net.kyori:adventure-text-serializer-legacy:4.14.0")
            force("net.kyori:adventure-text-serializer-json:4.14.0")
            force("net.kyori:adventure-api:4.14.0")
            force("net.kyori:adventure-nbt:4.14.0")
            force("net.kyori:adventure-key:4.14.0")
        }
    }
}

dependencies {
    //API
    implementation(project(":API"))

    //Velocity
    annotationProcessor("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-proxy:3.5.0-SNAPSHOT")

    //MySQL
    libby("org.mariadb.jdbc:mariadb-java-client:3.5.5")
    libby("com.zaxxer:HikariCP:7.0.1")

    //SQLite
    libby("org.xerial:sqlite-jdbc:3.50.3.0")

    //PostgreSQL
    libby("org.postgresql:postgresql:42.7.7")

    //ACF
    libby("com.github.kyngs.commands:acf-velocity:7d5bf7cac0")
    libby("com.github.kyngs.commands:acf-paper:7d5bf7cac0")

    //Utils
    libby("com.github.ben-manes.caffeine:caffeine:3.2.2")
    libby("org.spongepowered:configurate-hocon:4.2.0")
    libby("org.spongepowered:configurate-yaml:4.2.0")
    libby("at.favre.lib:bcrypt:0.10.2")
    libby("dev.samstevens.totp:totp:1.7.1")
    compileOnly("dev.simplix:protocolize-api:2.4.3")
    libby("org.bouncycastle:bcprov-jdk18on:1.81")
    libby("org.apache.commons:commons-email:1.6.0")
    libby("net.kyori:adventure-text-minimessage:4.14.0")
    libby("com.github.kyngs:LegacyMessage:0.2.0")

    //Geyser
    // Exclude transitive Geyser common: its mavenLocal jar bundles an old un-relocated gson
    // that shadows the real one on the compile classpath and breaks compilation.
    compileOnly("org.geysermc.floodgate:api:2.2.4-SNAPSHOT") {
        exclude(group = "org.geysermc.geyser")
    }
    //LuckPerms
    compileOnly("net.luckperms:api:5.5")

    //Velocity multi-proxy support
    compileOnly("com.github.ProxioDev.ValioBungee:RedisBungee-Velocity:0.13.0")

    //BStats
    libby("org.bstats:bstats-velocity:3.1.0")
    libby("org.bstats:bstats-bukkit:3.1.0")

    //Paper
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    //compileOnly "com.comphenix.protocol:ProtocolLib:5.1.0"
    libby("com.github.retrooper:packetevents-spigot:2.13.0")
    compileOnly("io.netty:netty-all:4.2.2.Final")
    libby("io.netty:netty-all:4.2.2.Final")
    compileOnly("com.mojang:datafixerupper:5.0.28") //I hate this so much
    compileOnly("org.apache.logging.log4j:log4j-core:2.23.1")

    //Libby
    implementation("xyz.kyngs.libby:libby-bukkit:1.7.1")
    implementation("xyz.kyngs.libby:libby-velocity:1.7.1")
    implementation("xyz.kyngs.libby:libby-paper:1.7.1")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.google.code.gson:gson:2.13.1")
    testImplementation("com.github.kyngs.commands:acf-paper:7d5bf7cac0")
    testImplementation("com.github.ben-manes.caffeine:caffeine:3.2.2")
    testImplementation("com.google.guava:guava:33.4.8-jre")
    // Match the Adventure API selected by the current Paper/Velocity compile classpath.
    testImplementation(platform("net.kyori:adventure-bom:5.2.0"))
    testImplementation("net.kyori:adventure-api")
    testImplementation("net.kyori:adventure-nbt")
    testImplementation("net.kyori:adventure-text-serializer-plain")
    testImplementation("org.bouncycastle:bcprov-jdk18on:1.81")
    testImplementation("org.bstats:bstats-base:3.1.0")
    testImplementation("io.netty:netty-all:4.2.2.Final")

    // ArchUnit for architecture boundary tests
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ProcessResources> {
    outputs.upToDateWhen { false }
    filesMatching("plugin.yml") {
        expand(mapOf("version" to version))
    }
    filesMatching("paper-plugin.yml") {
        expand(mapOf("version" to version))
    }
}
