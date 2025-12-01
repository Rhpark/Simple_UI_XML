package kr.open.library.simple_ui.core.logcat.internal.formatter.base

import kr.open.library.simple_ui.core.logcat.model.LogxType

/**
 * 포맷팅된 로그 데이터
 */
data class LogxFormattedData(
    val tag: String,
    val message: String,
    val logType: LogxType,
)
