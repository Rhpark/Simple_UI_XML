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
     * Called immediately after the binding object is created, inside onCreateView().<br>
     * Use only for binding variable assignment (e.g. binding.vm = vm).<br>
     * Do NOT access viewLifecycleOwner, start collectors, or call lifecycle-aware APIs here —
     * viewLifecycleOwner is not yet available at this point.<br><br>
     * onCreateView() 내부에서 바인딩 객체가 생성된 직후 호출됩니다.<br>
     * 바인딩 변수 할당(예: binding.vm = vm)에만 사용하세요.<br>
     * viewLifecycleOwner 접근, collector 시작, lifecycle-aware API 호출은 금지입니다 —
     * 이 시점에서는 viewLifecycleOwner를 아직 사용할 수 없습니다.<br>
     *
     * @param binding The initialized ViewBinding instance.<br><br>
     *                초기화된 ViewBinding 인스턴스.<br>
     * @param savedInstanceState Saved instance state, if available.<br><br>
     *                           저장된 상태가 있다면 해당 Bundle.<br>
     */
    fun onBindingCreated(binding: BINDING, savedInstanceState: Bundle?) {}
}
