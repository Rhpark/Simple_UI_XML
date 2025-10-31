package kr.open.library.simple_ui.logcat.config

import kr.open.library.simple_ui.logcat.model.LogxType
import java.util.EnumSet

/**
 * Core configuration data for Logx logging.
 *
 * This file intentionally keeps only logic that is free from Android framework
 * dependencies so that it can be covered entirely by plain unit tests.
 */
data class LogxConfig(
    val isDebug: Boolean = true,
    val isDebugFilter: Boolean = false,
    val isDebugSave: Boolean = false,
    val saveFilePath: String = LogxPathUtils.getDefaultLogPath(),
    val storageType: LogxStorageType = LogxStorageType.APP_EXTERNAL,
    val appName: String = "RhPark",
    val debugFilterList: Set<String> = emptySet(),
    val debugLogTypeList: EnumSet<LogxType> = EnumSet.allOf(LogxType::class.java),
) {
    companion object {
        /**
         * Fallback configuration used when no [android.content.Context] is available.
         */
        fun createFallback(): LogxConfig = LogxConfig(
            saveFilePath = LogxPathUtils.getDefaultLogPath(),
            storageType = LogxStorageType.INTERNAL,
        )
    }
}
