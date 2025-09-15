import com.diffplug.spotless.LineEnding
import com.github.gradle.node.npm.task.NpxTask
import org.springframework.boot.gradle.tasks.bundling.BootJar

// サービス名
val serviceTags = listOf(
    "Resource",
    "User",
    "Setting",
    "File",
    "Salary",
    "Qualification"
).joinToString(",")

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.21"
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.openapi.generator") version "7.14.0"
    id("com.github.node-gradle.node") version "7.0.2"
    id("com.diffplug.spotless") version "6.25.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
    implementation("software.amazon.awssdk:cognitoidentityprovider:2.32.33")
    implementation("software.amazon.awssdk:dynamodb:2.32.33")
    implementation("software.amazon.awssdk:dynamodb-enhanced:2.32.33")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

// Jarファイル名を指定
tasks.named<BootJar>("bootJar") {
    archiveFileName.set("app.jar")
}

// Node.js設定
node {
    download.set(true)
    version.set("22.15.0")
}

// OpenAPIバンドルタスク
val bundledFile = layout.buildDirectory.file("openapi-bundled.yaml")
tasks.register<NpxTask>("bundleOpenApi") {
    command.set("@redocly/cli")
    args.set(
        listOf(
            "bundle",
            "${rootProject.projectDir.parentFile}/openapi/openapi.yaml",
            "-o",
            bundledFile.get().asFile.absolutePath,
        ),
    )
}

// OpenAPIコード生成タスク
openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set(bundledFile.get().asFile.absolutePath)
    outputDir.set(
        layout.buildDirectory.dir("generated").get().asFile.path,
    )

    // 共通オプション
    globalProperties.set(
        mapOf(
            "apis" to serviceTags,
            "models" to "",
            "apiDocs" to "false",
            "modelDocs" to "false",
            "supportingFiles" to "false",
        ),
    )

    // Kotlin/Spring用の主要オプション
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useTags" to "true",
            "useSpringBoot3" to "true",
            "sourceFolder" to "src/main/kotlin",
            "packageName" to "com.github.moriakira.jibundashboard.generated",
            "apiPackage" to "com.github.moriakira.jibundashboard.generated.api",
            "modelPackage" to "com.github.moriakira.jibundashboard.generated.model",
            "hideGenerationTimestamp" to "true",
        ),
    )
}

// 生成コードをコンパイル対象に追加
kotlin {
    sourceSets {
        val main by getting
        main.kotlin.srcDir(layout.buildDirectory.dir("generated/src/main/kotlin"))
    }
}

// Spotless設定
spotless {
    // 成果物のフォーマット
    kotlin {
        target("src/**/*.kt")
        ktlint("1.3.1").editorConfigOverride(
            mapOf(
                // ワイルドカードimportを許可
                "ktlint_standard_no-wildcard-imports" to "disabled",
                // 長い行の警告をOFF
                "ktlint_standard_max-line-length" to "off",
            ),
        )
        endWithNewline()
        trimTrailingWhitespace()
        lineEndings = LineEnding.UNIX
    }
    // 生成コードのフォーマット
    format("kotlinGenerated") {
        target("build/generated/**/*.kt")
        trimTrailingWhitespace()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
}

detekt {
    config.setFrom(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = false
    parallel = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// OpenAPIバンドル → OpenAPI Generator → 生成コードのフォーマット → ビルドの順でタスクが行われるように
tasks.named("openApiGenerate") {
    dependsOn("bundleOpenApi")
}
tasks.named("spotlessKotlinGenerated") {
    dependsOn("openApiGenerate")
}
val prepareGeneratedSources = tasks.register("prepareGeneratedSources") {
    dependsOn("openApiGenerate")
    dependsOn("spotlessKotlinGeneratedApply")
}
tasks.named("compileKotlin") {
    dependsOn(prepareGeneratedSources)
}
tasks.named("check") {
    dependsOn(prepareGeneratedSources)
}

/* === 以下、ローカル環境用 === */
val composeFile = File("$rootDir/local/docker-compose.local.yml")
val tfDir = File("$rootDir/local/terraform")
val seedDir = File("$rootDir/local/seed")

tasks.register<Exec>("dynamodbDockerUp") {
    group = "local-dynamodb"
    commandLine("docker", "compose", "-f", composeFile.absolutePath, "up", "-d")
}

tasks.register<Exec>("dynamodbDockerDown") {
    group = "local-dynamodb"
    commandLine("docker", "compose", "-f", composeFile.absolutePath, "down", "-v")
}

tasks.register<Exec>("dynamodbTfInit") {
    group = "local-dynamodb"
    workingDir(tfDir)
    commandLine("terraform", "init")
}

tasks.register<Exec>("dynamodbTfApply") {
    group = "local-dynamodb"
    workingDir(tfDir)
    commandLine("terraform", "apply", "-auto-approve")
    dependsOn("dynamodbTfInit")
}

tasks.register<Exec>("dynamodbTfDestroy") {
    group = "local-dynamodb"
    workingDir(tfDir)
    commandLine("terraform", "destroy", "-auto-approve")
}

tasks.register("dynamodbUp") {
    group = "local-dynamodb"
    dependsOn("dynamodbDockerUp", "dynamodbTfApply")
}

tasks.register("dynamodbDown") {
    group = "local-dynamodb"
    dependsOn("dynamodbDockerDown")
}

fun registerSeedTaskFor(file: File) = tasks.register<Exec>("seed_" + file.nameWithoutExtension) {
    group = "local-dynamodb"
    environment("AWS_ACCESS_KEY_ID", "dummy")
    environment("AWS_SECRET_ACCESS_KEY", "dummy")
    environment("AWS_REGION", "ap-northeast-1")
    commandLine(
        "aws",
        "dynamodb",
        "batch-write-item",
        "--endpoint-url",
        "http://localhost:8000",
        "--request-items",
        "file://${file.absolutePath}"
    )
    isIgnoreExitValue = false
}

val seedFiles = seedDir.listFiles { f -> f.isFile && f.extension == "json" }?.sortedBy { it.name } ?: emptyList()
val perTableSeedTasks = seedFiles.map { registerSeedTaskFor(it) }

tasks.register("dynamodbSeedAll") {
    group = "local-dynamodb"
    dependsOn(perTableSeedTasks)
}

tasks.named("dynamodbUp") {
    finalizedBy("dynamodbSeedAll")
}
