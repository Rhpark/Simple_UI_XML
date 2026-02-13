package kr.open.library.simple_ui.xml.system_manager.controller.systembar.model

import android.graphics.Rect

/**
 * Represents current visibility-aware state of a system bar.<br>
 * Distinguishes between not-ready, not-present, hidden, and visible states explicitly.<br><br>
 * 시스템 바의 현재 가시성 기반 상태를 나타냅니다.<br>
 * 준비되지 않음, 미존재, 숨김, 표시 상태를 명시적으로 구분합니다.<br>
 */
public sealed interface SystemBarVisibleState {
    /**
     * Window/view/insets are not ready yet (too early call).<br><br>
     * Window/view/insets가 아직 준비되지 않았습니다 (너무 이른 호출).<br>
     */
    public data object NotReady : SystemBarVisibleState

    /**
     * System bar area does not exist on this device/mode.<br><br>
     * 현재 디바이스/모드에서 시스템 바 영역이 존재하지 않습니다.<br>
     */
    public data object NotPresent : SystemBarVisibleState

    /**
     * System bar exists but is currently hidden.<br><br>
     * 시스템 바는 존재하지만 현재 숨겨진 상태입니다.<br>
     */
    public data object Hidden : SystemBarVisibleState

    /**
     * System bar is visible with current window coordinates.<br><br>
     * 시스템 바가 표시 중이며 현재 윈도우 좌표를 제공합니다.<br>
     */
    public data class Visible(
        public val rect: Rect
    ) : SystemBarVisibleState
}

/**
 * Represents stable (system-defined) state of a system bar.<br>
 * Distinguishes between not-ready, not-present, and stable-present states explicitly.<br><br>
 * 시스템 바의 stable(시스템 정의) 상태를 나타냅니다.<br>
 * 준비되지 않음, 미존재, stable 존재 상태를 명시적으로 구분합니다.<br>
 */
public sealed interface SystemBarStableState {
    /**
     * Window/view/insets are not ready yet (too early call).<br><br>
     * Window/view/insets가 아직 준비되지 않았습니다 (너무 이른 호출).<br>
     */
    public data object NotReady : SystemBarStableState

    /**
     * System bar stable area does not exist on this device/mode.<br><br>
     * 현재 디바이스/모드에서 시스템 바 stable 영역이 존재하지 않습니다.<br>
     */
    public data object NotPresent : SystemBarStableState

    /**
     * Stable system bar area is available with window coordinates.<br><br>
     * stable 시스템 바 영역이 존재하며 윈도우 좌표를 제공합니다.<br>
     */
    public data class Stable(
        public val rect: Rect
    ) : SystemBarStableState
}
