import java.io.FileInputStream
import java.util.Properties

val localProperties = Properties().apply {
    val file = File(rootProject.rootDir, "local.properties")
    if (file.exists()) load(FileInputStream(file))
}

val githubToken: String? = localProperties["githubToken"]?.toString()
val githubActor: String? = localProperties["githubActor"]?.toString()

plugins {
    kotlin("multiplatform") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"
    `maven-publish`
}

group = "ca.tradejmark.jbind"
version = "0.0.2-SNAPSHOT"

repositories {
    mavenCentral()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/tradeJmark/jBind")
            credentials {
                username = githubActor ?: System.getenv("GITHUB_ACTOR")
                password = githubToken ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

kotlin {
    jvm()
    js(IR) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC")
                api("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0-RC")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-core:1.6.5")
                implementation("io.ktor:ktor-websockets:1.6.5")
                implementation("ch.qos.logback:logback-classic:1.2.6")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("io.ktor:ktor-server-test-host:1.6.5")
            }
        }
    }
}