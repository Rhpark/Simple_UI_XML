package kr.open.library.simpleui_xml

import android.app.Application
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simpleui_xml.crash.CrashReporter

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logx.init(this)
        // Setup crash reporter for verification build only
        // Verification 빌드에서만 크래시 리포터 설정
        Logx.d("BuildConfig.BUILD_TYPE_NAME ${BuildConfig.BUILD_TYPE_NAME}")
        if (BuildConfig.BUILD_TYPE_NAME == "verification") {
            setupCrashReporter()
        }
    }

    /**
     * Setup crash reporter for verification build
     * <br><br>
     * Verification 빌드용 크래시 리포터 설정
     * <br>
     */
    private fun setupCrashReporter() {
        val crashReporter =
            CrashReporter(
                cloudFunctionUrl = BuildConfig.CRASH_REPORT_URL,
                apiKey = BuildConfig.CRASH_API_KEY,
                appVersion = BuildConfig.VERSION_NAME,
            )
        Thread.setDefaultUncaughtExceptionHandler(crashReporter)
    }
}
