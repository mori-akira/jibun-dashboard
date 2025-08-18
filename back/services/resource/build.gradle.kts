import com.diffplug.spotless.LineEnding

// サービス名
val serviceName = "resource"
val serviceNameTag = "Resource"

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.openapi.generator")
    id("com.github.node-gradle.node")
    id("com.diffplug.spotless")
    id("io.gitlab.arturbosch.detekt")
    id("com.gradleup.shadow")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.amazonaws.serverless:aws-serverless-java-container-springboot3:2.1.4")
    implementation("software.amazon.awssdk:dynamodb:2.25.64")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    implementation(project(":libs:common"))

    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

// Node.js設定
node {
    download.set(true)
    version.set("22.15.0")
}

// OpenAPIバンドルタスク
val bundledFile = layout.buildDirectory.file("openapi-bundled.yaml")
tasks.register<com.github.gradle.node.npm.task.NpxTask>("bundleOpenApi") {
    command.set("@redocly/cli")
    args.set(
        listOf(
            "bundle",
            "${rootProject.projectDir.parentFile}/openapi/openapi.yaml",
            "-o",
            bundledFile.get().asFile.absolutePath
        )
    )
}

// OpenAPIコード生成タスク
openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set(bundledFile.get().asFile.absolutePath)
    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.path)

    // Resourceタグだけ生成
    globalProperties.set(
        mapOf(
            "apis" to serviceNameTag,
            "models" to "",
            "apiDocs" to "false",
            "modelDocs" to "false",
            "supportingFiles" to "false"
        )
    )

    // Kotlin/Spring用の主要オプション
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useTags" to "true",
            "useSpringBoot3" to "true",
            "sourceFolder" to "src/main/kotlin",
            "packageName" to "com.github.moriakira.jibundashboard.resource.generated",
            "apiPackage" to "com.github.moriakira.jibundashboard.resource.generated.api",
            "modelPackage" to "com.github.moriakira.jibundashboard.resource.generated.model",
            "hideGenerationTimestamp" to "true"
        )
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
                "ktlint_standard_max-line-length" to "off"
            )
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

// シャドーJAR関連
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveFileName.set("${serviceName}.jar")
    mergeServiceFiles()
    exclude("org/apache/tomcat/**")
}

tasks.named("build") {
    dependsOn("shadowJar")
}
