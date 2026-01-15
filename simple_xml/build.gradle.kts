import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("org.jetbrains.kotlinx.kover") version "0.9.3" // UnitTest
    id("org.jetbrains.dokka") version "2.1.0" // Dokka - Document
}

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

    lint {
        // lint.xml 파일을 AAR에 포함시키기 위한 설정
        lintConfig = file("src/main/lint.xml")
    }
}

dependencies {
    // Core module dependency
    implementation(project(":simple_core"))

    // UI dependencies
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle (XML에서 필요한 추가 라이프사이클)
    implementation(libs.androidx.lifecycle.process)

    // Dokka - Document
    dokkaPlugin("org.jetbrains.dokka:android-documentation-plugin:2.1.0")

    // Test
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
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

                // 2. 베이스 컴포넌트(단위 테스트 어려움)
                classes("kr.open.library.simple_ui.xml.ui.activity.RootActivity*")
                classes("kr.open.library.simple_ui.xml.ui.activity.BaseActivity*")
                classes("kr.open.library.simple_ui.xml.ui.activity.BaseBindingActivity*")
                classes("kr.open.library.simple_ui.xml.ui.fragment.RootFragment*")
                classes("kr.open.library.simple_ui.xml.ui.fragment.BaseFragment*")
                classes("kr.open.library.simple_ui.xml.ui.fragment.BaseBindingFragment*")
                classes("kr.open.library.simple_ui.xml.ui.fragment.dialog.RootDialogFragment*")
                classes("kr.open.library.simple_ui.xml.ui.fragment.dialog.BaseDialogFragment*")
                classes("kr.open.library.simple_ui.xml.ui.fragment.dialog.BaseBindingDialogFragment*")
                classes("kr.open.library.simple_ui.xml.system_manager.controller.window.*")
                // 3. Lifecycle 커스텀 Layout
                classes("kr.open.library.simple_ui.core.logcat.extensions.LogxExtensions*")
                classes("kr.open.library.simple_ui.core.extensions.display.DisplayUnitExtensions*")
                classes("kr.open.library.simple_ui.core.system_manager.controller.alarm.*")
                classes("kr.open.library.simple_ui.xml.permissions.register.*")
            }
        }
    }
}

// simple_xml 모듈 Dokka 설정
// - ./gradlew :simple_xml:dokkaHtml 실행 시 사용
tasks.dokkaHtml {
    // 생성되는 Dokka HTML 문서 상단에 표시할 모듈 이름
    moduleName.set("Simple UI XML")

    dokkaSourceSets {
        named("main") {
            // GitHub 소스 코드와 연결 (문서에서 "소스 보기" 링크를 생성)
            sourceLink {
                // 로컬 소스 디렉터리 경로
                localDirectory.set(file("src/main/java"))
                // GitHub 리포지토리의 대응 경로 (브랜치/디렉터리 구조가 일치해야 함)
                remoteUrl.set(
                    uri("https://github.com/Rhpark/Simple_UI_XML/tree/master/simple_xml/src/main/java").toURL(),
                )
                // 특정 라인으로 이동하기 위한 suffix 형식
                remoteLineSuffix.set("#L")
            }

            // Android 공식 레퍼런스와 연결 (예: Activity, Fragment 등 타입에 대한 링크 자동 생성)
            externalDocumentationLink {
                url.set(uri("https://developer.android.com/reference/").toURL())
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
