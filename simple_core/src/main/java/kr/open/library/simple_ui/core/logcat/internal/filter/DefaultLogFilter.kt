package kr.open.library.simple_ui.core.logcat.internal.filter

import kr.open.library.simple_ui.core.logcat.config.LogxConfig
import kr.open.library.simple_ui.core.logcat.internal.filter.base.LogFilterImp

/**
 * 기본 로그 필터 구현체
 * config.isDebugFilter == false 이면
 *  → Logcat 출력
 * config.isDebugFilter == true 이면
 *  → TAG 또는 FileName 이 config.debugFilterList 에 포함되어 있으면 Logcat 출력
 */
class DefaultLogFilter(
    private val config: LogxConfig,
) : LogFilterImp {
    override fun shouldLog(tag: String, fileName: String): Boolean = if (!config.isDebugFilter) {
        true
    } else {
        isTagAllowed(tag) || isFileNameAllowed(fileName)
    }

    private fun isTagAllowed(tag: String): Boolean = config.debugFilterList.contains(tag)

    private fun isFileNameAllowed(fileName: String): Boolean = config.debugFilterList.contains(fileName)
}
