package kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard

/**
 * Resize handling policy when configuring keyboard layout behavior.<br><br>
 * 키보드 레이아웃 동작을 설정할 때 사용할 resize 정책입니다.<br>
 */
public enum class SoftKeyboardResizePolicy {
    /**
     * Keep current window policy on API 30+, legacy adjust resize on API 29-.<br><br>
     * API 30+에서는 현재 윈도우 정책을 유지하고, API 29-에서는 레거시 adjust resize를 적용합니다.<br>
     */
    KEEP_CURRENT_WINDOW,

    /**
     * Force legacy `SOFT_INPUT_ADJUST_RESIZE` on all API levels.<br><br>
     * 모든 API에서 레거시 `SOFT_INPUT_ADJUST_RESIZE`를 강제합니다.<br>
     */
    LEGACY_ADJUST_RESIZE,

    /**
     * Force `WindowCompat.setDecorFitsSystemWindows(window, true)` on API 30+, fallback to legacy below.<br><br>
     * API 30+에서는 `WindowCompat.setDecorFitsSystemWindows(window, true)`를 강제하고, 이하 버전은 레거시 방식으로 처리합니다.<br>
     */
    FORCE_DECOR_FITS_TRUE,
}
