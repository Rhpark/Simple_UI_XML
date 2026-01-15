package kr.open.library.simple_ui.xml.ui.layout.base.bind.coordinator

import androidx.viewbinding.ViewBinding


/**
 * Callback interface for binding lifecycle hooks.<br><br>
 * 바인딩 생명주기 훅을 위한 콜백 인터페이스입니다.<br>
 */
internal interface LayoutBindingCallbacks<BINDING : ViewBinding> {
    /**
     * Creates a new binding instance for this layout.<br><br>
     * 이 레이아웃의 새 바인딩 인스턴스를 생성합니다.<br>
     *
     * @return The initialized ViewBinding instance. No logging is performed.<br><br>
     *         초기화된 ViewBinding 인스턴스를 반환합니다. 로깅은 수행하지 않습니다.<br>
     */
    fun createBinding(): BINDING

    /**
     * Called only when a new binding is created.<br><br>
     * 새 바인딩이 생성될 때만 호출됩니다.<br>
     *
     * @param binding The initialized ViewBinding instance.<br><br>
     *                초기화된 ViewBinding 인스턴스입니다.<br>
     */
    fun onInitBind(binding: BINDING)

    /**
     * Called to start event collection for UI updates.<br><br>
     * UI 갱신을 위한 이벤트 수집을 시작할 때 호출됩니다.<br>
     *
     * @param binding The initialized ViewBinding instance.<br><br>
     *                초기화된 ViewBinding 인스턴스입니다.<br>
     */
    fun onEventVmCollect(binding: BINDING)
}
