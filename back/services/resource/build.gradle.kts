import com.diffplug.spotless.LineEnding

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.openapi.generator")
    id("com.github.node-gradle.node")
    id("com.diffplug.spotless")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.amazonaws.serverless:aws-serverless-java-container-springboot3:2.0.2")
    implementation("software.amazon.awssdk:dynamodb:2.25.64")

    implementation(project(":libs:common"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// Lambdaのビルド設定
tasks.register<Zip>("bundleLambda") {
    dependsOn(tasks.named("bootJar"))
    val jar = layout.buildDirectory.file("libs/${project.name}-${project.version}.jar")
    from(jar)
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    archiveFileName.set("resource.zip")
}

// Node.js設定
node {
    download.set(true)
    version.set("22.15.0")
}

// OpenAPIバンドルタスク
val bundledPath: String = layout.buildDirectory.file("openapi-bundled.yaml").get().asFile.absolutePath
tasks.register<com.github.gradle.node.npm.task.NpxTask>("bundleOpenApi") {
    command.set("@redocly/cli")
    args.set(listOf(
        "bundle",
        "${rootProject.projectDir.parentFile}/openapi/openapi.yaml",
        "-o",
        bundledPath
    ))
}

// OpenAPIコード生成タスク
openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set(bundledPath)
    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.path)

    // Resourceタグだけ生成
    globalProperties.set(
        mapOf(
            "apis" to "Resource",
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
        target(
            "src/**/*.kt",
        )
        ktlint("1.3.1")
            .editorConfigOverride(
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

// コンパイル前にSpotlessApplyを実行
tasks.named("compileKotlin") {
    dependsOn("spotlessApply")
}
// SpotlessApply前にOpenAPIコード生成を実行
tasks.named("spotlessApply") {
    dependsOn("openApiGenerate")
}
tasks.named("spotlessKotlinGenerated") {
    dependsOn("openApiGenerate")
}
// OpenAPIコード生成前にバンドルを実行
tasks.named("openApiGenerate") {
    dependsOn("bundleOpenApi")
}
