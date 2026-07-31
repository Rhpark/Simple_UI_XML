import org.gradle.api.tasks.testing.Test
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import java.io.File

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("org.jetbrains.kotlinx.kover") version "0.9.3" // UnitTest
    id("org.jetbrains.dokka") version "2.1.0" // Dokka - Document
    id("com.vanniktech.maven.publish") version "0.32.0" apply false // Maven Central Publish
}

group = libs.versions.githubGroup.get()
version = libs.versions.appVersion.get()
val enableJitpackPublication = providers
    .gradleProperty("enableJitpackPublication")
    .orElse(providers.environmentVariable("JITPACK"))
    .map { it.equals("true", ignoreCase = true) }
    .getOrElse(false)

if (enableJitpackPublication) {
    publishing {
        publications {
            register("release", MavenPublication::class) {
                groupId = "com.github.Rhpark"
                artifactId = "Simple_UI_XML"
                version = libs.versions.appVersion.get()
                afterEvaluate {
                    from(components.findByName("release"))
                }
            }
        }
    }
} else {
    apply(plugin = "com.vanniktech.maven.publish")

    extensions.configure<MavenPublishBaseExtension> {
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
        signAllPublications()

        coordinates(
            libs.versions.githubGroup.get(),
            libs.versions.mavenArtifactIdXML.get(),
            libs.versions.appVersion.get()
        )

        pom {
            name.set(libs.versions.mavenArtifactIdXML.get())
            description.set("Android XML UI components, bindings, adapters, extensions, and permission request helpers.")
            url.set(libs.versions.githubUrl.get())

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set(libs.versions.githubId.get())
                    name.set("RH Park")
                    email.set(libs.versions.email.get())
                }
            }

            scm {
                url.set(libs.versions.githubUrl.get())
                connection.set(libs.versions.githubScmConnection.get())
                developerConnection.set(libs.versions.githubScmDeveloperConnection.get())
            }
        }
    }
}

android {
    namespace = "kr.open.library.simple_ui.xml"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        //noinspection DataBindingWithoutKapt
        dataBinding = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                val currentArgs = it.jvmArgs ?: emptyList()
                it.jvmArgs = currentArgs + "-XX:+EnableDynamicAgentLoading"
            }
        }
    }
}

dependencies {
    // 공개 API가 노출하는 Core 의존성
    api(project(":simple_core"))

    // 공개 API가 노출하는 UI 의존성
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.androidx.activity)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.recyclerview)
    api(libs.androidx.lifecycle.common)

    // Lifecycle (XML에서 필요한 추가 라이프사이클)
    implementation(libs.androidx.lifecycle.process)

    // Dokka - Document
    dokkaPlugin("org.jetbrains.dokka:android-documentation-plugin:2.1.0")

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Kover로 UnitTest
kover {
    reports {
        // HTML 리포트 설정 (로컬에서 확인용)
        filters {
            excludes {

                /****************
                 * 자동 파일 생성 *
                 *  테스트 금지   *
                 ****************/
                classes("**.BuildConfig")
                classes("**.R")
                classes("**.R$*")
                classes("**.databinding.**")
                classes("**.Databinding.**")
                classes("**.BR")

                classes("**.DataBinderMapperImpl*") // *로 Inner 클래스까지 함께 제외
                classes("**.DataBindingTriggerClass")
            }
        }
    }
}

// simple_xml 모듈 Dokka 설정
// - ./gradlew :simple_xml:dokkaGenerateHtml 실행 시 사용
dokka {
    dokkaPublications.html {
        // 생성되는 Dokka HTML 문서 상단에 표시할 모듈 이름
        moduleName.set("Simple UI XML")
    }

    dokkaSourceSets {
        named("main") {
            // GitHub 소스 코드와 연결 (문서에서 "소스 보기" 링크를 생성)
            sourceLink {
                // 로컬 소스 디렉터리 경로
                localDirectory.set(file("src/main/java"))
                // GitHub 리포지토리의 대응 경로 (브랜치/디렉터리 구조가 일치해야 함)
                remoteUrl.set(
                    uri("https://github.com/Rhpark/Simple_UI_XML/tree/master/simple_xml/src/main/java"),
                )
                // 특정 라인으로 이동하기 위한 suffix 형식
                remoteLineSuffix.set("#L")
            }
        }
    }
}

// Test tasks
tasks.register<Test>("testUnit") {
    description = "Runs pure unit tests only (no Android dependencies)"
    group = "verification"

    val testDebugTask = tasks.named<Test>("testDebugUnitTest")
    testClassesDirs = testDebugTask.get().testClassesDirs
    classpath = testDebugTask.get().classpath

    include("**/unit/**")
    failFast = true
}

tasks.register<Test>("testRobolectric") {
    description = "Runs Robolectric tests only (Android framework simulation)"
    group = "verification"

    val testDebugTask = tasks.named<Test>("testDebugUnitTest")
    testClassesDirs = testDebugTask.get().testClassesDirs
    classpath = testDebugTask.get().classpath

    include("**/robolectric/**")
    failFast = true
    mustRunAfter("testUnit")
}

tasks.register("testAll") {
    description = "Runs unit tests first, then robolectric tests if unit tests pass"
    group = "verification"

    dependsOn("testUnit", "testRobolectric")
}

// Android 리소스 공개 API 기준선
val releaseResourceSymbols =
    layout.buildDirectory.file("intermediates/compile_symbol_list/release/generateReleaseRFile/R.txt")
val resourceApiBaseline = layout.projectDirectory.file("api/simple_xml-resources.api")

fun readCompiledResourceSymbols(file: File): List<String> =
    file.useLines(Charsets.UTF_8) { lines ->
        lines
            .filter { it.isNotBlank() }
            .map { line ->
                val parts = line.trim().split(Regex("\\s+"), limit = 4)
                require(parts.size >= 3) { "Invalid Android resource symbol: $line" }
                "${parts[1]} ${parts[2]}"
            }.distinct()
            .sorted()
            .toList()
    }

fun readResourceApiBaseline(file: File): List<String> =
    file
        .readLines(Charsets.UTF_8)
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
        .sorted()

val resourceApiDump = tasks.register("resourceApiDump") {
    group = "verification"
    description = "Updates the simple_xml Android resource API baseline"
    dependsOn("generateReleaseRFile")

    inputs.file(releaseResourceSymbols)
    outputs.file(resourceApiBaseline)

    doLast {
        val symbols = readCompiledResourceSymbols(releaseResourceSymbols.get().asFile)
        resourceApiBaseline.asFile.apply {
            parentFile.mkdirs()
            writeText(symbols.joinToString(separator = "\n", postfix = "\n"), Charsets.UTF_8)
        }
    }
}

val resourceApiCheck = tasks.register("resourceApiCheck") {
    group = "verification"
    description = "Checks the simple_xml Android resource API baseline"
    dependsOn("generateReleaseRFile")

    inputs.file(releaseResourceSymbols)
    inputs.file(resourceApiBaseline)

    doLast {
        val expected = readResourceApiBaseline(resourceApiBaseline.asFile)
        val actual = readCompiledResourceSymbols(releaseResourceSymbols.get().asFile)
        if (expected != actual) {
            val expectedSet = expected.toSet()
            val actualSet = actual.toSet()
            val removedOrRenamed = expected.filterNot(actualSet::contains)
            val added = actual.filterNot(expectedSet::contains)
            throw GradleException(
                buildString {
                    appendLine("simple_xml Android resource API differs from its baseline.")
                    if (removedOrRenamed.isNotEmpty()) {
                        appendLine("Removed or renamed resources:")
                        removedOrRenamed.forEach { appendLine("- $it") }
                    }
                    if (added.isNotEmpty()) {
                        appendLine("Added resources:")
                        added.forEach { appendLine("+ $it") }
                    }
                    append("Run :simple_xml:resourceApiDump only when the resource API change is intentional.")
                },
            )
        }
    }
}

tasks.matching { it.name == "apiCheck" }.configureEach {
    dependsOn(resourceApiCheck)
}
