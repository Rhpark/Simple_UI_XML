import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("org.jetbrains.kotlinx.kover") version "0.9.3" //UnitTest
    id("org.jetbrains.dokka") version "2.1.0" //Dokka - Document
}

publishing {
    publications {
        register("release", MavenPublication::class) {
            groupId = "com.github.Rhpark"
            artifactId = "Simple_UI_Core"
            version = libs.versions.appVersion.get()

            afterEvaluate {
                from(components.findByName("release"))
            }
        }
    }
}

android {
    namespace = "kr.open.library.simple_ui.core"
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
                "proguard-rules.pro"
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
    // Core dependencies only (no UI)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.process)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    //Dokka - Document
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

// Dokka Configuration for simple_core module
// simple_core 모듈 Dokka 설정
tasks.dokkaHtml {
    moduleName.set("Simple UI Core")

    dokkaSourceSets {
        named("main") {
            sourceLink {
                localDirectory.set(file("src/main/java"))
                remoteUrl.set(uri("https://github.com/Rhpark/Simple_UI_XML/tree/master/simple_core/src/main/java").toURL())
                remoteLineSuffix.set("#L")
            }

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
