plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id ("maven-publish")
    id("org.jetbrains.kotlinx.kover") version "0.9.3"
}

publishing {
    publications {
        register("release", MavenPublication::class) { // MavenPublication::class 사용 가능
            groupId = "com.github.Rhpark"
            artifactId = "Simple_UI_XML"
            version = "0.2.0"

            afterEvaluate {
                from(components.findByName("release"))
            }
        }

        register("debug", MavenPublication::class) { // MavenPublication::class 사용 가능
            groupId = "com.github.Rhpark"
            artifactId = "Simple_UI_XML"
            version = "0.2.0" // 동일 버전 사용 시 주의 (이전 답변 참고)

            afterEvaluate {
                from(components.findByName("debug"))
            }
        }
    }
}

android {
    namespace = "kr.open.library.simple_ui"
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
    buildFeatures {
        // buildConfig = false
        //noinspection DataBindingWithoutKapt
        dataBinding = true
//        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Unit Test를 위한 추가 의존성
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.common)

}

//Kover로 UnitTest
kover {
    reports {
        // HTML 리포트 설정 (로컬에서 확인용)
        filters {
            excludes {
                // 1. Android 자동생성 파일
                classes("**/BuildConfig")
                classes("**/R")
                classes("**/R\\$*")
                classes("**/databinding/**")
                classes("**/BR")

                // 2. 베이스 컴포넌트(단위 테스트 어려움)
                classes("**/RootActivity")
                classes("**/RootFragment")
                classes("**/RootDialogFragment")
                classes("**/BaseActivity")
                classes("**/BaseBindingActivity")
                classes("**/BaseFragment")
                classes("**/BaseBindingFragment")
                classes("**/BaseDialogFragment")
                classes("**/BaseBindingDialogFragment")

                // 3. Lifecycle 커스텀 Layout
                classes("**/BaseLifeCycle*Layout")
            }
        }
    }
}