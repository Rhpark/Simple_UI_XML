plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // FireBase (Add the Google services Gradle plugin)
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution") //apk 자동 배포
    id("com.google.firebase.crashlytics") //Firebase Exception Report
}

// 빌드 타입별 Suffix 상수
object AppConfig {
    const val DEBUG = "debug"
    const val VERIFICATION = "verification"
    const val RELEASE = "release"
    const val DEBUG_SUFFIX = ".$DEBUG"
    const val VERIFICATION_SUFFIX = ".$VERIFICATION"
    const val RELEASE_SUFFIX = ""
}

android {
    namespace = "kr.open.library.simpleui_xml"
    compileSdk = 35

    defaultConfig {
        applicationId = "kr.open.library.simpleui_xml"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = libs.versions.appVersion.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = AppConfig.DEBUG_SUFFIX

            // Develop 모드: Crashlytics 비활성화
            manifestPlaceholders["crashlytics_collection_enabled"] = false

            buildConfigField("String", "BUILD_TYPE_NAME", "\"${AppConfig.DEBUG}\"")
            buildConfigField("String", "CRASH_REPORT_URL", "\"\"")
            buildConfigField("String", "CRASH_API_KEY", "\"\"")
        }

        create(AppConfig.VERIFICATION) {
            initWith(getByName(AppConfig.DEBUG))
            applicationIdSuffix = AppConfig.VERIFICATION_SUFFIX

            // verification 빌드타입이 없을 때 debug 설정을 재사용
            matchingFallbacks += listOf(AppConfig.DEBUG)

            // Testing 모드: Crashlytics 비활성화
            manifestPlaceholders["crashlytics_collection_enabled"] = false

            // BuildConfig 필드 추가
            buildConfigField("String", "BUILD_TYPE_NAME", "\"${AppConfig.VERIFICATION}\"")
            buildConfigField("String", "CRASH_REPORT_URL", "\"https://us-central1-rhpark-cc1f1.cloudfunctions.net/reportTestCrash\"")
            buildConfigField("String", "CRASH_API_KEY", "\"SIMPLE_UI_VER_2025_nR8kL4mX9pT2wQ7vK3sN\"")
        }

        release {
            applicationIdSuffix = AppConfig.RELEASE_SUFFIX
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Release 모드: Crashlytics 활성화
            manifestPlaceholders["crashlytics_collection_enabled"] = true

            buildConfigField("String", "BUILD_TYPE_NAME", "\"${AppConfig.RELEASE}\"")
            buildConfigField("String", "CRASH_REPORT_URL", "\"\"")
            buildConfigField("String", "CRASH_API_KEY", "\"\"")
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
//        viewBinding = true
        buildConfig = true  // BuildConfig 활성화
    }
}

firebaseAppDistribution {
    // Determine the App ID based on the task name
    val taskNames = gradle.startParameter.taskNames.toString().lowercase()
    appId = when {
        taskNames.contains("verification") -> "1:549084067814:android:3ecfc4be81884ce0738827" // Verification
        taskNames.contains("debug") -> "1:549084067814:android:d467d3ea55c4c608738827"        // Debug
        else -> "1:549084067814:android:2477eceb48b0314a738827"                                 // Release (Default)
    }

    val credentialsFile =
        (project.findProperty("firebaseCredentialsFile") as String?) ?:
        System.getenv("FIREBASE_CREDENTIALS_FILE") ?:
        rootProject.file("firebase-app-dist.json").takeIf { it.exists() }?.absolutePath

    if (!credentialsFile.isNullOrBlank()) {
        serviceCredentialsFile = credentialsFile
    }

    releaseNotes = (project.findProperty("firebaseReleaseNotes") as String?)
        ?: System.getenv("FIREBASE_RELEASE_NOTES")
        ?: "Automated build ${System.getenv("GITHUB_RUN_NUMBER") ?: ""}"

    val groupsValue = (project.findProperty("firebaseDistributionGroups") as String?)
        ?: System.getenv("FIREBASE_TESTER_GROUPS")
    if (!groupsValue.isNullOrBlank()) {
        groups = groupsValue
    }

    val testersValue = (project.findProperty("firebaseDistributionTesters") as String?)
        ?: System.getenv("FIREBASE_TESTERS")
    if (!testersValue.isNullOrBlank()) {
        testers = testersValue
    }

    val artifactOverride =
        (project.findProperty("firebaseArtifactPath") as String?) ?:
        System.getenv("FIREBASE_ARTIFACT_PATH")
    if (!artifactOverride.isNullOrBlank()) {
        artifactPath = artifactOverride
    }
}

dependencies {
    implementation(project(":simple_core"))
    implementation(project(":simple_xml"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    /**************
     *  FireBase  *
     **************/
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")


    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    // Add the dependencies for the Crashlytics NDK and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics-ndk")
    implementation("com.google.firebase:firebase-analytics")
}
