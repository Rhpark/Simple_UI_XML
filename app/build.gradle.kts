plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // FireBase (Add the Google services Gradle plugin)
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "kr.open.library.simpleui_xml"
    compileSdk = 35

    defaultConfig {
        applicationId = "kr.open.library.simpleui_xml"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.2.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        //noinspection DataBindingWithoutKapt
        dataBinding = true
//        viewBinding = true
    }
}

firebaseAppDistribution {
    appId = "1:549084067814:android:2477eceb48b0314a738827"

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
}

dependencies {
    implementation(project(":simple_ui"))
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
}
