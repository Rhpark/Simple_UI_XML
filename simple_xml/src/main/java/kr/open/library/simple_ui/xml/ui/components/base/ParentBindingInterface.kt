package kr.open.library.simple_ui.xml.ui.components.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding

/**
 * Interface defining the contract for binding lifecycle within UI components.<br>
 * Ensures consistent behavior for binding initialization and ViewModel event collection across Activities and Fragments.<br><br>
 *
 * UI 컴포넌트 내의 바인딩 생명주기에 대한 계약을 정의하는 인터페이스입니다.<br>
 * Activity 및 Fragment 전반에서 바인딩 초기화 및 ViewModel 이벤트 수집에 대한 일관된 동작을 보장합니다.<br>
 *
 * @param BINDING The type of ViewBinding class.<br><br>
 *                ViewBinding 클래스의 타입.<br>
 */
interface ParentBindingInterface<BINDING : ViewBinding> {
    /**
     * Called when the binding is initialized.<br>
     * Implement this to perform setup logic that depends on the binding.<br><br>
     *
     * 바인딩이 초기화될 때 호출됩니다.<br>
     * 바인딩에 의존하는 설정 로직을 수행하려면 이를 구현하세요.<br>
     *
     * @param binding The initialized ViewBinding instance.<br><br>
     *                초기화된 ViewBinding 인스턴스.<br>
     */
    fun onViewCreate(binding: BINDING, savedInstanceState: Bundle?) {}

    /**
     * Called immediately after binding creation in onCreateView().<br><br>
     * onCreateView()에서 바인딩 생성 직후 호출됩니다.<br>
     *
     * @param binding The initialized ViewBinding instance.<br><br>
     *                초기화된 ViewBinding 인스턴스.<br>
     */
    fun onCreateView(binding: BINDING, savedInstanceState: Bundle?) {}

    /**
     * Called to start collecting ViewModel events.<br>
     * This is typically invoked once per lifecycle (e.g., onCreate or onViewCreated).<br><br>
     *
     * ViewModel 이벤트 수집을 시작하기 위해 호출됩니다.<br>
     * 이는 일반적으로 생명주기당 한 번(예: onCreate 또는 onViewCreated) 호출됩니다.<br>
     */
    fun onEventVmCollect(binding: BINDING) {}
}
