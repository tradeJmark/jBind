plugins {
    id("org.jetbrains.kotlin.js") version "1.6.0"
}

group = "ca.tradejmark.jbind"
version = "0.0.1-SNAPSHOT"

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