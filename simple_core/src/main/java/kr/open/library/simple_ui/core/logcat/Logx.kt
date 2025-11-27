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

    @Volatile
    private var appContext: Context? = null

    public const val DEFAULT_TAG = ""

    private val configManager = LogxConfigManager()

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


    override fun v() = logWriter.write(DEFAULT_TAG, "", LogxType.VERBOSE)
    override fun v(msg: Any?) = logWriter.write(DEFAULT_TAG, msg, LogxType.VERBOSE)
    override fun v(tag: String, msg: Any?) = logWriter.write(tag, msg, LogxType.VERBOSE)
    fun v1(msg: Any?) = logWriter.writeExtensions(DEFAULT_TAG, msg, LogxType.VERBOSE)
    fun v1(tag: String, msg: Any?) = logWriter.writeExtensions(tag, msg, LogxType.VERBOSE)

    override fun d() = logWriter.write(DEFAULT_TAG, "", LogxType.DEBUG)
    override fun d(msg: Any?) = logWriter.write(DEFAULT_TAG, msg, LogxType.DEBUG)
    override fun d(tag: String, msg: Any?) = logWriter.write(tag, msg, LogxType.DEBUG)
    fun d1(msg: Any?) = logWriter.writeExtensions(DEFAULT_TAG, msg, LogxType.DEBUG)
    fun d1(tag: String, msg: Any?) = logWriter.writeExtensions(tag, msg, LogxType.DEBUG)

    override fun i() = logWriter.write(DEFAULT_TAG, "", LogxType.INFO)
    override fun i(msg: Any?) = logWriter.write(DEFAULT_TAG, msg, LogxType.INFO)
    override fun i(tag: String, msg: Any?) = logWriter.write(tag, msg, LogxType.INFO)
    fun i1(msg: Any?) = logWriter.writeExtensions(DEFAULT_TAG, msg, LogxType.INFO)
    fun i1(tag: String, msg: Any?) = logWriter.writeExtensions(tag, msg, LogxType.INFO)

    override fun w() = logWriter.write(DEFAULT_TAG, "", LogxType.WARN)
    override fun w(msg: Any?) = logWriter.write(DEFAULT_TAG, msg, LogxType.WARN)
    override fun w(tag: String, msg: Any?) = logWriter.write(tag, msg, LogxType.WARN)
    fun w1(msg: Any?) = logWriter.writeExtensions(DEFAULT_TAG, msg, LogxType.WARN)
    fun w1(tag: String, msg: Any?) = logWriter.writeExtensions(tag, msg, LogxType.WARN)

    override fun e() = logWriter.write(DEFAULT_TAG, "", LogxType.ERROR)
    override fun e(msg: Any?) = logWriter.write(DEFAULT_TAG, msg, LogxType.ERROR)
    override fun e(tag: String, msg: Any?) = logWriter.write(tag, msg, LogxType.ERROR)
    fun e1(msg: Any?) = logWriter.writeExtensions(DEFAULT_TAG, msg, LogxType.ERROR)
    fun e1(tag: String, msg: Any?) = logWriter.writeExtensions(tag, msg, LogxType.ERROR)

    // 확장 기능들
    override fun p() = logWriter.writeParent(DEFAULT_TAG, "")
    override fun p(msg: Any?) = logWriter.writeParent(DEFAULT_TAG, msg)
    override fun p(tag: String, msg: Any?) = logWriter.writeParent(tag, msg)
    fun p1(msg: Any?) = logWriter.writeExtensionsParent(DEFAULT_TAG, msg)
    fun p1(tag: String, msg: Any?) = logWriter.writeExtensionsParent(tag, msg)

    override fun t() = logWriter.writeThreadId(DEFAULT_TAG, "")
    override fun t(msg: Any?) = logWriter.writeThreadId(DEFAULT_TAG, msg)
    override fun t(tag: String, msg: Any?) = logWriter.writeThreadId(tag, msg)

    override fun j(msg: String) = logWriter.writeJson(DEFAULT_TAG, msg)
    override fun j(tag: String, msg: String) = logWriter.writeJson(tag, msg)
    fun j1(msg: String) = logWriter.writeJsonExtensions(DEFAULT_TAG, msg)
    fun j1(tag: String, msg: String) = logWriter.writeJsonExtensions(tag, msg)

    // 레거시 호환성을 위한 getter 메서드들
    fun getDebugMode(): Boolean = configManager.config.isDebug
    fun getDebugFilter(): Boolean = configManager.config.isDebugFilter
    fun getSaveToFile(): Boolean = configManager.config.isDebugSave
    fun getFilePath(): String = configManager.config.saveFilePath
    fun getAppName(): String = configManager.config.appName
    fun getDebugFilterList(): Set<String> = configManager.config.debugFilterList
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
