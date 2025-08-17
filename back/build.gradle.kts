plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.0" apply false
    id("org.springframework.boot") version "3.3.1" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    id("org.openapi.generator") version "7.14.0" apply false
    id("com.github.node-gradle.node") version "7.0.2" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

allprojects {
    group = "com.github.moriakira.jibundashboard"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}
