package kr.open.library.simple_ui.core.logcat.internal.formatter.base

import kr.open.library.simple_ui.core.logcat.config.LogxConfig
import kr.open.library.simple_ui.core.logcat.model.LogxType

/**
 * logcat 기본 포맷터 부분 설정 및 반환 추상 클래스
 * → Parent TAG : appName [TAG] [PARENT] : formatMessage()
 * → ThreadId TAG : appName [TAG] [T_ID] : formatMessage()
 * → Json TAG : appName [TAG] [JSON] : formatMessage()
 * → Else : appName [TAG] : formatMessage()
 */
abstract class LogxBaseFormatter(
    private val config: LogxConfig,
) : LogxFormatterImp {
    final override fun format(
        tag: String,
        message: Any?,
        logType: LogxType,
        stackInfo: String,
    ): LogxFormattedData? =
        if (!shouldFormat(logType)) {
            null
        } else {
            LogxFormattedData(createFormattedTag(tag), formatMessage(message, stackInfo), logType)
        }

    protected open fun shouldFormat(logType: LogxType): Boolean =
        config.isDebug && isIncludeLogType(logType) && config.debugLogTypeList.contains(logType)

    protected fun createFormattedTag(tag: String): String = "${config.appName}[$tag]${getTagSuffix()}"

    protected abstract fun getTagSuffix(): String

    protected abstract fun isIncludeLogType(logType: LogxType): Boolean

    protected abstract fun formatMessage(
        message: Any?,
        stackInfo: String,
    ): String
}
