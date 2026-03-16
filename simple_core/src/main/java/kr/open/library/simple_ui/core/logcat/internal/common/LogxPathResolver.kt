package kr.open.library.simple_ui.core.logcat.internal.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import java.io.File

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
    fun requiresPermission(storageType: LogStorageType): Boolean = checkSdkVersion(Build.VERSION_CODES.Q,
        positiveWork = { false },
        negativeWork = { storageType == LogStorageType.PUBLIC_EXTERNAL }
    )

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
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns whether a custom directory path is supported for file logging on the current platform.<br><br>
     * 현재 플랫폼에서 사용자 지정 디렉터리 경로를 파일 로그 저장용으로 지원하는지 반환합니다.<br>
     *
     * On Android 10+ only app-internal or app-specific external directories are supported for
     * direct file writes without SAF/MediaStore.<br><br>
     * Android 10+에서는 SAF/MediaStore 없이 직접 파일 쓰기를 수행할 수 있는 앱 내부 또는
     * 앱 전용 외부 디렉터리만 지원합니다.<br>
     *
     * @param context Application context.<br><br>
     *                애플리케이션 컨텍스트.<br>
     * @param directory Candidate custom directory.<br><br>
     *                  확인할 사용자 지정 디렉터리 후보입니다.<br>
     */
    fun isSupportedCustomDirectory(context: Context, directory: File): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true

        val candidatePath = directory.absolutePath
        val allowedRoots = buildList {
            add(context.filesDir.absolutePath)
            context
                .getExternalFilesDirs(null)
                .mapNotNull { it?.absolutePath }
                .forEach { add(it) }
        }

        return allowedRoots.any { root -> candidatePath == root || candidatePath.startsWith(root + File.separator) }
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
    private fun getAppExternalLogPath(context: Context): String =
        context.getExternalFilesDir(LogxConstants.LOG_DIR_NAME)?.absolutePath ?: getInternalLogPath(context)

    /**
     * Returns the log path for [LogStorageType.PUBLIC_EXTERNAL].<br>
     * Uses true public external storage on API 28 and below, and falls back to the app-specific
     * external Documents directory on API 29+ due to scoped storage.<br>
     * If the API 29+ Documents directory is unavailable, it falls back again to the app-specific
     * external log path.<br><br>
     * [LogStorageType.PUBLIC_EXTERNAL]용 로그 경로를 반환합니다.<br>
     * API 28 이하에서는 실제 공용 외부 저장소를 사용하고, API 29+에서는 Scoped Storage 정책에 따라
     * 앱 전용 외부 Documents 디렉터리로 대체됩니다.<br>
     * API 29+에서 Documents 디렉터리를 사용할 수 없으면 앱 전용 외부 로그 경로로 한 번 더 대체됩니다.<br>
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
