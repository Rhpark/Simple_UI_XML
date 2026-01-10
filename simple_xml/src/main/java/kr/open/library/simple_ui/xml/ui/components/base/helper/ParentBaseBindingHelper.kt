package kr.open.library.simple_ui.xml.ui.components.base.helper

import kr.open.library.simple_ui.core.logcat.Logx

/**
 * Helper class to manage the lifecycle of ViewModel event collection.<br>
 * Prevents duplicate event collection by tracking the start state.<br><br>
 *
 * ViewModel 이벤트 수집의 생명주기를 관리하는 헬퍼 클래스입니다.<br>
 * 시작 상태를 추적하여 중복 이벤트 수집을 방지합니다.<br>
 */
internal abstract class ParentBaseBindingHelper {
    /**
     * Tracks whether ViewModel event collection has already started for this component instance.<br><br>
     * 현재 컴포넌트 인스턴스에서 ViewModel 이벤트 수집이 이미 시작되었는지 여부를 추적합니다.<br>
     */
    protected var eventCollectStarted = false

    /**
     * Starts ViewModel event collection only once per component instance.<br>
     * Called from `onCreate()` for Activity or `onViewCreated()` for Fragment to prevent duplicate collectors.<br>
     * Subsequent calls are ignored and logged as warnings.<br><br>
     *
     * 컴포넌트 인스턴스당 ViewModel 이벤트 수집을 1회만 시작합니다.<br>
     * Activity의 경우 `onCreate()`에서, Fragment의 경우 `onViewCreated()`에서 호출되어 중복 수집을 방지합니다.<br>
     * 이후 호출은 무시되며 경고 로그를 남깁니다.<br>
     *
     * @param onEventVmCollect The lambda to execute for starting event collection.<br><br>
     *                         이벤트 수집 시작을 위해 실행할 람다.<br>
     */
    public fun startEventVmCollect(onEventVmCollect: () -> Unit) {
        if (eventCollectStarted) {
            Logx.w("Already started event collection.")
            return
        }
        eventCollectStarted = true
        onEventVmCollect()
    }
}
