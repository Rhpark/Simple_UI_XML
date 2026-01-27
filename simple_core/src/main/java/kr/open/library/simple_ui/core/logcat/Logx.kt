package kr.open.library.simple_ui.core.logcat

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.permissions.extentions.hasPermissions
import java.util.concurrent.atomic.AtomicBoolean
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigStore
import kr.open.library.simple_ui.core.logcat.config.StorageType
import kr.open.library.simple_ui.core.logcat.internal.common.LogxTagHelper
import kr.open.library.simple_ui.core.logcat.internal.pipeline.LogxPipeline
import kr.open.library.simple_ui.core.logcat.internal.writer.LogxFileWriter

/**
 * Main entry point for temp_logcat logging API.<br><br>
 * temp_logcat 로그 API의 진입점이다.<br>
 */
object Logx {
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
    private val pipeline = LogxPipeline(
        contextProvider = { appContext },
        fileWriter = fileWriter,
    )

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
            Log.e(LogxTagHelper.errorTag(null), "Tag block list contains blank tags. Removed invalid entries.")
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
            throw IllegalStateException("Logx.initialize(context) must be called before enabling file logging.")
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
    fun setStorageType(type: StorageType) {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = {
                LogxConfigStore.setStorageType(type)
            },
            negativeWork = {
                if(type == StorageType.PUBLIC_EXTERNAL) {
                    if(appContext == null) {
                        throw IllegalStateException("Logx.initialize(context) must be called before setting PUBLIC_EXTERNAL.")
                    } else {
                        if(!appContext!!.hasPermissions(WRITE_EXTERNAL_STORAGE)) {
                            throw SecurityException("WRITE_EXTERNAL_STORAGE permission is not granted.")
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
     * Example use case: MyLogger.d(...) wraps Logx.d(...). Without this, the call site may show MyLogger.<br><br>
     * 예) MyLogger.d(...)가 Logx.d(...)를 감싸는 구조라면, 설정 전에는 MyLogger가 호출 위치로 보일 수 있습니다.<br>
     *
     * @param packages Package prefixes to add (package or class FQCN prefixes).<br><br>
     *                 추가할 패키지/클래스 prefix(전체 경로 포함) 목록.<br>
     */
    @JvmStatic
    fun addSkipPackages(packages: Set<String>) {
        LogxConfigStore.addSkipPackages(packages)
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
    fun getStorageType(): StorageType = LogxConfigStore.getStorageType()

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
    fun v() = logStandard(LogType.VERBOSE, null, null, hasMessage = false, tagProvided = false)

    /**
     * Logs a VERBOSE message with body.<br><br>
     * 메시지를 포함한 VERBOSE 로그를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun v(msg: Any?) = logStandard(LogType.VERBOSE, null, msg, hasMessage = true, tagProvided = false)

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
    fun v(tag: String, msg: Any?) = logStandard(LogType.VERBOSE, tag, msg, hasMessage = true, tagProvided = true)

    /**
     * Logs a DEBUG message without message body.<br><br>
     * 메시지 없이 DEBUG 로그를 출력한다.<br>
     */
    @JvmStatic
    fun d() = logStandard(LogType.DEBUG, null, null, hasMessage = false, tagProvided = false)

    /**
     * Logs a DEBUG message with body.<br><br>
     * 메시지를 포함한 DEBUG 로그를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun d(msg: Any?) = logStandard(LogType.DEBUG, null, msg, hasMessage = true, tagProvided = false)

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
    fun d(tag: String, msg: Any?) = logStandard(LogType.DEBUG, tag, msg, hasMessage = true, tagProvided = true)

    /**
     * Logs an INFO message without message body.<br><br>
     * 메시지 없이 INFO 로그를 출력한다.<br>
     */
    @JvmStatic
    fun i() = logStandard(LogType.INFO, null, null, hasMessage = false, tagProvided = false)

    /**
     * Logs an INFO message with body.<br><br>
     * 메시지를 포함한 INFO 로그를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun i(msg: Any?) = logStandard(LogType.INFO, null, msg, hasMessage = true, tagProvided = false)

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
    fun i(tag: String, msg: Any?) = logStandard(LogType.INFO, tag, msg, hasMessage = true, tagProvided = true)

    /**
     * Logs a WARN message without message body.<br><br>
     * 메시지 없이 WARN 로그를 출력한다.<br>
     */
    @JvmStatic
    fun w() = logStandard(LogType.WARN, null, null, hasMessage = false, tagProvided = false)

    /**
     * Logs a WARN message with body.<br><br>
     * 메시지를 포함한 WARN 로그를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun w(msg: Any?) = logStandard(LogType.WARN, null, msg, hasMessage = true, tagProvided = false)

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
    fun w(tag: String, msg: Any?) = logStandard(LogType.WARN, tag, msg, hasMessage = true, tagProvided = true)

    /**
     * Logs an ERROR message without message body.<br><br>
     * 메시지 없이 ERROR 로그를 출력한다.<br>
     */
    @JvmStatic
    fun e() = logStandard(LogType.ERROR, null, null, hasMessage = false, tagProvided = false)

    /**
     * Logs an ERROR message with body.<br><br>
     * 메시지를 포함한 ERROR 로그를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun e(msg: Any?) = logStandard(LogType.ERROR, null, msg, hasMessage = true, tagProvided = false)

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
    fun e(tag: String, msg: Any?) = logStandard(LogType.ERROR, tag, msg, hasMessage = true, tagProvided = true)

    /**
     * Logs parent trace without message body.<br><br>
     * 메시지 없이 부모 호출 트레이스를 출력한다.<br>
     */
    @JvmStatic
    fun p() = logParent(null, null, hasMessage = false, tagProvided = false)

    /**
     * Logs parent trace with body.<br><br>
     * 메시지를 포함한 부모 호출 트레이스를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun p(msg: Any?) = logParent(null, msg, hasMessage = true, tagProvided = false)

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
    fun p(tag: String, msg: Any?) = logParent(tag, msg, hasMessage = true, tagProvided = true)

    /**
     * Logs JSON string with JSON formatting.<br><br>
     * JSON 문자열을 JSON 포맷으로 출력한다.<br>
     *
     * @param json JSON string.<br><br>
     *             JSON 문자열.<br>
     */
    @JvmStatic
    fun j(json: String) = logJson(null, json, tagProvided = false)

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
    fun j(tag: String, json: String) = logJson(tag, json, tagProvided = true)

    /**
     * Logs current thread id without message body.<br><br>
     * 메시지 없이 현재 스레드 ID를 출력한다.<br>
     */
    @JvmStatic
    fun t() = logThread(null, null, hasMessage = false, tagProvided = false)

    /**
     * Logs current thread id with body.<br><br>
     * 메시지를 포함한 현재 스레드 ID를 출력한다.<br>
     *
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     */
    @JvmStatic
    fun t(msg: Any?) = logThread(null, msg, hasMessage = true, tagProvided = false)

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
    fun t(tag: String, msg: Any?) = logThread(tag, msg, hasMessage = true, tagProvided = true)

    /**
     * Internal helper for standard log output.<br><br>
     * 표준 로그 출력용 내부 헬퍼이다.<br>
     *
     * @param type Log type to output.<br><br>
     *             출력할 로그 타입.<br>
     * @param inputTag Optional input tag.<br><br>
     *                 입력된 태그(선택).<br>
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     * @param hasMessage Whether message should be appended.<br><br>
     *                   메시지 포함 여부.<br>
     * @param tagProvided Whether tag parameter was provided by caller.<br><br>
     *                    호출자가 태그를 제공했는지 여부.<br>
     */
    private fun logStandard(
        type: LogType,
        inputTag: String?,
        msg: Any?,
        hasMessage: Boolean,
        tagProvided: Boolean,
    ) = pipeline.logStandard(type, inputTag, msg, hasMessage, tagProvided)

    /**
     * Internal helper for parent trace logging.<br><br>
     * 부모 호출 트레이스 출력용 내부 헬퍼이다.<br>
     *
     * @param inputTag Optional input tag.<br><br>
     *                 입력된 태그(선택).<br>
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     * @param hasMessage Whether message should be appended.<br><br>
     *                   메시지 포함 여부.<br>
     * @param tagProvided Whether tag parameter was provided by caller.<br><br>
     *                    호출자가 태그를 제공했는지 여부.<br>
     */
    private fun logParent(
        inputTag: String?,
        msg: Any?,
        hasMessage: Boolean,
        tagProvided: Boolean,
    ) = pipeline.logParent(inputTag, msg, hasMessage, tagProvided)

    /**
     * Internal helper for thread id logging.<br><br>
     * 스레드 ID 출력용 내부 헬퍼이다.<br>
     *
     * @param inputTag Optional input tag.<br><br>
     *                 입력된 태그(선택).<br>
     * @param msg Message payload.<br><br>
     *            메시지 본문.<br>
     * @param hasMessage Whether message should be appended.<br><br>
     *                   메시지 포함 여부.<br>
     * @param tagProvided Whether tag parameter was provided by caller.<br><br>
     *                    호출자가 태그를 제공했는지 여부.<br>
     */
    private fun logThread(
        inputTag: String?,
        msg: Any?,
        hasMessage: Boolean,
        tagProvided: Boolean,
    ) = pipeline.logThread(
        inputTag,
        msg,
        hasMessage,
        tagProvided,
        Process.myTid().toLong(),
    )

    /**
     * Internal helper for JSON logging.<br><br>
     * JSON 출력용 내부 헬퍼이다.<br>
     *
     * @param inputTag Optional input tag.<br><br>
     *                 입력된 태그(선택).<br>
     * @param json JSON string payload.<br><br>
     *             JSON 문자열 본문.<br>
     * @param tagProvided Whether tag parameter was provided by caller.<br><br>
     *                    호출자가 태그를 제공했는지 여부.<br>
     */
    private fun logJson(
        inputTag: String?,
        json: String,
        tagProvided: Boolean,
    ) = pipeline.logJson(inputTag, json, tagProvided)
}
