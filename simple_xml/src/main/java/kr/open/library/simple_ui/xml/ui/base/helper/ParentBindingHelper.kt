package kr.open.library.simple_ui.xml.ui.base.helper

import kr.open.library.simple_ui.core.logcat.Logx

/**
 * Helper to manage one-time ViewModel event collection.<br>
 * Prevents duplicate collectors by tracking the start state.<br><br>
 * ViewModel 이벤트 수집을 1회로 관리하는 헬퍼입니다.<br>
 * 시작 상태를 추적하여 중복 수집을 방지합니다.<br>
 */
internal abstract class ParentBindingHelper {
    /**
     * Tracks whether event collection has started for this instance.<br><br>
     * 이 인스턴스에서 이벤트 수집이 시작되었는지 여부를 저장합니다.<br>
     */
    protected var eventCollectStarted = false

    /**
     * Starts ViewModel event collection only once per instance.<br>
     * Subsequent calls are ignored and logged as warnings.<br><br>
     * 인스턴스당 1회만 ViewModel 이벤트 수집을 시작합니다.<br>
     * 이후 호출은 무시되며 경고 로그가 남습니다.<br>
     *
     * @param onEventVmCollect The lambda that starts event collection.<br><br>
     *                         이벤트 수집을 시작하는 람다.<br>
     */
    public fun startEventVmCollect(onEventVmCollect: () -> Unit) {
        if (eventCollectStarted) {
            Logx.w("Already started event collection.")
            return
        }
        eventCollectStarted = true
        onEventVmCollect()
    }

    /**
     * Returns whether event collection can start for this instance.<br><br>
     * 이 인스턴스에서 이벤트 수집을 시작할 수 있는지 반환합니다.<br>
     */
    public fun canStartEventCollect(): Boolean = !eventCollectStarted
}
