import org.gradle.api.tasks.testing.Test

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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {

    //Base
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    //Test
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.13")
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
                // 1. Android 자동생성 파일
                classes("**.BuildConfig")
                classes("**.R")
                classes("**.R$*")
                classes("**.databinding.**")
                classes("**.Databinding.**")
                classes("**.BR")

                classes("kr.open.library.simple_ui.DataBinderMapperImpl*") // *로 Inner 클래스까지 함께 제외
                classes("kr.open.library.simple_ui.DataBindingTriggerClass")



                /*****************************
                 * Android OS 에 종속 인 부분  *
                 *****************************/

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

                // 3. Lifecycle 커스텀 Layout
                classes("**.BaseLifeCycle*Layout")


                classes("kr.open.library.simple_ui.extensions.conditional.SdkVersionInlineKt*")
                classes("kr.open.library.simple_ui.extensions.bundle.BundleInlineKt*")
                classes("kr.open.library.simple_ui.extensions.string.StringExtensionsKt*")
                classes("kr.open.library.simple_ui.local.base.BaseSharedPreference*")

                classes("kr.open.library.simple_ui.logcat.ILogx*")
                classes("kr.open.library.simple_ui.logcat.extensions.LogxExtensions*")
                classes("kr.open.library.simple_ui.logcat.internal.file_writer.NoOpLogFileWriter*")
                classes("kr.open.library.simple_ui.logcat.Logx*")
                classes("kr.open.library.simple_ui.logcat.config.LogxConfig*")
                classes("kr.open.library.simple_ui.logcat.config.LogxConfigManager*")
                classes("kr.open.library.simple_ui.logcat.config.LogxPathUtils*")
                classes("kr.open.library.simple_ui.logcat.internal.file_writer.LogxFileWriter*")
                classes("kr.open.library.simple_ui.logcat.internal.file_writer.LogxFileWriterFactory*")
                classes("kr.open.library.simple_ui.logcat.internal.file_writer.LogxLifecycleFlushManager*")
                classes("kr.open.library.simple_ui.logcat.internal.stacktrace.LogxStackTrace*")
                classes("kr.open.library.simple_ui.logcat.internal.stacktrace.LogxStackTraceMetaData*")
                classes("kr.open.library.simple_ui.logcat.runtime.LogxWriter*")

                classes("kr.open.library.simple_ui.permissions.extentions.PermissionExtensionsKt*")
                classes("kr.open.library.simple_ui.permissions.manager.PermissionManager*")
                classes("kr.open.library.simple_ui.permissions.register.PermissionDelegate*")
                classes("kr.open.library.simple_ui.permissions.vo.PermissionConstants*")
                classes("kr.open.library.simple_ui.permissions.vo.PermissionSpecialType*")

                classes("kr.open.library.simple_ui.presenter.extensions.display.DisplayUnitExtensionsKt*")
                classes("kr.open.library.simple_ui.presenter.extensions.resource.ResourceExtensionsKt*")
                classes("kr.open.library.simple_ui.presenter.extensions.view.EditTextExtensionsKt*")
                classes("kr.open.library.simple_ui.presenter.extensions.view.ImageViewExtensionsKt*")
                classes("kr.open.library.simple_ui.presenter.extensions.view.SnackBarExtensionsKt*")
                classes("kr.open.library.simple_ui.presenter.extensions.view.TextViewExtensionsKt*")
                classes("kr.open.library.simple_ui.presenter.extensions.view.ToastExtensionsKt*")

                classes("kr.open.library.simple_ui.presenter.ui.adapter.list.base.BaseRcvListAdapter*")
                classes("kr.open.library.simple_ui.presenter.ui.adapter.list.diffutil.RcvListDiffUtilCallBack*")
                classes("kr.open.library.simple_ui.presenter.ui.adapter.list.simple.SimpleBindingRcvListAdapter*")
                classes("kr.open.library.simple_ui.presenter.ui.adapter.list.simple.SimpleRcvListAdapter*")
                classes("kr.open.library.simple_ui.presenter.ui.adapter.normal.base.BaseRcvAdapter*")
                classes("kr.open.library.simple_ui.presenter.ui.adapter.normal.simple.SimpleBindingRcvAdapter*")
                classes("kr.open.library.simple_ui.presenter.ui.adapter.normal.simple.SimpleRcvAdapter*")
                classes("kr.open.library.simple_ui.presenter.ui.adapter.viewholder.BaseBindingRcvViewHolder*")
                classes("kr.open.library.simple_ui.presenter.ui.adapter.viewholder.BaseRcvViewHolder*")
                classes("kr.open.library.simple_ui.presenter.ui.fragment.dialog.RootDialogFragment*")
                classes("kr.open.library.simple_ui.presenter.ui.view.recyclerview.RecyclerScrollStateView*")
                classes("kr.open.library.simple_ui.presenter.viewmodel.BaseViewModel*")
                classes("kr.open.library.simple_ui.presenter.viewmodel.BaseViewModelEvent*")

                classes("kr.open.library.simple_ui.system_manager.base.BaseSystemService*")
                classes("kr.open.library.simple_ui.system_manager.controller.alarm.AlarmController*")
                classes("kr.open.library.simple_ui.system_manager.controller.alarm.receiver.BaseAlarmReceiver*")
                classes("kr.open.library.simple_ui.system_manager.controller.alarm.vo.AlarmVo*")
                classes("kr.open.library.simple_ui.system_manager.controller.notification.SimpleNotificationController*")
                classes("kr.open.library.simple_ui.system_manager.controller.softkeyboard.SoftKeyboardController*")
                classes("kr.open.library.simple_ui.system_manager.controller.vibrator.VibratorController*")
                classes("kr.open.library.simple_ui.system_manager.controller.wifi.WifiController*")
                classes("kr.open.library.simple_ui.system_manager.controller.window.FloatingViewController*")
                classes("kr.open.library.simple_ui.system_manager.controller.window.drag.FloatingDragView*")
                classes("kr.open.library.simple_ui.system_manager.controller.window.drag.FloatingDragViewConfig*")
                classes("kr.open.library.simple_ui.system_manager.controller.window.fixed.FloatingFixedView*")
                classes("kr.open.library.simple_ui.system_manager.extensions.SystemServiceExtensionsKt*")
                classes("kr.open.library.simple_ui.system_manager.info.battery.BatteryStateEvent*")
                classes("kr.open.library.simple_ui.system_manager.info.battery.BatteryStateInfo*")
                classes("kr.open.library.simple_ui.system_manager.info.battery.power.PowerProfile*")
                classes("kr.open.library.simple_ui.system_manager.info.display.DisplayInfo*")
                classes("kr.open.library.simple_ui.system_manager.info.location.LocationSharedPreference*")
                classes("kr.open.library.simple_ui.system_manager.info.location.LocationStateEvent*")
                classes("kr.open.library.simple_ui.system_manager.info.location.LocationStateInfo*")
                classes("kr.open.library.simple_ui.system_manager.info.network.connectivity.NetworkConnectivityInfo*")
                classes("kr.open.library.simple_ui.system_manager.info.network.connectivity.callback.NetworkStateCallback*")
                classes("kr.open.library.simple_ui.system_manager.info.network.connectivity.data.NetworkCapabilitiesData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.connectivity.data.NetworkLinkPropertiesData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.sim.SimInfo*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.TelephonyInfo*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.callback.CommonTelephonyCallback*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.cdma.CellIdentityCdmaData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.cdma.CellInfoCdmaData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.cdma.CellSignalStrengthCdmaData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.gsm.CellIdentityGsmData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.gsm.CellInfoGsmData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.gsm.CellSignalStrengthGsmData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.lte.CellIdentityLteData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.lte.CellInfoLteData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.lte.CellSignalStrengthLteData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.nr.CellIdentityNrData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.nr.CellInfoNrData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.nr.CellSignalStrengthNrData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.tdscdma.CellIdentityTdscdmaData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.tdscdma.CellInfoTdscdmaData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.tdscdma.CellSignalStrengthDataTdscdma*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.wcdma.CellIdentityWcdmaData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.wcdma.CellInfoWcdmaData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.wcdma.CellSignalStrengthWcdmaData*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentCellInfo*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentServiceState*")
                classes("kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentSignalStrength*")
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
