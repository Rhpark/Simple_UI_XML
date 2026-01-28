package kr.open.library.simple_ui.core.logcat.config

import kr.open.library.simple_ui.core.logcat.internal.common.LogxConstants
import java.util.Collections

/**
 * Central in-memory configuration store for Logx.<br><br>
 * Logx의 중앙 메모리 설정 저장소이다.<br>
 */
internal object LogxConfigStore {
    private val snapshotLock = Any()
    private val defaultSkipPackages = Collections.unmodifiableSet(linkedSetOf(
        "kr.open.library.simple_ui.core.logcat",
        "java.",
        "kotlin.",
        "kotlinx.coroutines.",
        "kotlin.coroutines",
        "android.util.",
        "android.os.",
        "dalvik.system.",
    ))

    /**
     * Immutable snapshot that represents the current configuration.<br><br>
     * 현재 설정을 표현하는 불변 스냅샷.<br>
     */
    @Volatile
    private var snapshot: LogxConfigSnapshot = LogxConfigSnapshot(
        isLogging = true,
        logTypes = Collections.unmodifiableSet(enumValues<LogType>().toSet()),
        isLogTagBlockListEnabled = false,
        logTagBlockList = Collections.unmodifiableSet(emptySet()),
        isSaveEnabled = false,
        storageType = LogStorageType.APP_EXTERNAL,
        saveDirectory = null,
        appName = LogxConstants.DEFAULT_APP_NAME,
        skipPackages = defaultSkipPackages,
    )

    /**
     * Sets logging enabled state.<br><br>
     * 로그 출력 활성화 여부를 설정한다.<br>
     *
     * @param enabled Whether to enable logging.<br><br>
     *                로그 활성화 여부.<br>
     */
    fun setLogging(enabled: Boolean) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(isLogging = enabled)
        }
    }

    /**
     * Returns current logging enabled state.<br><br>
     * 현재 로그 활성화 여부를 반환한다.<br>
     */
    fun isLogging(): Boolean = snapshot.isLogging

    /**
     * Sets allowed log types (allowlist).<br><br>
     * 허용할 로그 타입 목록을 설정한다.<br>
     *
     * @param types Allowed log types.<br><br>
     *              허용할 로그 타입 목록.<br>
     */
    fun setLogTypes(types: Set<LogType>) {
        val sanitized = Collections.unmodifiableSet(types.toSet())
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(logTypes = sanitized)
        }
    }

    /**
     * Returns allowed log types.<br><br>
     * 허용된 로그 타입 목록을 반환한다.<br>
     */
    fun getLogTypes(): Set<LogType> = snapshot.logTypes

    /**
     * Enables or disables tag blocklist filtering.<br><br>
     * 태그 차단 목록 필터 사용 여부를 설정한다.<br>
     *
     * @param enabled Whether to enable blocklist filtering.<br><br>
     *                차단 목록 필터 활성화 여부.<br>
     */
    fun setLogTagBlockListEnabled(enabled: Boolean) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(isLogTagBlockListEnabled = enabled)
        }
    }

    /**
     * Returns whether tag blocklist filtering is enabled.<br><br>
     * 태그 차단 목록 필터 활성화 여부를 반환한다.<br>
     */
    fun isLogTagBlockListEnabled(): Boolean = snapshot.isLogTagBlockListEnabled

    /**
     * Sets tag blocklist entries.<br><br>
     * 태그 차단 목록을 설정한다.<br>
     *
     * @param tags Tags to block.<br><br>
     *             차단할 태그 목록.<br>
     */
    fun setLogTagBlockList(tags: Set<String>) {
        val sanitized = Collections.unmodifiableSet(tags.toSet())
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(logTagBlockList = sanitized)
        }
    }

    /**
     * Returns current tag blocklist entries.<br><br>
     * 현재 태그 차단 목록을 반환한다.<br>
     */
    fun getLogTagBlockList(): Set<String> = snapshot.logTagBlockList

    /**
     * Sets file logging enabled state.<br><br>
     * 파일 저장 활성화 여부를 설정한다.<br>
     *
     * @param enabled Whether to enable file logging.<br><br>
     *                파일 저장 활성화 여부.<br>
     */
    fun setSaveEnabled(enabled: Boolean) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(isSaveEnabled = enabled)
        }
    }

    /**
     * Returns file logging enabled state.<br><br>
     * 파일 저장 활성화 여부를 반환한다.<br>
     */
    fun isSaveEnabled(): Boolean = snapshot.isSaveEnabled

    /**
     * Sets storage type for file output.<br><br>
     * 파일 저장소 타입을 설정한다.<br>
     *
     * @param type Storage type to use.<br><br>
     *             사용할 저장소 타입.<br>
     */
    fun setStorageType(type: LogStorageType) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(storageType = type)
        }
    }

    /**
     * Returns current storage type.<br><br>
     * 현재 저장소 타입을 반환한다.<br>
     */
    fun getStorageType(): LogStorageType = snapshot.storageType

    /**
     * Sets custom save directory path.<br><br>
     * 사용자 지정 저장 경로를 설정한다.<br>
     *
     * @param path Directory path or null to use default.<br><br>
     *             저장 경로 또는 기본 경로 사용 시 null.<br>
     */
    fun setSaveDirectory(path: String?) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(saveDirectory = path)
        }
    }

    /**
     * Returns custom save directory path.<br><br>
     * 사용자 지정 저장 경로를 반환한다.<br>
     */
    fun getSaveDirectory(): String? = snapshot.saveDirectory

    /**
     * Sets application name used in log prefix and file name.<br><br>
     * 로그 프리픽스/파일명에 사용할 앱 이름을 설정한다.<br>
     *
     * @param name Application name.<br><br>
     *             앱 이름.<br>
     */
    fun setAppName(name: String) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(appName = name)
        }
    }

    /**
     * Returns current application name.<br><br>
     * 현재 앱 이름을 반환한다.<br>
     */
    fun getAppName(): String = snapshot.appName

    /**
     * Adds package prefixes to skip during stack trace resolution.<br><br>
     * 스택 트레이스 해석 시 제외할 패키지 prefix를 추가한다.<br>
     *
     * @param packages Package prefixes to add.<br><br>
     *                 추가할 패키지 prefix 목록.<br>
     */
    fun addSkipPackages(packages: Set<String>) {
        val filtered = packages.filter { it.isNotBlank() }.toSet()
        if (filtered.isEmpty()) return
        synchronized(snapshotLock) {
            val merged = snapshot.skipPackages.toMutableSet()
            merged.addAll(filtered)
            snapshot = snapshot.copy(skipPackages = Collections.unmodifiableSet(merged))
        }
    }

    /**
     * Returns current skip package prefixes.<br><br>
     * 현재 제외 패키지 prefix 목록을 반환한다.<br>
     */
    fun getSkipPackages(): Set<String> = snapshot.skipPackages

    /**
     * Creates an immutable snapshot of current configuration.<br><br>
     * 현재 설정의 불변 스냅샷을 생성한다.<br>
     */
    fun snapshot(): LogxConfigSnapshot = snapshot
}
