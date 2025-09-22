package kr.open.library.simple_ui.logcat.internal.formatter


import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.internal.formatter.base.LogxBaseFormatter
import kr.open.library.simple_ui.logcat.internal.formatter.base.LogxFormattedData
import kr.open.library.simple_ui.logcat.internal.formatter.base.LogxFormatterImp
import kr.open.library.simple_ui.logcat.internal.stacktrace.LogxStackTrace
import kr.open.library.simple_ui.logcat.model.LogxType


/**
 * logcat PARENT 전용 포맷터 부분 설정 및 반환
 */
class ParentLogFormatter(
    config: LogxConfig,
    private val stackTrace: LogxStackTrace,
    private val isExtensions: Boolean = false
) : LogxBaseFormatter(config), LogxFormatterImp {

    override fun isIncludeLogType(logType: LogxType): Boolean = logType == LogxType.PARENT

    override fun getTagSuffix(): String = "[PARENT]"

    override fun formatMessage(message: Any?, stackInfo: String): String = "┖${stackInfo}${message ?: ""}"
    
    /**
     * 부모 메서드 정보를 먼저 로그로 출력하기 위한 메서드
     */
    fun formatParentInfo(tag: String): LogxFormattedData? {
        if (!shouldFormat(LogxType.PARENT)) return null
        
        val parentInfo = getParentInfo()
        val formattedTag = createFormattedTag(tag)
        
        return LogxFormattedData(
            tag = formattedTag,
            message = "┎${parentInfo.getMsgFrontParent()}",
            logType = LogxType.PARENT
        )
    }
    
    private fun getParentInfo() = if (isExtensions) {
        stackTrace.getParentExtensionsStackTrace()
    } else {
        stackTrace.getParentStackTrace()
    }
}