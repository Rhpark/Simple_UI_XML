package kr.open.library.simple_ui.core.logcat.internal.writer

import android.content.Context
import android.util.Log
import java.io.File
import kr.open.library.simple_ui.core.logcat.config.LogxConfigSnapshot
import kr.open.library.simple_ui.core.logcat.internal.common.LogxPathResolver

/**
 * 로그 파일 저장 경로를 계산하고 디렉터리를 보장합니다.
 *
 * Resolves log file directory paths and ensures directories exist.
 * <br><br>
 * 로그 저장 경로를 계산하고 디렉터리를 생성합니다.
 */
internal class LogxFilePathResolver {
    /**
     * 로그 저장 디렉터리를 반환합니다.
     *
     * Resolves the log directory, creating it if needed.
     * <br><br>
     * 로그 디렉터리를 계산하고 필요 시 생성합니다.
     *
     * @param context 앱 컨텍스트.
     * @param config 현재 설정 스냅샷.
     * @param errorTag 오류 로그에 사용할 태그.
     */
    fun resolveDirectory(
        context: Context,
        config: LogxConfigSnapshot,
        errorTag: String,
    ): File? {
        val directoryPath = resolveDirectoryPath(context, config, errorTag) ?: return null
        val logDirectory = File(directoryPath)
        if (!logDirectory.exists() && !logDirectory.mkdirs()) {
            Log.e(errorTag, "Failed to create log directory: $directoryPath")
            return null
        }
        return logDirectory
    }

    /**
     * 디렉터리 경로 문자열을 계산합니다.
     *
     * Resolves the directory path string for log storage.
     * <br><br>
     * 로그 저장 경로 문자열을 계산합니다.
     *
     * @param context 앱 컨텍스트.
     * @param config 현재 설정 스냅샷.
     * @param errorTag 오류 로그에 사용할 태그.
     */
    private fun resolveDirectoryPath(
        context: Context,
        config: LogxConfigSnapshot,
        errorTag: String,
    ): String? {
        if (config.saveDirectory != null) {
            return config.saveDirectory
        }

        if (LogxPathResolver.requiresPermission(config.storageType) &&
            !LogxPathResolver.hasWritePermission(context, config.storageType)
        ) {
            Log.e(errorTag, "Storage permission required for PUBLIC_EXTERNAL on API 28 and below.")
            return null
        }

        return LogxPathResolver.resolvePath(context, config.storageType)
    }
}


