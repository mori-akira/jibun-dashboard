plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.0"
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.openapi.generator") version "7.14.0"
    id("com.github.node-gradle.node") version "7.0.2"
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

// コンパイル前にOpenAPIコード生成を実行
tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}
// OpenAPIコード生成前にバンドルを実行
tasks.named("openApiGenerate") {
    dependsOn("bundleOpenApi")
}
