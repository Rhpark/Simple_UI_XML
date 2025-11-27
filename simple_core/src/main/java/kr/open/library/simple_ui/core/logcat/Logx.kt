package kr.open.library.simple_ui.core.logcat

import android.annotation.SuppressLint
import android.content.Context
import kr.open.library.simple_ui.core.logcat.config.LogxConfig
import kr.open.library.simple_ui.core.logcat.config.LogxConfigManager
import kr.open.library.simple_ui.core.logcat.config.LogxConfigFactory
import kr.open.library.simple_ui.core.logcat.config.LogxDslBuilder
import kr.open.library.simple_ui.core.logcat.config.LogxPathUtils
import kr.open.library.simple_ui.core.logcat.config.LogxStorageType
import kr.open.library.simple_ui.core.logcat.config.logxConfig
import kr.open.library.simple_ui.core.logcat.model.LogxType
import kr.open.library.simple_ui.core.logcat.runtime.LogxWriter

import java.util.EnumSet

/**
 * Main singleton object for the Logx logging library (refactored with SRP principle).<br>
 * Configuration management is delegated to LogxConfigManager following the Single Responsibility Principle.<br><br>
 * Logx 로깅 라이브러리의 메인 싱글톤 객체입니다 (SRP 원칙으로 리팩토링됨).<br>
 * 단일 책임 원칙에 따라 설정 관리는 LogxConfigManager에 위임됩니다.<br>
 *
 * Basic usage:<br>
 * ```kotlin
 * // Initialize with Context (recommended), Context로 초기화 (권장)
 * Logx.init(context)
 *
 * // Standard logging
 * Logx.d()                    // Debug log with stack trace, 스택 트레이스와 함께 Debug 로그
 * Logx.d("message")           // Debug log with message, 메시지와 함께 Debug 로그
 * Logx.d("TAG", "message")    // Debug log with custom tag, 커스텀 태그와 메시지
 *
 * // Same pattern for v(), i(), w(), e()
 *
 * Log output format:<br>
 * `D/AppName ["tag"] : (FileName:LineNumber).Method - msg`<br><br>
 * 로그 출력 형식:<br>
 * `D/AppName ["tag"] : (FileName:LineNumber).Method - msg`<br>
 *
 * Extended features:<br>
 * - `Logx.p()`: Parent method call tracking, 부모 메서드 호출 추적<<br>
 * - `Logx.j()`: JSON formatting with visual markers, 시각적 마커를 사용한 JSON 포맷팅<br>
 * - `Logx.t()`: Current thread ID display, 현재 스레드 ID 표시<br><br>
 *
 * Configuration:<br>
 * ```kotlin
 * // DSL-based configuration
 * Logx.configure {
 *     debugMode = true
 *     appName = "MyApp"
 *     fileConfig {
 *         saveToFile = true
 *     }
 * }
 *
 * // Or use individual setters, 또는 개별 setter 사용
 * Logx.setDebugMode(true)
 * Logx.setSaveToFile(true)
 * ```
 */
object Logx : ILogx {

    /**
     * Application context for Android-specific operations.<br>
     * Used for file path resolution and lifecycle management.<br><br>
     * Android 특정 작업을 위한 애플리케이션 컨텍스트입니다.<br>
     * 파일 경로 확인 및 라이프사이클 관리에 사용됩니다.<br>
     */
    @Volatile
    private var appContext: Context? = null

    /**
     * Default tag used when no custom tag is specified.<br><br>
     * 커스텀 태그가 지정되지 않았을 때 사용되는 기본 태그입니다.<br>
     */
    public const val DEFAULT_TAG = ""

    /**
     * Configuration manager responsible for managing Logx settings.<br>
     * Follows Single Responsibility Principle by delegating configuration logic.<br><br>
     * Logx 설정 관리를 담당하는 설정 관리자입니다.<br>
     * 단일 책임 원칙에 따라 설정 로직을 위임합니다.<br>
     */
    private val configManager = LogxConfigManager()

    /**
     * Writer responsible for outputting log messages to Logcat and files.<br>
     * Automatically receives configuration updates from configManager.<br><br>
     * 로그 메시지를 Logcat과 파일로 출력하는 작성기입니다.<br>
     * configManager로부터 설정 업데이트를 자동으로 받습니다.<br>
     */
    @SuppressLint("StaticFieldLeak")
    public var logWriter = LogxWriter(configManager.config)

    init {
        // 설정 변경 시 LogxWriter에 자동 전파
        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                logWriter.updateConfig(newConfig)
            }
        })
    }

    /**
     * Initializes Logx with Android Context (recommended).<br>
     * Automatically configures optimal storage paths for new users and activates Android Lifecycle-based flush system.<br><br>
     * Android Context로 Logx를 초기화합니다 (권장).<br>
     * 신규 사용자를 위한 최적 저장소 경로를 자동으로 구성하고 Android Lifecycle 기반 플러시 시스템을 활성화합니다.<br>
     *
     * @param context The Android application context.<br><br>
     *                Android 애플리케이션 컨텍스트.
     */
    @Synchronized
    override fun init(context: Context) {
        appContext = context.applicationContext

        // Context가 설정되면 LogxWriter를 Context와 함께 재생성
        logWriter = LogxWriter(configManager.config, appContext)

        // Context 기반 최적 설정으로 업데이트
        val contextConfig = LogxConfigFactory.createDefault(context)
        configManager.updateConfig(contextConfig)
    }


    /**
     * Sets the storage type for log files.<br>
     * Requires Context to be initialized via init() first.<br><br>
     * 로그 파일의 저장소 타입을 설정합니다.<br>
     * 먼저 init()를 통해 Context를 초기화해야 합니다.<br>
     *
     * @param storageType The storage type to use (INTERNAL, APP_EXTERNAL, or PUBLIC_EXTERNAL).<br><br>
     *                    사용할 저장소 타입 (INTERNAL, APP_EXTERNAL, PUBLIC_EXTERNAL).
     */
    fun setStorageType(storageType: LogxStorageType) {
        appContext?.let { context ->
            val newConfig = LogxConfigFactory.create(context, storageType)
            configManager.updateConfig(newConfig)
        }
    }

    /**
     * Configures log storage to use internal storage (no permissions required, not user-accessible).<br><br>
     * 내부 저장소를 사용하도록 로그 저장소를 구성합니다 (권한 불필요, 사용자 접근 불가).<br>
     */
    fun setInternalStorage() {
        setStorageType(LogxStorageType.INTERNAL)
    }

    /**
     * Configures log storage to use app-specific external storage (no permissions required, user-accessible via file manager).<br><br>
     * 앱 전용 외부 저장소를 사용하도록 로그 저장소를 구성합니다 (권한 불필요, 파일 관리자로 사용자 접근 가능).<br>
     */
    fun setAppExternalStorage() {
        setStorageType(LogxStorageType.APP_EXTERNAL)
    }

    /**
     * Configures log storage to use public external storage (requires permission on API 28 and below).<br><br>
     * 공용 외부 저장소를 사용하도록 로그 저장소를 구성합니다 (API 28 이하에서 권한 필요).<br>
     */
    fun setPublicExternalStorage() {
        setStorageType(LogxStorageType.PUBLIC_EXTERNAL)
    }

    /**
     * Returns the absolute paths for all available storage types.<br><br>
     * 모든 사용 가능한 저장소 타입의 절대 경로를 반환합니다.<br>
     *
     * @return A map of storage types to their absolute paths, or empty map if Context not initialized.<br><br>
     *         저장소 타입과 절대 경로의 맵, Context가 초기화되지 않은 경우 빈 맵.
     */
    fun getStorageInfo(): Map<LogxStorageType, String> {
        return appContext?.let { context ->
            mapOf(
                LogxStorageType.INTERNAL to LogxPathUtils.getInternalLogPath(context),
                LogxStorageType.APP_EXTERNAL to LogxPathUtils.getAppExternalLogPath(context),
                LogxStorageType.PUBLIC_EXTERNAL to LogxPathUtils.getPublicExternalLogPath(context)
            )
        } ?: emptyMap()
    }

    /**
     * Checks whether the current storage type requires runtime permissions.<br><br>
     * 현재 저장소 타입이 런타임 권한을 필요로 하는지 확인합니다.<br>
     *
     * @return `true` if WRITE_EXTERNAL_STORAGE permission is required, `false` otherwise.<br><br>
     *         WRITE_EXTERNAL_STORAGE 권한이 필요하면 `true`, 그 외는 `false`.
     */
    fun requiresStoragePermission(): Boolean {
        return LogxPathUtils.requiresPermission(configManager.config.storageType)
    }

    // 설정 관리 메서드들 - ConfigManager에 위임
    override fun setDebugMode(isDebug: Boolean) {
        configManager.setDebugMode(isDebug)
    }

    override fun setDebugFilter(isFilter: Boolean) {
        configManager.setDebugFilter(isFilter)
    }

    override fun setSaveToFile(isSave: Boolean) {
        configManager.setSaveToFile(isSave)
    }

    override fun setFilePath(path: String) {
        configManager.setFilePath(path)
    }

    override fun setAppName(name: String) {
        configManager.setAppName(name)
    }

    override fun setDebugLogTypeList(types: EnumSet<LogxType>) {
        configManager.setDebugLogTypeList(types)
    }

    override fun setDebugFilterList(tags: List<String>) {
        configManager.setDebugFilterList(tags)
    }

    /**
     * Updates the entire configuration at once.<br><br>
     * 전체 설정을 한 번에 업데이트합니다.<br>
     *
     * @param newConfig The new configuration to apply.<br><br>
     *                  적용할 새로운 설정.
     */
    fun updateConfig(newConfig: LogxConfig) {
        configManager.updateConfig(newConfig)
    }

    /**
     * Configures Logx using a type-safe DSL builder.<br><br>
     * DSL 빌더를 사용하여 Logx를 구성합니다.<br>
     *
     * Example usage:<br>
     * ```kotlin
     * Logx.configure {
     *     debugMode = true
     *     appName = "MyApp"
     *     fileConfig {
     *         saveToFile = true
     *         filePath = "/custom/path"
     *     }
     *     logTypes {
     *         basic()
     *         +LogxType.JSON
     *     }
     * }
     * ```
     *
     * @param block The DSL configuration block.<br><br>
     *              DSL 설정 블록.
     */
    fun configure(block: LogxDslBuilder.() -> Unit) {
        val newConfig = logxConfig(block)
        updateConfig(newConfig)
    }



    /**
     * Logs a VERBOSE level message with stack trace information.<br><br>
     * VERBOSE 레벨 메시지를 스택 트레이스 정보와 함께 기록합니다.<br>
     */
    override fun v() = logWriter.write(DEFAULT_TAG, "", LogxType.VERBOSE)

    /**
     * Logs a VERBOSE level message.<br><br>
     * VERBOSE 레벨 메시지를 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun v(msg: Any?) = logWriter.write(DEFAULT_TAG, msg, LogxType.VERBOSE)

    /**
     * Logs a VERBOSE level message with a custom tag.<br><br>
     * 커스텀 태그와 함께 VERBOSE 레벨 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun v(tag: String, msg: Any?) = logWriter.write(tag, msg, LogxType.VERBOSE)

    /**
     * Logs a VERBOSE level message with extended formatting.<br><br>
     * 확장된 포맷으로 VERBOSE 레벨 메시지를 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun v1(msg: Any?) = logWriter.writeExtensions(DEFAULT_TAG, msg, LogxType.VERBOSE)

    /**
     * Logs a VERBOSE level message with extended formatting and a custom tag.<br><br>
     * 확장된 포맷과 커스텀 태그로 VERBOSE 레벨 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun v1(tag: String, msg: Any?) = logWriter.writeExtensions(tag, msg, LogxType.VERBOSE)

    /**
     * Logs a DEBUG level message with stack trace information.<br><br>
     * DEBUG 레벨 메시지를 스택 트레이스 정보와 함께 기록합니다.<br>
     */
    override fun d() = logWriter.write(DEFAULT_TAG, "", LogxType.DEBUG)

    /**
     * Logs a DEBUG level message.<br><br>
     * DEBUG 레벨 메시지를 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun d(msg: Any?) = logWriter.write(DEFAULT_TAG, msg, LogxType.DEBUG)

    /**
     * Logs a DEBUG level message with a custom tag.<br><br>
     * 커스텀 태그와 함께 DEBUG 레벨 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun d(tag: String, msg: Any?) = logWriter.write(tag, msg, LogxType.DEBUG)

    /**
     * Logs a DEBUG level message with extended formatting.<br><br>
     * 확장된 포맷으로 DEBUG 레벨 메시지를 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun d1(msg: Any?) = logWriter.writeExtensions(DEFAULT_TAG, msg, LogxType.DEBUG)

    /**
     * Logs a DEBUG level message with extended formatting and a custom tag.<br><br>
     * 확장된 포맷과 커스텀 태그로 DEBUG 레벨 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun d1(tag: String, msg: Any?) = logWriter.writeExtensions(tag, msg, LogxType.DEBUG)

    /**
     * Logs an INFO level message with stack trace information.<br><br>
     * INFO 레벨 메시지를 스택 트레이스 정보와 함께 기록합니다.<br>
     */
    override fun i() = logWriter.write(DEFAULT_TAG, "", LogxType.INFO)

    /**
     * Logs an INFO level message.<br><br>
     * INFO 레벨 메시지를 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun i(msg: Any?) = logWriter.write(DEFAULT_TAG, msg, LogxType.INFO)

    /**
     * Logs an INFO level message with a custom tag.<br><br>
     * 커스텀 태그와 함께 INFO 레벨 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun i(tag: String, msg: Any?) = logWriter.write(tag, msg, LogxType.INFO)

    /**
     * Logs an INFO level message with extended formatting.<br><br>
     * 확장된 포맷으로 INFO 레벨 메시지를 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun i1(msg: Any?) = logWriter.writeExtensions(DEFAULT_TAG, msg, LogxType.INFO)

    /**
     * Logs an INFO level message with extended formatting and a custom tag.<br><br>
     * 확장된 포맷과 커스텀 태그로 INFO 레벨 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun i1(tag: String, msg: Any?) = logWriter.writeExtensions(tag, msg, LogxType.INFO)

    /**
     * Logs a WARN level message with stack trace information.<br><br>
     * WARN 레벨 메시지를 스택 트레이스 정보와 함께 기록합니다.<br>
     */
    override fun w() = logWriter.write(DEFAULT_TAG, "", LogxType.WARN)

    /**
     * Logs a WARN level message.<br><br>
     * WARN 레벨 메시지를 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun w(msg: Any?) = logWriter.write(DEFAULT_TAG, msg, LogxType.WARN)

    /**
     * Logs a WARN level message with a custom tag.<br><br>
     * 커스텀 태그와 함께 WARN 레벨 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun w(tag: String, msg: Any?) = logWriter.write(tag, msg, LogxType.WARN)

    /**
     * Logs a WARN level message with extended formatting.<br><br>
     * 확장된 포맷으로 WARN 레벨 메시지를 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun w1(msg: Any?) = logWriter.writeExtensions(DEFAULT_TAG, msg, LogxType.WARN)

    /**
     * Logs a WARN level message with extended formatting and a custom tag.<br><br>
     * 확장된 포맷과 커스텀 태그로 WARN 레벨 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun w1(tag: String, msg: Any?) = logWriter.writeExtensions(tag, msg, LogxType.WARN)

    /**
     * Logs an ERROR level message with stack trace information.<br><br>
     * ERROR 레벨 메시지를 스택 트레이스 정보와 함께 기록합니다.<br>
     */
    override fun e() = logWriter.write(DEFAULT_TAG, "", LogxType.ERROR)

    /**
     * Logs an ERROR level message.<br><br>
     * ERROR 레벨 메시지를 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun e(msg: Any?) = logWriter.write(DEFAULT_TAG, msg, LogxType.ERROR)

    /**
     * Logs an ERROR level message with a custom tag.<br><br>
     * 커스텀 태그와 함께 ERROR 레벨 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun e(tag: String, msg: Any?) = logWriter.write(tag, msg, LogxType.ERROR)

    /**
     * Logs an ERROR level message with extended formatting.<br><br>
     * 확장된 포맷으로 ERROR 레벨 메시지를 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun e1(msg: Any?) = logWriter.writeExtensions(DEFAULT_TAG, msg, LogxType.ERROR)

    /**
     * Logs an ERROR level message with extended formatting and a custom tag.<br><br>
     * 확장된 포맷과 커스텀 태그로 ERROR 레벨 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun e1(tag: String, msg: Any?) = logWriter.writeExtensions(tag, msg, LogxType.ERROR)

    // 확장 기능들
    /**
     * Logs the parent method call information.<br>
     * Useful for debugging call hierarchies.<br><br>
     * 부모 메서드 호출 정보를 기록합니다.<br>
     * 호출 계층 디버깅에 유용합니다.<br>
     */
    override fun p() = logWriter.writeParent(DEFAULT_TAG, "")

    /**
     * Logs the parent method call information with a message.<br><br>
     * 부모 메서드 호출 정보를 메시지와 함께 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun p(msg: Any?) = logWriter.writeParent(DEFAULT_TAG, msg)

    /**
     * Logs the parent method call information with a custom tag and message.<br><br>
     * 부모 메서드 호출 정보를 커스텀 태그와 메시지와 함께 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun p(tag: String, msg: Any?) = logWriter.writeParent(tag, msg)

    /**
     * Logs the parent method call information with extended formatting.<br><br>
     * 확장된 포맷으로 부모 메서드 호출 정보를 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun p1(msg: Any?) = logWriter.writeExtensionsParent(DEFAULT_TAG, msg)

    /**
     * Logs the parent method call information with extended formatting and a custom tag.<br><br>
     * 확장된 포맷과 커스텀 태그로 부모 메서드 호출 정보를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    fun p1(tag: String, msg: Any?) = logWriter.writeExtensionsParent(tag, msg)

    /**
     * Logs the current thread ID.<br>
     * Useful for debugging multi-threaded applications.<br><br>
     * 현재 스레드 ID를 기록합니다.<br>
     * 멀티스레드 애플리케이션 디버깅에 유용합니다.<br>
     */
    override fun t() = logWriter.writeThreadId(DEFAULT_TAG, "")

    /**
     * Logs the current thread ID with a message.<br><br>
     * 현재 스레드 ID를 메시지와 함께 기록합니다.<br>
     *
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun t(msg: Any?) = logWriter.writeThreadId(DEFAULT_TAG, msg)

    /**
     * Logs the current thread ID with a custom tag and message.<br><br>
     * 현재 스레드 ID를 커스텀 태그와 메시지와 함께 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The message to log.<br><br>
     *            기록할 메시지.
     */
    override fun t(tag: String, msg: Any?) = logWriter.writeThreadId(tag, msg)

    /**
     * Logs a JSON-formatted message with visual markers for enhanced readability.<br><br>
     * 가독성을 높이기 위한 시각적 마커와 함께 JSON 형식의 메시지를 기록합니다.<br>
     *
     * @param msg The JSON string to log.<br><br>
     *            기록할 JSON 문자열.
     */
    override fun j(msg: String) = logWriter.writeJson(DEFAULT_TAG, msg)

    /**
     * Logs a JSON-formatted message with visual markers and a custom tag.<br><br>
     * 시각적 마커와 커스텀 태그로 JSON 형식의 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The JSON string to log.<br><br>
     *            기록할 JSON 문자열.
     */
    override fun j(tag: String, msg: String) = logWriter.writeJson(tag, msg)

    /**
     * Logs a JSON-formatted message with extended formatting and visual markers.<br><br>
     * 확장된 포맷과 시각적 마커로 JSON 형식의 메시지를 기록합니다.<br>
     *
     * @param msg The JSON string to log.<br><br>
     *            기록할 JSON 문자열.
     */
    fun j1(msg: String) = logWriter.writeJsonExtensions(DEFAULT_TAG, msg)

    /**
     * Logs a JSON-formatted message with extended formatting, visual markers, and a custom tag.<br><br>
     * 확장된 포맷, 시각적 마커, 커스텀 태그로 JSON 형식의 메시지를 기록합니다.<br>
     *
     * @param tag Custom tag for filtering logs.<br><br>
     *            로그 필터링을 위한 커스텀 태그.
     * @param msg The JSON string to log.<br><br>
     *            기록할 JSON 문자열.
     */
    fun j1(tag: String, msg: String) = logWriter.writeJsonExtensions(tag, msg)

    // 레거시 호환성을 위한 getter 메서드들
    /**
     * Returns the current debug mode setting.<br><br>
     * 현재 디버그 모드 설정을 반환합니다.<br>
     *
     * @return `true` if debug mode is enabled, `false` otherwise.<br><br>
     *         디버그 모드가 활성화되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    fun getDebugMode(): Boolean = configManager.config.isDebug

    /**
     * Returns the current debug filter setting.<br><br>
     * 현재 디버그 필터 설정을 반환합니다.<br>
     *
     * @return `true` if debug filter is enabled, `false` otherwise.<br><br>
     *         디버그 필터가 활성화되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    fun getDebugFilter(): Boolean = configManager.config.isDebugFilter

    /**
     * Returns the current save-to-file setting.<br><br>
     * 현재 파일 저장 설정을 반환합니다.<br>
     *
     * @return `true` if saving logs to file is enabled, `false` otherwise.<br><br>
     *         로그를 파일에 저장하는 기능이 활성화되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    fun getSaveToFile(): Boolean = configManager.config.isDebugSave

    /**
     * Returns the current log file path.<br><br>
     * 현재 로그 파일 경로를 반환합니다.<br>
     *
     * @return The absolute path where log files are saved.<br><br>
     *         로그 파일이 저장되는 절대 경로.<br>
     */
    fun getFilePath(): String = configManager.config.saveFilePath

    /**
     * Returns the current application name used in logs.<br><br>
     * 로그에 사용되는 현재 애플리케이션 이름을 반환합니다.<br>
     *
     * @return The application name string.<br><br>
     *         애플리케이션 이름 문자열.<br>
     */
    fun getAppName(): String = configManager.config.appName

    /**
     * Returns the current set of tags used for debug filtering.<br><br>
     * 디버그 필터링에 사용되는 현재 태그 집합을 반환합니다.<br>
     *
     * @return A set of filter tag strings.<br><br>
     *         필터 태그 문자열의 집합.<br>
     */
    fun getDebugFilterList(): Set<String> = configManager.config.debugFilterList

    /**
     * Returns the current set of enabled log types.<br><br>
     * 현재 활성화된 로그 타입 집합을 반환합니다.<br>
     *
     * @return An EnumSet of enabled LogxType values.<br><br>
     *         활성화된 LogxType 값들의 EnumSet.<br>
     */
    fun getDebugLogTypeList(): EnumSet<LogxType> = configManager.config.debugLogTypeList

    /**
     * Cleans up all resources including file writers and configuration listeners.<br>
     * Recommended to call when the application is shutting down to ensure all buffered logs are written.<br><br>
     * 파일 작성기 및 설정 리스너를 포함한 모든 리소스를 정리합니다.<br>
     * 모든 버퍼링된 로그가 작성되도록 애플리케이션 종료 시 호출하는 것이 권장됩니다.<br>
     */
    fun cleanup() {
        logWriter.cleanup()
        configManager.removeAllConfigChangeListener()
    }
}
