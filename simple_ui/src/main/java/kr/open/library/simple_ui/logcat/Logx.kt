package kr.open.library.simple_ui.logcat

import android.annotation.SuppressLint
import android.content.Context
import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.config.LogxConfigManager
import kr.open.library.simple_ui.logcat.config.LogxConfigFactory
import kr.open.library.simple_ui.logcat.config.LogxDslBuilder
import kr.open.library.simple_ui.logcat.config.LogxPathUtils
import kr.open.library.simple_ui.logcat.config.LogxStorageType
import kr.open.library.simple_ui.logcat.config.logxConfig
import kr.open.library.simple_ui.logcat.model.LogxType
import kr.open.library.simple_ui.logcat.runtime.LogxWriter

import java.util.EnumSet

/**
 * Logx 라이브러리의 메인 클래스 (리팩토링됨)
 * SRP 원칙을 준수하여 설정 관리를 LogxConfigManager로 분리
 *
 * 사용법:
 * Logx.d(), Logx.d(msg), Logx.d(tag, msg)
 *
 * 로그캣 출력 형식:
 * D/AppName ["tag"] : (FileName:NumberLine).Method - msg
 *
 * 특수 기능:
 * - Logx.p() : 부모 메서드 호출 정보 표시
 * - Logx.j() : JSON 포맷팅
 * - Logx.t() : 현재 스레드 ID 표시
 */
object Logx : ILogx {

    @Volatile
    private var appContext: Context? = null

    public const val DEFAULT_TAG = ""

    public val configManager = LogxConfigManager()

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
     * Context 기반 초기화 (권장)
     * 신규 사용자를 위한 최적 경로 자동 설정
     * Android Lifecycle 기반 플러시 시스템 활성화
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
     * 저장소 타입 설정
     */
    fun setStorageType(storageType: LogxStorageType) {
        appContext?.let { context ->
            val newConfig = LogxConfigFactory.create(context, storageType)
            configManager.updateConfig(newConfig)
        }
    }

    /**
     * 내부 저장소로 설정
     */
    fun setInternalStorage() {
        setStorageType(LogxStorageType.INTERNAL)
    }

    /**
     * 앱 전용 외부 저장소로 설정 (권한 불필요)
     */
    fun setAppExternalStorage() {
        setStorageType(LogxStorageType.APP_EXTERNAL)
    }

    /**
     * 공용 외부 저장소로 설정 (권한 필요)
     */
    fun setPublicExternalStorage() {
        setStorageType(LogxStorageType.PUBLIC_EXTERNAL)
    }

    /**
     * 현재 저장소 타입별 경로 정보 반환
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
     * 현재 저장소 타입에서 권한이 필요한지 확인
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
     * 설정을 한 번에 업데이트
     */
    fun updateConfig(newConfig: LogxConfig) {
        configManager.updateConfig(newConfig)
    }

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
     * 리소스 정리 (앱 종료 시 호출 권장)
     */
    fun cleanup() {
        logWriter.cleanup()
        configManager.removeAllConfigChangeListener()
    }
}
