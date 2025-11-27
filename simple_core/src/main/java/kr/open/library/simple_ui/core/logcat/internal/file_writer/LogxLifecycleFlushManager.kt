package kr.open.library.simple_ui.core.logcat.internal.file_writer

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import java.lang.ref.WeakReference

/**
 * Android Lifecycle을 활용한 안전하고 신뢰할 수 있는 로그 플러시 관리자
 * Runtime.addShutdownHook를 대체하는 Android 친화적 솔루션
 */
class LogxLifecycleFlushManager private constructor() : DefaultLifecycleObserver, ComponentCallbacks2 {

    companion object {
        @Volatile
        private var INSTANCE: LogxLifecycleFlushManager? = null
        private const val TAG = "LogxLifecycleFlush"

        fun getInstance(): LogxLifecycleFlushManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LogxLifecycleFlushManager().also { INSTANCE = it }
            }
        }
    }

    private var applicationRef: WeakReference<Application>? = null
    private var logWriterScopeRef: WeakReference<CoroutineScope>? = null
    private var isRegistered = false
    private var originalExceptionHandler: Thread.UncaughtExceptionHandler? = null

    /**
     * 초기화 (Context가 필요한 시점에서 호출)
     */
    fun initialize(context: Context, logWriterScope: CoroutineScope) {
        if (isRegistered) return

        try {
            val app = context.applicationContext as? Application
            if (app != null) {
                applicationRef = WeakReference(app)
                logWriterScopeRef = WeakReference(logWriterScope)

                // ProcessLifecycleOwner에 observer 등록
                ProcessLifecycleOwner.get().lifecycle.addObserver(this)

                // ComponentCallbacks2 등록 (메모리 압박 감지)
                app.registerComponentCallbacks(this)

                // 안전한 크래시 핸들러 설정
                setupSafeCrashHandler()

                isRegistered = true
                Log.d(TAG, "LogxLifecycleFlushManager initialized successfully")

            } else {
                Log.w(TAG, "Context is not Application instance - fallback to basic initialization")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize LogxLifecycleFlushManager", e)
        }
    }

    /**
     * 안전한 크래시 핸들러 설정 (기존 핸들러와 연계)
     */
    private fun setupSafeCrashHandler() {
        try {
            // 기존 핸들러 백업
            originalExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

            // 새로운 핸들러 설정 (기존 핸들러 호출 보장)
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    // 로그 즉시 플러시
                    flushLogs("CRASH")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during crash flush", e)
                } finally {
                    // 기존 핸들러 호출 (중요!)
                    originalExceptionHandler?.uncaughtException(thread, throwable)
                        ?: throwable.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup crash handler", e)
        }
    }

    /**
     * ProcessLifecycleOwner - 앱이 background로 갈 때
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App going to background - flushing logs")
        flushLogs("BACKGROUND")
    }

    /**
     * ProcessLifecycleOwner - 앱 프로세스가 종료될 때
     */
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.d(TAG, "App process destroying - final flush")
        flushLogs("DESTROY")
        cleanup()
    }

    /**
     * ComponentCallbacks2 - 메모리 압박 상황 감지
     */
    override fun onTrimMemory(level: Int) {
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                Log.d(TAG, "UI hidden - flushing logs")
                flushLogs("UI_HIDDEN")
            }
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                Log.d(TAG, "App in background - memory pressure")
                flushLogs("MEMORY_PRESSURE")
            }
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                Log.d(TAG, "High memory pressure - emergency flush")
                flushLogs("EMERGENCY")
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w(TAG, "Critical memory situation - immediate flush")
                flushLogs("CRITICAL")
            }

            else-> {}
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // 설정 변경 시 특별한 처리 불필요
    }

    override fun onLowMemory() {
        Log.w(TAG, "Low memory warning - flushing logs")
        flushLogs("LOW_MEMORY")
    }

    /**
     * 로그 플러시 실행
     */
    private fun flushLogs(reason: String) {
        try {
            val scope = logWriterScopeRef?.get()
            if (scope != null) {
                Log.d(TAG, "Flushing logs - reason: $reason")
                // 코루틴 스코프 취소 (진행 중인 작업들이 완료되도록)
                scope.cancel("Log flush requested: $reason")
            } else {
                Log.w(TAG, "LogWriter scope is null - cannot flush")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during log flush", e)
        }
    }

    /**
     * 수동 플러시 (외부에서 호출 가능)
     */
    fun manualFlush(reason: String = "MANUAL") {
        flushLogs(reason)
    }

    /**
     * 리소스 정리
     */
    private fun cleanup() {
        try {
            if (isRegistered) {
                // Observer 제거
                ProcessLifecycleOwner.get().lifecycle.removeObserver(this)

                // ComponentCallbacks 제거
                applicationRef?.get()?.unregisterComponentCallbacks(this)

                // 예외 핸들러 복원
                if (originalExceptionHandler != null) {
                    Thread.setDefaultUncaughtExceptionHandler(originalExceptionHandler)
                }

                isRegistered = false
                Log.d(TAG, "LogxLifecycleFlushManager cleaned up")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    /**
     * 강제 정리 (외부에서 호출 가능)
     */
    fun forceCleanup() {
        cleanup()
        INSTANCE = null
    }
}