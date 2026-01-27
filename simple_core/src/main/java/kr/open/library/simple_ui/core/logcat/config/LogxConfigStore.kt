package kr.open.library.simple_ui.core.logcat.config

import kr.open.library.simple_ui.core.logcat.internal.common.LogxConstants

/**
 * Central in-memory configuration store for Logx.<br><br>
 * Logx의 중앙 메모리 설정 저장소이다.<br>
 */
internal object LogxConfigStore {
    /**
     * Whether logging output is enabled.<br><br>
     * 로그 출력 활성화 여부.<br>
     */
    @Volatile
    private var isLogging: Boolean = true

    /**
     * Lock for logTypes updates.<br><br>
     * logTypes 갱신 동기화를 위한 락.<br>
     */
    private val logTypesLock = Any()

    /**
     * Allowed log types (allowlist).<br><br>
     * 허용된 로그 타입 목록(허용 목록).<br>
     */
    private var logTypes: Set<LogType> = enumValues<LogType>().toSet()

    /**
     * Whether tag blocklist filtering is enabled.<br><br>
     * 태그 차단 목록 필터 활성화 여부.<br>
     */
    @Volatile
    private var isLogTagBlockListEnabled: Boolean = false

    /**
     * Lock for logTagBlockList updates.<br><br>
     * logTagBlockList 갱신 동기화를 위한 락.<br>
     */
    private val logTagBlockListLock = Any()

    /**
     * Blocked tag list used when blocklist is enabled.<br><br>
     * 차단 목록 활성화 시 적용되는 태그 목록.<br>
     */
    private var logTagBlockList: Set<String> = emptySet()

    /**
     * Whether file logging is enabled.<br><br>
     * 파일 저장 활성화 여부.<br>
     */
    @Volatile
    private var isSaveEnabled: Boolean = false

    /**
     * Current storage type for file output.<br><br>
     * 파일 저장에 사용할 저장소 타입.<br>
     */
    @Volatile
    private var storageType: StorageType = StorageType.APP_EXTERNAL

    /**
     * Custom save directory path (optional).<br><br>
     * 사용자 지정 저장 경로(선택).<br>
     */
    @Volatile
    private var saveDirectory: String? = null

    /**
     * Application name used for tags and file names.<br><br>
     * 태그/파일명에 사용하는 앱 이름.<br>
     */
    @Volatile
    private var appName: String = LogxConstants.defaultAppName

    /**
     * Lock for skipPackages updates.<br><br>
     * skipPackages 갱신 동기화를 위한 락.<br>
     */
    private val skipPackagesLock = Any()

    /**
     * Package prefixes to skip when resolving stack frames.<br><br>
     * 스택 프레임 해석 시 제외할 패키지 prefix 목록.<br>
     */
    private var skipPackages: Set<String> = linkedSetOf(
        "kr.open.library.simple_ui.core.temp_logcat",
        "kr.open.library.simple_ui.core.logcat",
        "java.",
        "kotlin.",
        "kotlinx.coroutines.",
        "kotlin.coroutines",
        "android.util.",
        "android.os.",
        "dalvik.system.",
    )

    /**
     * Sets logging enabled state.<br><br>
     * 로그 출력 활성화 여부를 설정한다.<br>
     *
     * @param enabled Whether to enable logging.<br><br>
     *                로그 활성화 여부.<br>
     */
    fun setLogging(enabled: Boolean) {
        isLogging = enabled
    }

    /**
     * Returns current logging enabled state.<br><br>
     * 현재 로그 활성화 여부를 반환한다.<br>
     */
    fun isLogging(): Boolean = isLogging

    /**
     * Sets allowed log types (allowlist).<br><br>
     * 허용할 로그 타입 목록을 설정한다.<br>
     *
     * @param types Allowed log types.<br><br>
     *              허용할 로그 타입 목록.<br>
     */
    fun setLogTypes(types: Set<LogType>) {
        synchronized(logTypesLock) {
            logTypes = types.toSet()
        }
    }

    /**
     * Returns allowed log types.<br><br>
     * 허용된 로그 타입 목록을 반환한다.<br>
     */
    fun getLogTypes(): Set<LogType> = synchronized(logTypesLock) {
        logTypes.toSet()
    }

    /**
     * Enables or disables tag blocklist filtering.<br><br>
     * 태그 차단 목록 필터 사용 여부를 설정한다.<br>
     *
     * @param enabled Whether to enable blocklist filtering.<br><br>
     *                차단 목록 필터 활성화 여부.<br>
     */
    fun setLogTagBlockListEnabled(enabled: Boolean) {
        isLogTagBlockListEnabled = enabled
    }

    /**
     * Returns whether tag blocklist filtering is enabled.<br><br>
     * 태그 차단 목록 필터 활성화 여부를 반환한다.<br>
     */
    fun isLogTagBlockListEnabled(): Boolean = isLogTagBlockListEnabled

    /**
     * Sets tag blocklist entries.<br><br>
     * 태그 차단 목록을 설정한다.<br>
     *
     * @param tags Tags to block.<br><br>
     *             차단할 태그 목록.<br>
     */
    fun setLogTagBlockList(tags: Set<String>) {
        synchronized(logTagBlockListLock) {
            logTagBlockList = tags.toSet()
        }
    }

    /**
     * Returns current tag blocklist entries.<br><br>
     * 현재 태그 차단 목록을 반환한다.<br>
     */
    fun getLogTagBlockList(): Set<String> = synchronized(logTagBlockListLock) {
        logTagBlockList.toSet()
    }

    /**
     * Sets file logging enabled state.<br><br>
     * 파일 저장 활성화 여부를 설정한다.<br>
     *
     * @param enabled Whether to enable file logging.<br><br>
     *                파일 저장 활성화 여부.<br>
     */
    fun setSaveEnabled(enabled: Boolean) {
        isSaveEnabled = enabled
    }

    /**
     * Returns file logging enabled state.<br><br>
     * 파일 저장 활성화 여부를 반환한다.<br>
     */
    fun isSaveEnabled(): Boolean = isSaveEnabled

    /**
     * Sets storage type for file output.<br><br>
     * 파일 저장소 타입을 설정한다.<br>
     *
     * @param type Storage type to use.<br><br>
     *             사용할 저장소 타입.<br>
     */
    fun setStorageType(type: StorageType) {
        storageType = type
    }

    /**
     * Returns current storage type.<br><br>
     * 현재 저장소 타입을 반환한다.<br>
     */
    fun getStorageType(): StorageType = storageType

    /**
     * Sets custom save directory path.<br><br>
     * 사용자 지정 저장 경로를 설정한다.<br>
     *
     * @param path Directory path or null to use default.<br><br>
     *             저장 경로 또는 기본 경로 사용 시 null.<br>
     */
    fun setSaveDirectory(path: String?) {
        saveDirectory = path
    }

    /**
     * Returns custom save directory path.<br><br>
     * 사용자 지정 저장 경로를 반환한다.<br>
     */
    fun getSaveDirectory(): String? = saveDirectory

    /**
     * Sets application name used in log prefix and file name.<br><br>
     * 로그 프리픽스/파일명에 사용할 앱 이름을 설정한다.<br>
     *
     * @param name Application name.<br><br>
     *             앱 이름.<br>
     */
    fun setAppName(name: String) {
        appName = name
    }

    /**
     * Returns current application name.<br><br>
     * 현재 앱 이름을 반환한다.<br>
     */
    fun getAppName(): String = appName

    /**
     * Adds package prefixes to skip during stack trace resolution.<br><br>
     * 스택 트레이스 해석 시 제외할 패키지 prefix를 추가한다.<br>
     *
     * @param packages Package prefixes to add.<br><br>
     *                 추가할 패키지 prefix 목록.<br>
     */
    fun addSkipPackages(packages: Set<String>) {
        synchronized(skipPackagesLock) {
            skipPackages = skipPackages.toMutableSet().apply { addAll(packages) }.toSet()
        }
    }

    /**
     * Returns current skip package prefixes.<br><br>
     * 현재 제외 패키지 prefix 목록을 반환한다.<br>
     */
    fun getSkipPackages(): Set<String> = synchronized(skipPackagesLock) {
        skipPackages.toSet()
    }

    /**
     * Creates an immutable snapshot of current configuration.<br><br>
     * 현재 설정의 불변 스냅샷을 생성한다.<br>
     */
    fun snapshot(): LogxConfigSnapshot = LogxConfigSnapshot(
        isLogging = isLogging,
        logTypes = getLogTypes(),
        isLogTagBlockListEnabled = isLogTagBlockListEnabled,
        logTagBlockList = getLogTagBlockList(),
        isSaveEnabled = isSaveEnabled,
        storageType = storageType,
        saveDirectory = saveDirectory,
        appName = appName,
        skipPackages = getSkipPackages(),
    )
}

