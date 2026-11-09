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
    "Qualification",
    "Vocabulary",
    "Cardbook",
    "Share",
    "SharedLink"
).joinToString(",")

plugins {
    kotlin("jvm") version "2.3.21"
    id("org.jetbrains.kotlin.plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.14.0"
    id("com.github.node-gradle.node") version "7.0.2"
    id("com.diffplug.spotless") version "6.25.0"
    id("dev.detekt") version "2.0.0-alpha.6"
    id("jacoco")
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
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
    implementation("software.amazon.awssdk:cognitoidentityprovider:2.32.33")
    implementation("software.amazon.awssdk:dynamodb:2.32.33")
    implementation("software.amazon.awssdk:dynamodb-enhanced:2.32.33")
    implementation("software.amazon.awssdk:s3:2.32.33")
    implementation("software.amazon.awssdk:sqs:2.32.33")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

// AWS SDK は同期クライアントのみ使用（HTTP は apache-client）。非同期用の Netty は不要なため除外する
configurations.all {
    exclude(group = "software.amazon.awssdk", module = "netty-nio-client")
}

// Jarファイル名を指定
tasks.named<BootJar>("bootJar") {
    archiveFileName.set("app.jar")
}

// Node.js設定
node {
    download.set(true)
    version.set("24.22.2")
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
        ktlint("1.8.0").editorConfigOverride(
            mapOf(
                // ワイルドカードimportを許可
                "ktlint_standard_no-wildcard-imports" to "disabled",
                // detekt の MaxLineLength（既定 120）に合わせる
                // ktlint 1.x では max-line-length ルールを無効にすると他ルールの折り返し判定も無制限になるため、無効化しない
                "max_line_length" to "120",
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

// detekt は自身のビルドに使われた Kotlin でしか起動しないため、detekt 用の Kotlin だけを固定する
// https://detekt.dev/docs/gettingstarted/gradle#dependencies
configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("2.4.10")
        }
    }
}

// テスト設定（Kotest は JUnit Platform 上で動作する）
tasks.named<Test>("test") {
    useJUnitPlatform()
    // MockK の静的モック（mockkStatic(UUID/OffsetDateTime)）が JDK 内部にリフレクションするため対象パッケージを開放
    jvmArgs(
        "--add-opens", "java.base/java.util=ALL-UNNAMED",
        "--add-opens", "java.base/java.time=ALL-UNNAMED",
    )
}

// Jacoco 設定とレポート
jacoco {
    toolVersion = "0.8.12"
}

// 生成コードや自動生成パッケージを除外
val jacocoExcludes = listOf(
    "com/github/moriakira/jibundashboard/generated/**",
)

// jacocoTestReport は jacoco プラグインが提供する既存タスクを設定する
tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn("test")
    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }
    classDirectories.setFrom(
        files(
            classDirectories.files.map { dir ->
                fileTree(dir) { exclude(jacocoExcludes) }
            },
        ),
    )
    sourceDirectories.setFrom(files("src/main/kotlin"))
    executionData.setFrom(files(layout.buildDirectory.file("jacoco/test.exec")))
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

tasks.register<Exec>("localDockerUp") {
    group = "local-env"
    commandLine("docker", "compose", "-f", composeFile.absolutePath, "up", "-d")
}

tasks.register<Exec>("localDockerDown") {
    group = "local-env"
    commandLine("docker", "compose", "-f", composeFile.absolutePath, "down", "-v")
}

tasks.register<Exec>("localTfInit") {
    group = "local-env"
    workingDir(tfDir)
    commandLine("terraform", "init")
}

tasks.register<Exec>("localTfApply") {
    group = "local-env"
    workingDir(tfDir)
    commandLine("terraform", "apply", "-auto-approve")
    dependsOn("localTfInit")
}

tasks.register<Exec>("localTfDestroy") {
    group = "local-env"
    workingDir(tfDir)
    commandLine("terraform", "destroy", "-auto-approve")
}

tasks.register("localEnvUp") {
    group = "local-env"
    dependsOn("localDockerUp", "localTfApply")
}

tasks.register("localEnvDown") {
    group = "local-env"
    dependsOn("localDockerDown")
}

fun registerSeedTaskFor(file: File) = tasks.register<Exec>("seed_" + file.nameWithoutExtension) {
    group = "local-env"
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
    group = "local-env"
    dependsOn(perTableSeedTasks)
}

tasks.named("localEnvUp") {
    finalizedBy("dynamodbSeedAll")
}
