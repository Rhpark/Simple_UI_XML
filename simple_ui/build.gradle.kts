plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id ("maven-publish")
}

publishing {
    publications {
        register("release", MavenPublication::class) { // MavenPublication::class 사용 가능
            groupId = "com.github.Rhpark"
            artifactId = "Simple_UI_XML"
            version = "0.1.1"

            afterEvaluate {
                from(components.findByName("release"))
            }
        }

        register("debug", MavenPublication::class) { // MavenPublication::class 사용 가능
            groupId = "com.github.Rhpark"
            artifactId = "Simple_UI_XML"
            version = "0.1.1" // 동일 버전 사용 시 주의 (이전 답변 참고)

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


    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.common)

}