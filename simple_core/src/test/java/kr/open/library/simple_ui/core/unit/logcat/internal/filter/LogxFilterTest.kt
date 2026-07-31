package kr.open.library.simple_ui.core.unit.logcat.internal.filter

import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigSnapshot
import kr.open.library.simple_ui.core.logcat.internal.filter.LogxFilter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the single Logx filtering policy implementation.<br><br>
 * 단일 Logx 필터 정책 구현의 전역·타입·태그 판정을 검증합니다.<br>
 */
class LogxFilterTest {
    /**
     * Verifies that global and type allowlist switches reject disallowed log entries.<br><br>
     * 전역 설정과 타입 허용 목록이 허용되지 않은 로그를 차단하는지 검증합니다.<br>
     */
    @Test
    fun isAllowedShouldRejectDisabledLoggingAndExcludedType() {
        assertFalse(LogxFilter.isAllowed(LogType.DEBUG, null, createConfig(isLogging = false)))
        assertFalse(LogxFilter.isAllowed(LogType.DEBUG, null, createConfig(logTypes = setOf(LogType.ERROR))))
        assertTrue(LogxFilter.isAllowed(LogType.ERROR, null, createConfig(logTypes = setOf(LogType.ERROR))))
    }

    /**
     * Verifies that tag blocking applies only to valid tags in the enabled blocklist.<br><br>
     * 태그 차단이 활성화된 목록의 유효한 태그에만 적용되는지 검증합니다.<br>
     */
    @Test
    fun isAllowedShouldApplyEnabledTagBlockList() {
        val config = createConfig(
            isLogTagBlockListEnabled = true,
            logTagBlockList = setOf(BLOCKED_TAG),
        )

        assertFalse(LogxFilter.isAllowed(LogType.DEBUG, BLOCKED_TAG, config))
        assertTrue(LogxFilter.isAllowed(LogType.DEBUG, ALLOWED_TAG, config))
        assertTrue(LogxFilter.isAllowed(LogType.DEBUG, null, config))
    }

    private fun createConfig(
        isLogging: Boolean = true,
        logTypes: Set<LogType> = enumValues<LogType>().toSet(),
        isLogTagBlockListEnabled: Boolean = false,
        logTagBlockList: Set<String> = emptySet(),
    ): LogxConfigSnapshot = LogxConfigSnapshot(
        isLogging = isLogging,
        logTypes = logTypes,
        isLogTagBlockListEnabled = isLogTagBlockListEnabled,
        logTagBlockList = logTagBlockList,
        isSaveEnabled = false,
        storageType = LogStorageType.APP_EXTERNAL,
        saveDirectory = null,
        appName = "TestApp",
        skipPackages = emptySet(),
    )

    private companion object {
        const val ALLOWED_TAG = "NETWORK"
        const val BLOCKED_TAG = "BLOCKED"
    }
}
