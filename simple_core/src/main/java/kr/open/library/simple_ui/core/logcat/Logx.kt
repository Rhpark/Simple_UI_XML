package kr.open.library.simple_ui.core.logcat

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.annotation.RestrictTo
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigStore
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.internal.pipeline.LogxPipeline
import kr.open.library.simple_ui.core.logcat.internal.writer.LogxFileWriter
import kr.open.library.simple_ui.core.permissions.extentions.hasPermissions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main entry point for logcat logging API.<br><br>
 * logcat 로그 API의 진입점이다.<br>
 */
object Logx {
    private val TAG = this::class.java.simpleName

    /**
     * Application context used for file logging and permission checks.<br><br>
     * 파일 저장 및 권한 확인에 사용하는 애플리케이션 컨텍스트.<br>
     */
    @Volatile
    private var appContext: Context? = null

    /**
     * File writer responsible for file output.<br><br>
     * 파일 출력을 담당하는 작성기.<br>
     */
    private val fileWriter = LogxFileWriter()

    /**
     * Guard to ensure lifecycle observer is registered once.<br><br>
     * 라이프사이클 옵저버가 1회만 등록되도록 보장하는 플래그.<br>
     */
    private val lifecycleRegistered = AtomicBoolean(false)

    /**
     * Log processing pipeline entry.<br><br>
     * 로그 처리 파이프라인 진입점.<br>
     */
    @Volatile
    private var pipeline: LogxPipeline = createDefaultPipeline()

    /**
     * Lifecycle observer to close file writer on app backgrounding.<br><br>
     * 앱 백그라운드 진입 시 파일 작성기를 닫는 라이프사이클 옵저버.<br>
     */
    private val lifecycleObserver = object : DefaultLifecycleObserver {
        /**
         * Called when app goes to background.<br><br>
         * 앱이 백그라운드로 전환될 때 호출된다.<br>
         *
         * @param owner Lifecycle owner.<br><br>
         *              라이프사이클 소유자.<br>
         */
        override fun onStop(owner: LifecycleOwner) {
            fileWriter.requestClose()
        }
    }

    /**
     * Initializes Logx with application context.<br><br>
     * Logx를 애플리케이션 컨텍스트로 초기화한다.<br>
     *
     * @param context Application context.<br><br>
     *                애플리케이션 컨텍스트.<br>
     */
    @JvmStatic
    fun initialize(context: Context) {
        appContext = context.applicationContext
        pipeline.setDevelopmentMode(context)
        registerLifecycleObserverOnce()
    }

    /**
     * Registers lifecycle observer only once.<br><br>
     * 라이프사이클 옵저버를 한 번만 등록한다.<br>
     */
    private fun registerLifecycleObserverOnce() {
        if (!lifecycleRegistered.compareAndSet(false, true)) return
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    private fun createDefaultPipeline(): LogxPipeline = LogxPipeline(
        contextProvider = { appContext },
        fileWriter = fileWriter,
    )

    /**
     * Enables or disables logging globally.<br><br>
     * 전역 로그 출력 활성화 여부를 설정한다.<br>
     *
     * @param enabled Whether to enable logging.<br><br>
     *                로그 활성화 여부.<br>
     */
    @JvmStatic
    fun setLogging(enabled: Boolean) {
        LogxConfigStore.setLogging(enabled)
    }

    /**
     * Sets allowed log types (allowlist).<br><br>
     * 허용할 로그 타입 목록을 설정한다.<br>
     *
     * @param types Allowed log types.<br><br>
     *              허용할 로그 타입 목록.<br>
     */
    @JvmStatic
    fun setLogTypes(types: Set<LogType>) {
        LogxConfigStore.setLogTypes(types)
    }

    /**
     * Enables or disables tag blocklist filtering.<br><br>
     * 태그 차단 목록 필터 사용 여부를 설정한다.<br>
     *
     * @param enabled Whether to enable blocklist filtering.<br><br>
     *                차단 목록 필터 활성화 여부.<br>
     */
    @JvmStatic
    fun setLogTagBlockListEnabled(enabled: Boolean) {
        LogxConfigStore.setLogTagBlockListEnabled(enabled)
    }

    /**
     * Sets tag blocklist entries.<br><br>
     * 태그 차단 목록을 설정한다.<br>
     *
     * @param tags Tags to block.<br><br>
     *             차단할 태그 목록.<br>
     */
    @JvmStatic
    fun setLogTagBlockList(tags: Set<String>) {
        val filtered = tags.filter { it.isNotBlank() }.toSet()
        if (filtered.size != tags.size) {
            Log.e(TAG, "Tag block list contains blank tags. Removed invalid entries.")
        }
        LogxConfigStore.setLogTagBlockList(filtered)
    }

    /**
     * Enables or disables file logging.<br><br>
     * 파일 로그 저장 활성화 여부를 설정한다.<br>
     *
     * @param enabled Whether to enable file logging.<br><br>
     *                파일 로그 활성화 여부.<br>
     */
    @JvmStatic
    fun setSaveEnabled(enabled: Boolean) {
        if (enabled && appContext == null) {
            val message = "Logx.initialize(context) must be called before enabling file logging. " +
                "Call Logx.initialize(applicationContext) in Application.onCreate()."
            if (pipeline.isDevelopmentMode()) {
                error(message)
            } else {
                Log.e(TAG, message)
            }
        }
        if (!enabled) {
            fileWriter.requestClose()
        }
        LogxConfigStore.setSaveEnabled(enabled)
    }

    /**
     * Sets storage type for file logging.<br><br>
     * 파일 로그 저장소 타입을 설정한다.<br>
     *
     * @param type Storage type to use.<br><br>
     *             사용할 저장소 타입.<br>
     */
    @JvmStatic
    fun setStorageType(type: LogStorageType) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { LogxConfigStore.setStorageType(type) },
            negativeWork = {
                if (type == LogStorageType.PUBLIC_EXTERNAL) {
                    if (appContext == null) {
                        val message =
                            "Logx.initialize(context) must be called before setting PUBLIC_EXTERNAL. " +
                                "Call Logx.initialize(applicationContext) in Application.onCreate()."
                        if (pipeline.isDevelopmentMode()) {
                            error(message)
                        } else {
                            Log.e(TAG, message)
                        }
                    } else {
                        if (!appContext!!.hasPermissions(WRITE_EXTERNAL_STORAGE)) {
                            val message = "WRITE_EXTERNAL_STORAGE permission is not granted."
                            if (pipeline.isDevelopmentMode()) {
                                error(message)
                            } else {
                                Log.e(TAG, message)
                            }
                        }
                    }
                }
                LogxConfigStore.setStorageType(type)
            }
        )
    }

    /**
     * Sets a custom save directory path.<br><br>
     * 사용자 지정 저장 경로를 설정한다.<br>
     *
     * @param path Directory path.<br><br>
     *             저장 경로.<br>
     */
    @JvmStatic
    fun setSaveDirectory(path: String) {
        LogxConfigStore.setSaveDirectory(path)
    }

    /**
     * Sets application name used in tags and file names.<br><br>
     * 태그/파일명에 사용할 앱 이름을 설정한다.<br>
     *
     * @param name Application name.<br><br>
     *             앱 이름.<br>
     */
    @JvmStatic
    fun setAppName(name: String) {
        LogxConfigStore.setAppName(name)
    }

    /**
     * Adds package prefixes to skip in stack trace resolution.<br><br>
     * 스택 트레이스 탐색에서 제외할 패키지 prefix를 추가합니다.<br>
     *
     * This does NOT suppress logs. It only shifts the reported call site to the next frame.<br><br>
     * 로그 출력 자체를 차단하는 기능이 아니며, 표시되는 호출 위치를 다음 프레임으로 이동시킵니다.<br>
     *
     * Use when you have a custom wrapper/logger and want the log location to point to the real caller.<br><br>
     * 커스텀 래퍼/로거를 사용해 로그 위치가 래퍼로 찍힐 때 실제 호출 지점으로 이동시키기 위해 사용합니다.<br>
     *
     * Prefix matching is applied to className.startsWith(prefix).<br><br>
     * className.startsWith(prefix) 기준으로 접두사 매칭을 수행합니다.<br>
     *
     * This affects all log types (standard/PARENT/THREAD/JSON).<br><br>
     * 표준/PARENT/THREAD/JSON 모든 로그 타입에 동일하게 적용됩니다.<br>
     *
     * Note: Adding many prefixes can increase stack scan cost.<br><br>
     * 주의: prefix가 많아질수록 스택 탐색 비용이 증가할 수 있습니다.<br>
     *
     * Example use case: MyLogger.d(...) wraps Logx.d(...). Without this, the call site may show MyLogger.<br><br>
     * 예) MyLogger.d(...)가 Logx.d(...)를 감싸는 구조라면, 설정 전에는 MyLogger가 호출 위치로 보일 수 있습니다.<br>
     * Example code:<br><br>
     * 예시 코드:<br>
     * ```
     * Logx.addSkipPackages(setOf("com.example.logger."))
     * MyLogger.d("message")
     * ```
     *
     * @param packages Package prefixes to add (package or class FQCN prefixes).<br><br>
     *                 추가할 패키지/클래스 prefix(전체 경로 포함) 목록.<br>
     */
    @JvmStatic
    fun addSkipPackages(packages: Set<String>) {
        LogxConfigStore.addSkipPackages(packages)
    }

    /**
     * Injects a pipeline for tests.<br><br>
     * 테스트용 파이프라인을 주입합니다.<br>
     *
     * @param custom Pipeline for tests.<br><br>
     *              테스트용 파이프라인.<br>
     */
    @JvmStatic
    @RestrictTo(RestrictTo.Scope.TESTS)
    internal fun setPipelineForTest(custom: LogxPipeline) {
        pipeline = custom
    }

    /**
     * Restores the default pipeline after tests.<br><br>
     * 테스트 후 기본 파이프라인으로 복원합니다.<br>
     */
    @JvmStatic
    internal fun resetPipelineForTest() {
        pipeline = createDefaultPipeline()
    }

    /**
     * Returns whether logging is enabled.<br><br>
     * 로그 출력 활성화 여부를 반환합니다.<br>
     */
    @JvmStatic
    fun isLogging(): Boolean = LogxConfigStore.isLogging()

    /**
     * Returns allowed log types.<br><br>
     * 허용된 로그 타입 목록을 반환한다.<br>
     */
    @JvmStatic
    fun getLogTypes(): Set<LogType> = LogxConfigStore.getLogTypes()

    /**
     * Returns whether tag blocklist filtering is enabled.<br><br>
     * 태그 차단 목록 필터 활성화 여부를 반환한다.<br>
     */
    @JvmStatic
    fun isLogTagBlockListEnabled(): Boolean = LogxConfigStore.isLogTagBlockListEnabled()

    /**
     * Returns tag blocklist entries.<br><br>
     * 태그 차단 목록을 반환한다.<br>
     */
    @JvmStatic
    fun getLogTagBlockList(): Set<String> = LogxConfigStore.getLogTagBlockList()

    /**
     * Returns whether file logging is enabled.<br><br>
     * 파일 저장 활성화 여부를 반환한다.<br>
     */
    @JvmStatic
    fun isSaveEnabled(): Boolean = LogxConfigStore.isSaveEnabled()

    /**
     * Returns current storage type.<br><br>
     * 현재 저장소 타입을 반환한다.<br>
     */
    @JvmStatic
    fun getStorageType(): LogStorageType = LogxConfigStore.getStorageType()

    /**
     * Returns custom save directory path.<br><br>
     * 사용자 지정 저장 경로를 반환한다.<br>
     */
    @JvmStatic
    fun getSaveDirectory(): String? = LogxConfigStore.getSaveDirectory()

    /**
     * Returns current application name.<br><br>
     * 현재 앱 이름을 반환한다.<br>
     */
    @JvmStatic
    fun getAppName(): String = LogxConfigStore.getAppName()

    /**
     * Returns skip package prefixes used in stack trace resolution.<br><br>
     * 스택 트레이스 해석에 사용하는 제외 패키지 prefix를 반환한다.<br>
     */
    @JvmStatic
    fun getSkipPackages(): Set<String> = LogxConfigStore.getSkipPackages()

    /**
     * Logs a VERBOSE message without message body.<br><br>
     * 메시지 없이 VERBOSE 로그를 출력한다.<br>
     */
    @JvmStatic
    fun v() = pipeline.logStandard(LogType.VERBOSE, null, null, false, false)

    /**
     * Logs a VERBOSE message with body.<br><br>
     * 메시지를 포함한 VERBOSE 로그를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun v(msg: Any?) = pipeline.logStandard(LogType.VERBOSE, null, msg, true, false)

    /**
     * Logs a VERBOSE message with custom tag and body.<br><br>
     * 커스텀 태그와 메시지를 포함한 VERBOSE 로그를 출력한다.<br>
     *
     * @param tag Tag to use.<br><br>
     *            사용할 태그.<br>
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun v(tag: String, msg: Any?) = pipeline.logStandard(LogType.VERBOSE, tag, msg, true, true)

    /**
     * Logs a DEBUG message without message body.<br><br>
     * 메시지 없이 DEBUG 로그를 출력한다.<br>
     */
    @JvmStatic
    fun d() = pipeline.logStandard(LogType.DEBUG, null, null, false, false)

    /**
     * Logs a DEBUG message with body.<br><br>
     * 메시지를 포함한 DEBUG 로그를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun d(msg: Any?) = pipeline.logStandard(LogType.DEBUG, null, msg, true, false)

    /**
     * Logs a DEBUG message with custom tag and body.<br><br>
     * 커스텀 태그와 메시지를 포함한 DEBUG 로그를 출력한다.<br>
     *
     * @param tag Tag to use.<br><br>
     *            사용할 태그.<br>
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun d(tag: String, msg: Any?) = pipeline.logStandard(LogType.DEBUG, tag, msg, true, true)

    /**
     * Logs an INFO message without message body.<br><br>
     * 메시지 없이 INFO 로그를 출력한다.<br>
     */
    @JvmStatic
    fun i() = pipeline.logStandard(LogType.INFO, null, null, false, false)

    /**
     * Logs an INFO message with body.<br><br>
     * 메시지를 포함한 INFO 로그를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun i(msg: Any?) = pipeline.logStandard(LogType.INFO, null, msg, true, false)

    /**
     * Logs an INFO message with custom tag and body.<br><br>
     * 커스텀 태그와 메시지를 포함한 INFO 로그를 출력한다.<br>
     *
     * @param tag Tag to use.<br><br>
     *            사용할 태그.<br>
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun i(tag: String, msg: Any?) = pipeline.logStandard(LogType.INFO, tag, msg, true, true)

    /**
     * Logs a WARN message without message body.<br><br>
     * 메시지 없이 WARN 로그를 출력한다.<br>
     */
    @JvmStatic
    fun w() = pipeline.logStandard(LogType.WARN, null, null, false, false)

    /**
     * Logs a WARN message with body.<br><br>
     * 메시지를 포함한 WARN 로그를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun w(msg: Any?) = pipeline.logStandard(LogType.WARN, null, msg, true, false)

    /**
     * Logs a WARN message with custom tag and body.<br><br>
     * 커스텀 태그와 메시지를 포함한 WARN 로그를 출력한다.<br>
     *
     * @param tag Tag to use.<br><br>
     *            사용할 태그.<br>
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun w(tag: String, msg: Any?) = pipeline.logStandard(LogType.WARN, tag, msg, true, true)

    /**
     * Logs an ERROR message without message body.<br><br>
     * 메시지 없이 ERROR 로그를 출력한다.<br>
     */
    @JvmStatic
    fun e() = pipeline.logStandard(LogType.ERROR, null, null, false, false)

    /**
     * Logs an ERROR message with body.<br><br>
     * 메시지를 포함한 ERROR 로그를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun e(msg: Any?) = pipeline.logStandard(LogType.ERROR, null, msg, true, false)

    /**
     * Logs an ERROR message with custom tag and body.<br><br>
     * 커스텀 태그와 메시지를 포함한 ERROR 로그를 출력한다.<br>
     *
     * @param tag Tag to use.<br><br>
     *            사용할 태그.<br>
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun e(tag: String, msg: Any?) = pipeline.logStandard(LogType.ERROR, tag, msg, true, true)

    /**
     * Logs parent trace without message body.<br><br>
     * 메시지 없이 부모 호출 트레이스를 출력한다.<br>
     */
    @JvmStatic
    fun p() = pipeline.logParent(null, null, false, false)

    /**
     * Logs parent trace with body.<br><br>
     * 메시지를 포함한 부모 호출 트레이스를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun p(msg: Any?) = pipeline.logParent(null, msg, true, false)

    /**
     * Logs parent trace with custom tag and body.<br><br>
     * 커스텀 태그와 메시지를 포함한 부모 호출 트레이스를 출력한다.<br>
     *
     * @param tag Tag to use.<br><br>
     *            사용할 태그.<br>
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun p(tag: String, msg: Any?) = pipeline.logParent(tag, msg, true, true)

    /**
     * Logs JSON string with JSON formatting.<br><br>
     * JSON 문자열을 JSON 포맷으로 출력한다.<br>
     *
     * @param json JSON string.<br><br>
     *             JSON 문자열.<br>
     */
    @JvmStatic
    fun j(json: String) = pipeline.logJson(null, json, false)

    /**
     * Logs JSON string with JSON formatting and custom tag.<br><br>
     * JSON 문자열을 JSON 포맷과 커스텀 태그로 출력한다.<br>
     *
     * @param tag Tag to use.<br><br>
     *            사용할 태그.<br>
     * @param json JSON string.<br><br>
     *             JSON 문자열.<br>
     */
    @JvmStatic
    fun j(tag: String, json: String) = pipeline.logJson(tag, json, true)

    /**
     * Logs current thread id without message body.<br><br>
     * 메시지 없이 현재 스레드 ID를 출력한다.<br>
     */
    @JvmStatic
    fun t() =
        pipeline.logThread(null, null, false, false, Process.myTid().toLong())

    /**
     * Logs current thread id with body.<br><br>
     * 메시지를 포함한 현재 스레드 ID를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun t(msg: Any?) =
        pipeline.logThread(null, msg, true, false, Process.myTid().toLong())

    /**
     * Logs current thread id with custom tag and body.<br><br>
     * 커스텀 태그와 메시지를 포함한 현재 스레드 ID를 출력한다.<br>
     *
     * @param tag Tag to use.<br><br>
     *            사용할 태그.<br>
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun t(tag: String, msg: Any?) =
        pipeline.logThread(tag, msg, true, true, Process.myTid().toLong())
}
