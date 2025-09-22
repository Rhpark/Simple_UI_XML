package kr.open.library.simple_ui.logcat.internal.formatter

import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.internal.formatter.base.LogxBaseFormatter
import kr.open.library.simple_ui.logcat.internal.formatter.base.LogxFormatterImp
import kr.open.library.simple_ui.logcat.model.LogxType


/**
 * logcat THREAD_ID 전용 포맷터 부분 설정 및 반환
 */
class ThreadIdLogFormatter(config: LogxConfig) :
    LogxBaseFormatter(config), LogxFormatterImp {

    override fun getTagSuffix(): String = "[T_ID]"

    override fun isIncludeLogType(logType: LogxType): Boolean = logType == LogxType.THREAD_ID

    override fun formatMessage(message: Any?, stackInfo: String): String =
        "[${Thread.currentThread().id}]${stackInfo}${message ?: ""}"
}