package kr.open.library.simple_ui.core.unit.logcat.internal.filter

import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigSnapshot
import kr.open.library.simple_ui.core.logcat.internal.filter.LogxFilter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogxFilterTest {
    @Test
    fun allowsWhenEnabledAndNotBlocked() {
        val config = LogxConfigSnapshot(
            isLogging = true,
            logTypes = setOf(LogType.DEBUG, LogType.INFO),
            isLogTagBlockListEnabled = true,
            logTagBlockList = setOf("BLOCK"),
            isSaveEnabled = false,
            storageType = LogStorageType.INTERNAL,
            saveDirectory = null,
            appName = "AppName",
            skipPackages = emptySet(),
        )

        assertTrue(LogxFilter.isAllowed(LogType.DEBUG, "OK", config))
        assertFalse(LogxFilter.isAllowed(LogType.DEBUG, "BLOCK", config))
        assertFalse(LogxFilter.isAllowed(LogType.WARN, "OK", config))
        assertTrue(LogxFilter.isAllowed(LogType.DEBUG, null, config))
    }

    @Test
    fun blocksWhenLoggingDisabled() {
        val config = LogxConfigSnapshot(
            isLogging = false,
            logTypes = setOf(LogType.DEBUG),
            isLogTagBlockListEnabled = false,
            logTagBlockList = emptySet(),
            isSaveEnabled = false,
            storageType = LogStorageType.INTERNAL,
            saveDirectory = null,
            appName = "AppName",
            skipPackages = emptySet(),
        )

        assertFalse(LogxFilter.isAllowed(LogType.DEBUG, "TAG", config))
    }
}
