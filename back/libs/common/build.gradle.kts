plugins {
    kotlin("jvm") version "2.0.0"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
}

tasks.test {
    useJUnitPlatform()
}
