package kr.open.library.simple_ui.core.logcat.internal.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.config.LogStorageType

/**
 * Resolves log directory paths for different storage types.<br><br>
 * 저장소 타입별 로그 디렉터리 경로를 계산한다.<br>
 */
internal object LogxPathResolver {
    /**
     * Resolves absolute path for the given storage type.<br><br>
     * 지정된 저장소 타입의 절대 경로를 반환한다.<br>
     *
     * @param context Application context.<br><br>
     *                애플리케이션 컨텍스트.<br>
     * @param storageType Storage target.<br><br>
     *                    저장소 대상.<br>
     */
    fun resolvePath(context: Context, storageType: LogStorageType): String = when (storageType) {
        LogStorageType.INTERNAL -> getInternalLogPath(context)
        LogStorageType.APP_EXTERNAL -> getAppExternalLogPath(context)
        LogStorageType.PUBLIC_EXTERNAL -> getPublicExternalLogPath(context)
    }

    /**
     * Returns whether the given storage type requires runtime permission.<br><br>
     * 지정된 저장소 타입이 런타임 권한을 요구하는지 반환한다.<br>
     *
     * @param storageType Storage target.<br><br>
     *                    저장소 대상.<br>
     */
    fun requiresPermission(storageType: LogStorageType): Boolean =
        storageType == LogStorageType.PUBLIC_EXTERNAL && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P

    /**
     * Checks WRITE_EXTERNAL_STORAGE permission if required.<br><br>
     * 필요한 경우 WRITE_EXTERNAL_STORAGE 권한 보유 여부를 확인한다.<br>
     *
     * @param context Application context.<br><br>
     *                애플리케이션 컨텍스트.<br>
     * @param storageType Storage target.<br><br>
     *                    저장소 대상.<br>
     */
    fun hasWritePermission(context: Context, storageType: LogStorageType): Boolean {
        if (!requiresPermission(storageType)) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns internal storage log path.<br><br>
     * 내부 저장소 로그 경로를 반환한다.<br>
     */
    private fun getInternalLogPath(context: Context): String = context.filesDir.absolutePath + "/${LogxConstants.LOG_DIR_NAME}"

    /**
     * Returns app-specific external storage log path.<br><br>
     * 앱 전용 외부 저장소 로그 경로를 반환한다.<br>
     */
    private fun getAppExternalLogPath(context: Context): String {
        val externalDir = context.getExternalFilesDir(LogxConstants.LOG_DIR_NAME)
        return externalDir?.absolutePath ?: getInternalLogPath(context)
    }

    /**
     * Returns public external storage log path with API branching.<br><br>
     * API 분기에 따라 공용 외부 저장소 로그 경로를 반환한다.<br>
     */
    private fun getPublicExternalLogPath(context: Context): String = checkSdkVersion(
        Build.VERSION_CODES.Q,
        positiveWork = {
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (documentsDir != null) {
                documentsDir.absolutePath + "/${LogxConstants.LOG_DIR_NAME}"
            } else {
                getAppExternalLogPath(context)
            }
        },
        negativeWork = {
            @Suppress("DEPRECATION")
            Environment.getExternalStorageDirectory().absolutePath + "/${LogxConstants.LOG_DIR_NAME}"
        },
    )
}
