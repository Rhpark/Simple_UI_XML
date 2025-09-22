package kr.open.library.simple_ui.logcat.config

import android.content.Context
import android.os.Build
import android.os.Environment
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion

/**
 * 저장소 타입 정의
 */
enum class LogxStorageType {
    INTERNAL,           // 앱 내부 저장소
    APP_EXTERNAL,       // 앱 전용 외부 저장소 (권한 불필요)
    PUBLIC_EXTERNAL     // 공용 외부 저장소 (권한 필요)
}

/**
 * 로그 파일 경로 관련 유틸리티
 */
object LogxPathUtils {

    private val LOG_DIR_NAME = "AppLogs"
    /**
     * 안전한 기본 로그 경로 (Context 없을 때 fallback)
     */
    fun getDefaultLogPath(): String {
        return "/data/data/$LOG_DIR_NAME"
    }

    /**
     * 저장소 타입별 로그 경로 반환
     */
    fun getLogPath(context: Context, storageType: LogxStorageType): String {
        return when (storageType) {
            LogxStorageType.INTERNAL -> getInternalLogPath(context)
            LogxStorageType.APP_EXTERNAL -> getAppExternalLogPath(context)
            LogxStorageType.PUBLIC_EXTERNAL -> getPublicExternalLogPath(context)
        }
    }

    /**
     * 앱 내부 저장소 경로 (항상 권한 불필요)
     */
    fun getInternalLogPath(context: Context): String {
        return context.filesDir.absolutePath + "/$LOG_DIR_NAME"
    }

    /**
     * 앱 전용 외부 저장소 경로 (권한 불필요)
     */
    fun getAppExternalLogPath(context: Context): String {
        return context.getExternalFilesDir(LOG_DIR_NAME)?.absolutePath
            ?: getInternalLogPath(context) // fallback to internal
    }

    /**
     * 공용 외부 저장소 경로 (권한 필요)
     */
    fun getPublicExternalLogPath(context: Context): String {
        return checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = {
                (context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/$LOG_DIR_NAME")
                    ?: getAppExternalLogPath(context)
            },
            negativeWork = {
                // API 28 이하: 전통적인 외부 저장소
                @Suppress("DEPRECATION")
                Environment.getExternalStorageDirectory().absolutePath + "/$LOG_DIR_NAME"
            }
        )
    }

    /**
     * 저장소 타입별 권한 필요 여부 확인
     */
    fun requiresPermission(storageType: LogxStorageType): Boolean {
        return when (storageType) {
            LogxStorageType.INTERNAL -> false
            LogxStorageType.APP_EXTERNAL -> false
            LogxStorageType.PUBLIC_EXTERNAL -> Build.VERSION.SDK_INT <= Build.VERSION_CODES.P // API 28 이하에서만 권한 필요
        }
    }

    /**
     * 저장소 타입별 사용자 접근 가능 여부
     */
    fun isUserAccessible(storageType: LogxStorageType): Boolean {
        return when (storageType) {
            LogxStorageType.INTERNAL -> false          // 사용자가 직접 접근 불가
            LogxStorageType.APP_EXTERNAL -> true       // 파일 관리자로 접근 가능
            LogxStorageType.PUBLIC_EXTERNAL -> true    // 파일 관리자로 쉽게 접근 가능
        }
    }
}