package kr.open.library.simple_ui.xml.ui.components.base.lifecycle

import androidx.viewbinding.ViewBinding

/**
 * Interface defining the binding lifecycle contract for UI components.<br>
 * Ensures consistent binding initialization and ViewModel event collection across Activities and Fragments.<br><br>
 * UI 컴포넌트의 바인딩 생명주기 계약을 정의하는 인터페이스입니다.<br>
 * Activity/Fragment 전반에서 바인딩 초기화와 ViewModel 이벤트 수집을 일관되게 제공합니다.<br>
 *
 * @param BINDING The type of ViewBinding class.<br><br>
 *                ViewBinding 클래스 타입.<br>
 */
abstract interface ParentBindingInterface<BINDING : ViewBinding> {
    /**
     * Called to start collecting ViewModel events.<br>
     * Typically invoked once per lifecycle (e.g., onCreate or onViewCreated).<br><br>
     * ViewModel 이벤트 수집을 시작할 때 호출됩니다.<br>
     * 보통 생명주기 당 1회(onCreate 또는 onViewCreated) 호출됩니다.<br>
     *
     * @param binding The initialized binding instance.<br><br>
     *                초기화된 바인딩 인스턴스.<br>
     */
    fun onEventVmCollect(binding: BINDING) {}
}
