import org.gradle.api.tasks.testing.Test
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id ("maven-publish")
    id("org.jetbrains.kotlinx.kover") version "0.9.3" //UnitTest
    id("org.jetbrains.dokka") version "2.1.0" //Dokka - Document
}

publishing {
    publications {
        register("release", MavenPublication::class) { // MavenPublication::class 사용 가능
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

    //Base
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    //Dokka - Document
    dokkaPlugin("org.jetbrains.dokka:android-documentation-plugin:2.1.0")

    //Test
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.test:core:1.6.1")  // ApplicationProvider 등을 위해 필요
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    //AAC LifeCycle
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

                classes("kr.open.library.simple_ui.DataBinderMapperImpl*") // *로 Inner 클래스까지 함께 제외
                classes("kr.open.library.simple_ui.DataBindingTriggerClass")



                /*******************************
                 *   Android OS 에 종속 인 부분  *
                 * 통합 테스트 단위에서 태스트 예정 *
                 *******************************/

                // 2. 베이스 컴포넌트(단위 테스트 어려움)
                classes("**.RootActivity")
                classes("**.RootFragment")
                classes("**.RootDialogFragment")
                classes("**.BaseActivity")
                classes("**.BaseBindingActivity")
                classes("**.BaseFragment")
                classes("**.BaseBindingFragment")
                classes("**.BaseDialogFragment")
                classes("**.BaseBindingDialogFragment")
                classes("**.RootDialogFragment")
                // 3. Lifecycle 커스텀 Layout
                classes("**.BaseLifeCycle*Layout")


                classes("kr.open.library.simple_ui.logcat.ILogx*")
                classes("kr.open.library.simple_ui.logcat.extensions.LogxExtensions*")
                classes("kr.open.library.simple_ui.logcat.Logx*")
                classes("kr.open.library.simple_ui.logcat.runtime.LogxWriter*")

                classes("kr.open.library.simple_ui.system_manager.base.BaseSystemService*")
                classes("kr.open.library.simple_ui.system_manager.controller.alarm.AlarmController*")
                classes("kr.open.library.simple_ui.system_manager.controller.alarm.receiver.BaseAlarmReceiver*")
                classes("kr.open.library.simple_ui.system_manager.controller.wifi.WifiController*")
                classes("kr.open.library.simple_ui.system_manager.controller.window.FloatingViewController*")
                classes("kr.open.library.simple_ui.system_manager.info.battery.BatteryStateInfo*")
                classes("kr.open.library.simple_ui.system_manager.info.battery.power.PowerProfile*")

                classes("kr.open.library.simple_ui.system_manager.info.location.LocationStateInfo*")
                classes("kr.open.library.simple_ui.system_manager.info.network.connectivity.NetworkConnectivityInfo*")
                classes("kr.open.library.simple_ui.system_manager.info.network.sim.SimInfo*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.TelephonyInfo*")
            }
        }
    }
}

// 테스트 태스크 분리: Unit Test와 Robolectric Test
tasks.register<Test>("testUnit") {
    description = "Runs pure unit tests only (no Android dependencies)"
    group = "verification"

    // Lazy하게 참조 - 실제 실행 시점에 testDebugUnitTest 찾음
    val testDebugTask = tasks.named<Test>("testDebugUnitTest")
    testClassesDirs = testDebugTask.get().testClassesDirs
    classpath = testDebugTask.get().classpath

    include("**/unit/**")

    // 실패 시 즉시 중단
    failFast = true
}

tasks.register<Test>("testRobolectric") {
    description = "Runs Robolectric tests only (Android framework simulation)"
    group = "verification"

    val testDebugTask = tasks.named<Test>("testDebugUnitTest")
    testClassesDirs = testDebugTask.get().testClassesDirs
    classpath = testDebugTask.get().classpath

    include("**/robolectric/**")

    // 실패 시 즉시 중단
    failFast = true

    mustRunAfter("testUnit")
}

tasks.register("testAll") {
    description = "Runs unit tests first, then robolectric tests if unit tests pass"
    group = "verification"

    dependsOn("testUnit", "testRobolectric")
}

dokka {
    dokkaSourceSets.configureEach {
        documentedVisibilities.set(setOf(VisibilityModifier.Public, VisibilityModifier.Protected))
    }
}
