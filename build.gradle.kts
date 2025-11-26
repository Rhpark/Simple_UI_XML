// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false

    // FireBase (Add the dependency for the Google services Gradle plugin)
    id("com.google.gms.google-services") version "4.4.4" apply false //Firebase Common
    id("com.google.firebase.appdistribution") version "5.0.0" apply false // Firebase App Distribution(Apk 테스트 자동 베포)
    id("com.google.firebase.crashlytics") version "3.0.6" apply false // Firebase Exception Report
}