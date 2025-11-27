package kr.open.library.simple_ui.core.logcat.config

import kr.open.library.simple_ui.core.logcat.model.LogxType
import java.util.EnumSet

/**
 * Core configuration data class for Logx logging system.<br>
 * This class intentionally avoids Android framework dependencies to enable pure unit testing without Robolectric.<br><br>
 * Logx 로깅 시스템의 핵심 설정 데이터 클래스입니다.<br>
 * 이 클래스는 Robolectric 없이 순수 단위 테스트를 가능하게 하기 위해 의도적으로 Android 프레임워크 의존성을 피합니다.<br>
 *
 * @property isDebug Enables or disables debug logging.<br><br>
 *                   디버그 로깅을 활성화 또는 비활성화합니다.
 *
 * @property isDebugFilter Enables tag-based filtering of debug logs.<br><br>
 *                         태그 기반 디버그 로그 필터링을 활성화합니다.
 *
 * @property isDebugSave Enables saving logs to a file.<br><br>
 *                       로그를 파일에 저장하는 기능을 활성화합니다.
 *
 * @property saveFilePath The absolute path where log files will be saved.<br><br>
 *                        로그 파일이 저장될 절대 경로입니다.
 *
 * @property storageType The type of storage to use for log files.<br><br>
 *                       로그 파일에 사용할 저장소 타입입니다.
 *
 * @property appName The application name used in log file naming and organization.<br><br>
 *                   로그 파일 이름 지정 및 구성에 사용되는 애플리케이션 이름입니다.
 *
 * @property debugFilterList A set of tag filters. Only logs matching these tags will be displayed when [isDebugFilter] is true.<br><br>
 *                           태그 필터 집합입니다. [isDebugFilter]가 true일 때 이 태그와 일치하는 로그만 표시됩니다.
 *
 * @property debugLogTypeList The set of log types to display (VERBOSE, DEBUG, INFO, WARN, ERROR, etc.).<br><br>
 *                            표시할 로그 타입 집합입니다 (VERBOSE, DEBUG, INFO, WARN, ERROR 등).
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
         * Creates a fallback configuration when Android Context is not available.<br>
         * Uses internal storage and default paths to ensure basic functionality.<br><br>
         * Android Context를 사용할 수 없을 때 폴백 설정을 생성합니다.<br>
         * 기본 기능을 보장하기 위해 내부 저장소와 기본 경로를 사용합니다.<br>
         *
         * @return A LogxConfig instance configured for fallback scenarios.<br><br>
         *         폴백 시나리오용으로 구성된 LogxConfig 인스턴스.<br>
         */
        fun createFallback(): LogxConfig = LogxConfig(
            saveFilePath = LogxPathUtils.getDefaultLogPath(),
            storageType = LogxStorageType.INTERNAL,
        )
    }
}
