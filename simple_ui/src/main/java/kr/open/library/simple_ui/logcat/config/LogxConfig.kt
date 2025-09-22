package kr.open.library.simple_ui.logcat.config

import android.content.Context
import kr.open.library.simple_ui.logcat.model.LogxType

import java.util.EnumSet

/**
 * Logx 라이브러리의 설정을 관리하는 데이터 클래스
 * 불변성과 타입 안전성을 보장
 */
data class LogxConfig(
    val isDebug: Boolean = true,
    val isDebugFilter: Boolean = false,
    val isDebugSave: Boolean = false,
    val saveFilePath: String = LogxPathUtils.getDefaultLogPath(),
    val storageType: LogxStorageType = LogxStorageType.APP_EXTERNAL,
    val appName: String = "RhPark",
    val debugFilterList: Set<String> = emptySet(),
    val debugLogTypeList: EnumSet<LogxType> = EnumSet.allOf(LogxType::class.java)
//    val debugLogTypeList: List<LogxType> = listOf(
//        LogxType.VERBOSE,
//        LogxType.DEBUG,
//        LogxType.INFO,
//        LogxType.WARN,
//        LogxType.ERROR,
//        LogxType.PARENT,
//        LogxType.JSON,
//        LogxType.THREAD_ID,
//    )
) {
    companion object {

        /**
         * Context 기반 최적 설정 생성 (권장)
         */
        fun createDefault(context: Context, storageType: LogxStorageType = LogxStorageType.APP_EXTERNAL): LogxConfig {
            return LogxConfig(
                saveFilePath = LogxPathUtils.getLogPath(context, storageType),
                storageType = storageType
            )
        }

        /**
         * 내부 저장소 전용 설정
         */
        fun createInternal(context: Context): LogxConfig {
            return LogxConfig(
                saveFilePath = LogxPathUtils.getInternalLogPath(context),
                storageType = LogxStorageType.INTERNAL
            )
        }

        /**
         * 앱 전용 외부 저장소 설정 (권한 불필요)
         */
        fun createAppExternal(context: Context): LogxConfig {
            return LogxConfig(
                saveFilePath = LogxPathUtils.getAppExternalLogPath(context),
                storageType = LogxStorageType.APP_EXTERNAL
            )
        }

        /**
         * 공용 외부 저장소 설정 (권한 필요)
         */
        fun createPublicExternal(context: Context): LogxConfig {
            return LogxConfig(
                saveFilePath = LogxPathUtils.getPublicExternalLogPath(context),
                storageType = LogxStorageType.PUBLIC_EXTERNAL
            )
        }

        /**
         * Context 없을 때 fallback 설정
         */
        fun createFallback(): LogxConfig {
            return LogxConfig(
                saveFilePath = LogxPathUtils.getDefaultLogPath(),
                storageType = LogxStorageType.INTERNAL
            )
        }

        /**
         * 저장소 타입별 설정 생성 헬퍼
         */
        fun create(context: Context, storageType: LogxStorageType): LogxConfig {
            return when (storageType) {
                LogxStorageType.INTERNAL -> createInternal(context)
                LogxStorageType.APP_EXTERNAL -> createAppExternal(context)
                LogxStorageType.PUBLIC_EXTERNAL -> createPublicExternal(context)
            }
        }
    }
}
