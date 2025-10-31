package kr.open.library.simple_ui.logcat.config

import android.content.Context

/**
 * Android-context dependent factory helpers for [LogxConfig].
 * These methods are grouped separately to make Robolectric coverage management easier.
 */
object LogxConfigFactory {

    fun createDefault(
        context: Context,
        storageType: LogxStorageType = LogxStorageType.APP_EXTERNAL,
    ): LogxConfig = LogxConfig(
        saveFilePath = LogxPathUtils.getLogPath(context, storageType),
        storageType = storageType,
    )

    fun createInternal(context: Context): LogxConfig = LogxConfig(
        saveFilePath = LogxPathUtils.getInternalLogPath(context),
        storageType = LogxStorageType.INTERNAL,
    )

    fun createAppExternal(context: Context): LogxConfig = LogxConfig(
        saveFilePath = LogxPathUtils.getAppExternalLogPath(context),
        storageType = LogxStorageType.APP_EXTERNAL,
    )

    fun createPublicExternal(context: Context): LogxConfig = LogxConfig(
        saveFilePath = LogxPathUtils.getPublicExternalLogPath(context),
        storageType = LogxStorageType.PUBLIC_EXTERNAL,
    )

    fun create(
        context: Context,
        storageType: LogxStorageType,
    ): LogxConfig = when (storageType) {
        LogxStorageType.INTERNAL -> createInternal(context)
        LogxStorageType.APP_EXTERNAL -> createAppExternal(context)
        LogxStorageType.PUBLIC_EXTERNAL -> createPublicExternal(context)
    }
}
