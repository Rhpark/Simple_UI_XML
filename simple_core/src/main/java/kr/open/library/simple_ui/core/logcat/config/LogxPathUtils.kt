package kr.open.library.simple_ui.core.logcat.config

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion

/**
 * Defines the storage type for log files.<br><br>
 * 로그 파일의 저장소 타입을 정의합니다.<br>
 */
enum class LogxStorageType {
    /**
     * App internal storage (no permission required).<br><br>
     * 앱 내부 저장소 (권한 불필요).<br>
     */
    INTERNAL,

    /**
     * App-specific external storage (no permission required, accessible via file manager).<br><br>
     * 앱 전용 외부 저장소 (권한 불필요, 파일 관리자로 접근 가능).<br>
     */
    APP_EXTERNAL,

    /**
     * Public external storage (permission required on API 28 and below).<br><br>
     * 공용 외부 저장소 (API 28 이하에서 권한 필요).<br>
     */
    PUBLIC_EXTERNAL,
}

/**
 * Utility object for managing log file paths across different storage types.<br><br>
 * 다양한 저장소 타입에서 로그 파일 경로를 관리하는 유틸리티 객체입니다.<br>
 */
object LogxPathUtils {
    private val LOG_DIR_NAME = "AppLogs"

    /**
     * Returns a safe default log path as a fallback when Context is not available.<br>
     * This path is hardcoded and may not be accessible on all devices.<br><br>
     * Context를 사용할 수 없을 때 폴백으로 사용할 안전한 기본 로그 경로를 반환합니다.<br>
     * 이 경로는 하드코딩되어 있으며 모든 기기에서 접근 가능하지 않을 수 있습니다.<br>
     *
     * @return A hardcoded fallback log path.<br><br>
     *         하드코딩된 폴백 로그 경로.<br>
     */
    @SuppressLint("SdCardPath")
    fun getDefaultLogPath(): String = "/data/data/$LOG_DIR_NAME"

    /**
     * Returns the log file path based on the specified storage type.<br><br>
     * 지정된 저장소 타입에 따라 로그 파일 경로를 반환합니다.<br>
     *
     * @param context The Android context for accessing storage paths.<br><br>
     *                저장소 경로에 접근하기 위한 Android 컨텍스트.
     *
     * @param storageType The type of storage to use for log files.<br><br>
     *                    로그 파일에 사용할 저장소 타입.
     *
     * @return The absolute path to the log directory for the specified storage type.<br><br>
     *         지정된 저장소 타입의 로그 디렉토리에 대한 절대 경로.<br>
     */
    fun getLogPath(
        context: Context,
        storageType: LogxStorageType,
    ): String =
        when (storageType) {
            LogxStorageType.INTERNAL -> getInternalLogPath(context)
            LogxStorageType.APP_EXTERNAL -> getAppExternalLogPath(context)
            LogxStorageType.PUBLIC_EXTERNAL -> getPublicExternalLogPath(context)
        }

    /**
     * Returns the log path in app internal storage.<br>
     * This storage is always available and requires no permissions, but is not accessible to users.<br><br>
     * 앱 내부 저장소의 로그 경로를 반환합니다.<br>
     * 이 저장소는 항상 사용 가능하고 권한이 필요 없지만, 사용자가 직접 접근할 수 없습니다.<br>
     *
     * @param context The Android context.<br><br>
     *                Android 컨텍스트.
     *
     * @return The absolute path to the internal log directory.<br><br>
     *         내부 로그 디렉토리의 절대 경로.<br>
     */
    fun getInternalLogPath(context: Context): String = context.filesDir.absolutePath + "/$LOG_DIR_NAME"

    /**
     * Returns the log path in app-specific external storage.<br>
     * This storage requires no permissions and is accessible via file manager, but is deleted when the app is uninstalled.<br><br>
     * 앱 전용 외부 저장소의 로그 경로를 반환합니다.<br>
     * 이 저장소는 권한이 필요 없고 파일 관리자로 접근 가능하지만, 앱 삭제 시 함께 삭제됩니다.<br>
     *
     * @param context The Android context.<br><br>
     *                Android 컨텍스트.
     *
     * @return The absolute path to the app-specific external log directory, or falls back to internal storage if unavailable.<br><br>
     *         앱 전용 외부 로그 디렉토리의 절대 경로, 사용 불가 시 내부 저장소로 폴백.<br>
     */
    fun getAppExternalLogPath(context: Context): String {
        val externalDir = context.getExternalFilesDir(LOG_DIR_NAME)
        return if (externalDir != null) {
            externalDir.absolutePath
        } else {
            getInternalLogPath(context) // Fallback to internal | 내부 저장소로 폴백
        }
    }

    /**
     * Returns the log path in public external storage.<br>
     * Behavior differs by Android version:<br>
     * - API 29+: Uses scoped storage (app-specific external Documents directory)<br>
     * - API 28 and below: Uses legacy external storage (requires WRITE_EXTERNAL_STORAGE permission)<br><br>
     * 공용 외부 저장소의 로그 경로를 반환합니다.<br>
     * Android 버전에 따라 동작이 다릅니다:<br>
     * - API 29+: 범위 지정 저장소 사용 (앱 전용 외부 Documents 디렉토리)<br>
     * - API 28 이하: 레거시 외부 저장소 사용 (WRITE_EXTERNAL_STORAGE 권한 필요)<br>
     *
     * @param context The Android context.<br><br>
     *                Android 컨텍스트.
     *
     * @return The absolute path to the public external log directory, with fallback to app-specific external storage.<br><br>
     *         공용 외부 로그 디렉토리의 절대 경로, 사용 불가 시 앱 전용 외부 저장소로 폴백.<br>
     */
    fun getPublicExternalLogPath(context: Context): String =
        checkSdkVersion(
            Build.VERSION_CODES.Q,
            positiveWork = {
                // API 29+: Scoped storage | 범위 지정 저장소
                val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                if (documentsDir != null) {
                    documentsDir.absolutePath + "/$LOG_DIR_NAME"
                } else {
                    getAppExternalLogPath(context)
                }
            },
            negativeWork = {
                // API 28 and below: Legacy external storage | API 28 이하: 레거시 외부 저장소
                @Suppress("DEPRECATION")
                Environment.getExternalStorageDirectory().absolutePath + "/$LOG_DIR_NAME"
            },
        )

    /**
     * Checks whether the specified storage type requires runtime permissions.<br><br>
     * 지정된 저장소 타입이 런타임 권한을 필요로 하는지 확인합니다.<br>
     *
     * @param storageType The storage type to check.<br><br>
     *                    확인할 저장소 타입.
     *
     * @return `true` if WRITE_EXTERNAL_STORAGE permission is required (only for PUBLIC_EXTERNAL on API 28 and below), `false` otherwise.<br><br>
     *         WRITE_EXTERNAL_STORAGE 권한이 필요하면 `true` (API 28 이하에서 PUBLIC_EXTERNAL인 경우만), 그 외는 `false`.<br>
     */
    fun requiresPermission(storageType: LogxStorageType): Boolean =
        when (storageType) {
            LogxStorageType.INTERNAL -> false
            LogxStorageType.APP_EXTERNAL -> false
            LogxStorageType.PUBLIC_EXTERNAL -> Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
        }

    /**
     * Checks whether log files in the specified storage type are accessible to users via file manager.<br><br>
     * 지정된 저장소 타입의 로그 파일을 사용자가 파일 관리자를 통해 접근할 수 있는지 확인합니다.<br>
     *
     * @param storageType The storage type to check.<br><br>
     *                    확인할 저장소 타입.
     *
     * @return `true` if users can access the files via file manager, `false` if only accessible programmatically.<br><br>
     *         사용자가 파일 관리자로 파일에 접근할 수 있으면 `true`, 프로그래밍 방식으로만 접근 가능하면 `false`.<br>
     */
    fun isUserAccessible(storageType: LogxStorageType): Boolean =
        when (storageType) {
            LogxStorageType.INTERNAL -> false // Not accessible to users | 사용자가 직접 접근 불가
            LogxStorageType.APP_EXTERNAL -> true // Accessible via file manager | 파일 관리자로 접근 가능
            LogxStorageType.PUBLIC_EXTERNAL -> true // Easily accessible via file manager | 파일 관리자로 쉽게 접근 가능
        }
}
