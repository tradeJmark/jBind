import org.jetbrains.kotlin.gradle.plugin.statistics.ReportStatisticsToElasticSearch.url

plugins {
    id("org.jetbrains.kotlin.js") version "1.6.0"
    `maven-publish`
    id("dev.petuska.npm.publish") version "2.1.1"
}

group = "ca.tradejmark.jbind"
version = "0.0.1"

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/tradeJmark/jBind")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

npmPublishing {
    organization = "tradeJmark"
    repositories {
        repository("GitHubPackages") {
            registry = uri("https://npm.pkg.github.com")
            authToken = System.getenv("GITHUB_TOKEN")
        }
    }
}

repositories {
    mavenCentral()
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC")
    implementation(kotlin("stdlib-js"))
    api("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")

    testImplementation(kotlin("test-js"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0-RC")
}

kotlin {
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
}