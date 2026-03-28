import org.gradle.api.tasks.testing.Test
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("org.jetbrains.kotlinx.kover") version "0.9.3"
    id("org.jetbrains.dokka") version "2.1.0"
    id("com.vanniktech.maven.publish") version "0.32.0" apply false // Maven Central Publish
}

group = libs.versions.githubGroup.get()
version = libs.versions.appVersion.get()
val enableJitpackPublication = providers.gradleProperty("enableJitpackPublication")
    .orElse(providers.environmentVariable("JITPACK"))
    .map { it.equals("true", ignoreCase = true) }
    .getOrElse(false)

if (enableJitpackPublication) {
    publishing {
        publications {
            register("release", MavenPublication::class) {
                groupId = "com.github.Rhpark"
                artifactId = "Simple_UI_System_Manager"
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
        publishToMavenCentral()
        signAllPublications()

        coordinates(
            libs.versions.githubGroup.get(),
            libs.versions.mavenArtifactIdSystemManager.get(),
            libs.versions.appVersion.get()
        )

        pom {
            name.set(libs.versions.mavenArtifactIdSystemManager.get())
            description.set("Android system manager helpers for device information and system controllers.")
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
    namespace = "kr.open.library.simple_ui.system_manager"
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
    implementation(project(":simple_core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.material)

    dokkaPlugin("org.jetbrains.dokka:android-documentation-plugin:2.1.0")

    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kover {
    reports {
        filters {
            excludes {
                classes("**.BuildConfig")
                classes("**.R")
                classes("**.R$*")
                classes("**.databinding.**")
                classes("**.Databinding.**")
                classes("**.BR")
                classes("**.DataBinderMapperImpl*")
                classes("**.DataBindingTriggerClass")
            }
        }
    }
}

tasks.dokkaHtml {
    moduleName.set("Simple UI System Manager")

    dokkaSourceSets {
        named("main") {
            sourceLink {
                localDirectory.set(file("src/main/java"))
                remoteUrl.set(
                    uri("https://github.com/Rhpark/Simple_UI_XML/tree/master/simple_system_manager/src/main/java").toURL(),
                )
                remoteLineSuffix.set("#L")
            }

            externalDocumentationLink {
                url.set(uri("https://developer.android.com/reference/").toURL())
            }
        }
    }
}

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
