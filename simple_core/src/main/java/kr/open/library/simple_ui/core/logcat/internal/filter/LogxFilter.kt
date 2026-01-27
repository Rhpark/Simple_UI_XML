package kr.open.library.simple_ui.core.logcat.internal.filter

import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigSnapshot
import kr.open.library.simple_ui.core.logcat.internal.common.LogxTagHelper

/**
 * Log 출력 가능 여부를 판정하는 내부 필터 유틸리티입니다.
 *
 * Determines whether a log entry should be emitted based on config rules.
 * <br><br>
 * 설정 규칙에 따라 로그 출력 여부를 결정합니다.
 */
internal object LogxFilter {
    /**
     * 입력된 타입/태그/설정값을 기준으로 로그 출력 허용 여부를 반환합니다.
     *
     * Returns whether the log entry is allowed under the current config.
     * <br><br>
     * 현재 설정 기준으로 로그 출력을 허용할지 반환합니다.
     *
     * @param type 로그 타입.
     * @param tag 로그 태그(없을 수 있음).
     * @param config 현재 스냅샷 설정.
     */
    fun isAllowed(type: LogType, tag: String?, config: LogxConfigSnapshot): Boolean {
        if (!config.isLogging) return false
        if (!config.logTypes.contains(type)) return false
        if (!config.isLogTagBlockListEnabled) return true
        if (!LogxTagHelper.isValidTag(tag)) return true
        return !config.logTagBlockList.contains(tag)
    }
}


