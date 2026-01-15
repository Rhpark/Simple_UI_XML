package kr.open.library.simple_ui.xml.ui.base.lifecycle

import android.os.Bundle
import androidx.viewbinding.ViewBinding

/**
 * Interface for Fragment binding lifecycle hooks.<br>
 * Extends ParentBindingInterface to provide binding callbacks for Fragments.<br><br>
 * Fragment 바인딩 생명주기 훅을 정의하는 인터페이스입니다.<br>
 * ParentBindingInterface를 확장하여 Fragment 전용 바인딩 콜백을 제공합니다.<br>
 *
 * @param BINDING The type of ViewBinding class.<br><br>
 *                ViewBinding 클래스 타입.<br>
 */
interface ParentBindingInterfaceForFragment<BINDING : ViewBinding> : ParentBindingInterface<BINDING> {
    /**
     * Called when the binding is initialized in onViewCreated().<br>
     * Implement setup logic that requires binding here.<br><br>
     * onViewCreated에서 바인딩이 초기화된 후 호출됩니다.<br>
     * 바인딩이 필요한 초기화 로직을 여기서 수행하세요.<br>
     *
     * @param binding The initialized ViewBinding instance.<br><br>
     *                초기화된 ViewBinding 인스턴스.<br>
     * @param savedInstanceState Saved instance state, if available.<br><br>
     *                           저장된 상태가 있다면 해당 Bundle.<br>
     */
    fun onViewCreated(binding: BINDING, savedInstanceState: Bundle?) {}

    /**
     * Called immediately after binding creation in onCreateView().<br>
     * Use this hook for early initialization before onViewCreated().<br><br>
     * onCreateView에서 바인딩 생성 직후 호출됩니다.<br>
     * onViewCreated 이전의 초기화 로직에 사용하세요.<br>
     *
     * @param binding The initialized ViewBinding instance.<br><br>
     *                초기화된 ViewBinding 인스턴스.<br>
     * @param savedInstanceState Saved instance state, if available.<br><br>
     *                           저장된 상태가 있다면 해당 Bundle.<br>
     */
    fun onCreateView(binding: BINDING, savedInstanceState: Bundle?) {}
}
