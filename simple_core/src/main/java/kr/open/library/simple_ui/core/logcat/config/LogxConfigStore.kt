package kr.open.library.simple_ui.core.logcat.config

import kr.open.library.simple_ui.core.logcat.internal.common.LogxConstants
import java.util.Collections

/**
 * Logx 설정을 메모리에서 중앙 관리하는 저장소입니다.<br><br>
 * Central in-memory configuration store for Logx.<br>
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
     * 현재 설정 상태를 나타내는 불변 스냅샷입니다.<br><br>
     * Immutable snapshot that represents the current configuration.<br>
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
     * 로그 출력 활성화 여부를 설정합니다.<br><br>
     * Sets logging enabled state.<br>
     *
     * @param enabled 로그 출력 활성화 여부입니다.<br><br>
     *                Whether to enable logging.<br>
     */
    fun setLogging(enabled: Boolean) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(isLogging = enabled)
        }
    }

    /**
     * 현재 로그 출력 활성화 여부를 반환합니다.<br><br>
     * Returns current logging enabled state.<br>
     */
    fun isLogging(): Boolean = snapshot.isLogging

    /**
     * 허용할 로그 타입 목록을 설정합니다.<br><br>
     * Sets allowed log types (allowlist).<br>
     *
     * @param types 허용할 로그 타입 집합입니다.<br><br>
     *              Allowed log types.<br>
     */
    fun setLogTypes(types: Set<LogType>) {
        val sanitized = Collections.unmodifiableSet(types.toSet())
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(logTypes = sanitized)
        }
    }

    /**
     * 허용된 로그 타입 집합을 반환합니다.<br><br>
     * Returns allowed log types.<br>
     */
    fun getLogTypes(): Set<LogType> = snapshot.logTypes

    /**
     * 태그 차단 목록 필터 활성화 여부를 설정합니다.<br><br>
     * Enables or disables tag blocklist filtering.<br>
     *
     * @param enabled 차단 목록 필터 활성화 여부입니다.<br><br>
     *                Whether to enable blocklist filtering.<br>
     */
    fun setLogTagBlockListEnabled(enabled: Boolean) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(isLogTagBlockListEnabled = enabled)
        }
    }

    /**
     * 태그 차단 목록 필터 활성화 여부를 반환합니다.<br><br>
     * Returns whether tag blocklist filtering is enabled.<br>
     */
    fun isLogTagBlockListEnabled(): Boolean = snapshot.isLogTagBlockListEnabled

    /**
     * 태그 차단 목록을 설정합니다.<br><br>
     * Sets tag blocklist entries.<br>
     *
     * @param tags 차단할 태그 집합입니다.<br><br>
     *             Tags to block.<br>
     */
    fun setLogTagBlockList(tags: Set<String>) {
        val sanitized = Collections.unmodifiableSet(tags.toSet())
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(logTagBlockList = sanitized)
        }
    }

    /**
     * 현재 태그 차단 목록을 반환합니다.<br><br>
     * Returns current tag blocklist entries.<br>
     */
    fun getLogTagBlockList(): Set<String> = snapshot.logTagBlockList

    /**
     * 파일 저장 활성화 여부를 설정합니다.<br><br>
     * Sets file logging enabled state.<br>
     *
     * @param enabled 파일 저장 활성화 여부입니다.<br><br>
     *                Whether to enable file logging.<br>
     */
    fun setSaveEnabled(enabled: Boolean) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(isSaveEnabled = enabled)
        }
    }

    /**
     * 파일 저장 활성화 여부를 반환합니다.<br><br>
     * Returns file logging enabled state.<br>
     */
    fun isSaveEnabled(): Boolean = snapshot.isSaveEnabled

    /**
     * 파일 저장소 타입을 설정합니다.<br><br>
     * Sets storage type for file output.<br>
     *
     * @param type 사용할 저장소 타입입니다.<br><br>
     *             Storage type to use.<br>
     */
    fun setStorageType(type: LogStorageType) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(storageType = type)
        }
    }

    /**
     * 현재 저장소 타입을 반환합니다.<br><br>
     * Returns current storage type.<br>
     */
    fun getStorageType(): LogStorageType = snapshot.storageType

    /**
     * 사용자 지정 저장 디렉터리를 설정합니다.<br><br>
     * Sets custom save directory path.<br>
     *
     * @param path 저장 디렉터리 경로입니다. `null`이면 기본 경로를 사용합니다.<br><br>
     *             Directory path or null to use default.<br>
     */
    fun setSaveDirectory(path: String?) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(saveDirectory = path)
        }
    }

    /**
     * 사용자 지정 저장 디렉터리 경로를 반환합니다.<br><br>
     * Returns custom save directory path.<br>
     */
    fun getSaveDirectory(): String? = snapshot.saveDirectory

    /**
     * 로그 접두사와 파일명에 사용할 앱 이름을 설정합니다.<br><br>
     * Sets application name used in log prefix and file name.<br>
     *
     * @param name 앱 이름입니다.<br><br>
     *             Application name.<br>
     */
    fun setAppName(name: String) {
        synchronized(snapshotLock) {
            snapshot = snapshot.copy(appName = name)
        }
    }

    /**
     * 현재 앱 이름을 반환합니다.<br><br>
     * Returns current application name.<br>
     */
    fun getAppName(): String = snapshot.appName

    /**
     * 스택 트레이스 해석에서 제외할 패키지 접두사를 추가합니다.<br><br>
     * Adds package prefixes to skip during stack trace resolution.<br>
     *
     * @param packages 추가할 패키지 접두사 집합입니다.<br><br>
     *                 Package prefixes to add.<br>
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
     * 현재 제외 패키지 접두사 목록을 반환합니다.<br><br>
     * Returns current skip package prefixes.<br>
     */
    fun getSkipPackages(): Set<String> = snapshot.skipPackages

    /**
     * 현재 설정의 불변 스냅샷을 생성해 반환합니다.<br><br>
     * Creates an immutable snapshot of current configuration.<br>
     */
    fun snapshot(): LogxConfigSnapshot = snapshot
}
