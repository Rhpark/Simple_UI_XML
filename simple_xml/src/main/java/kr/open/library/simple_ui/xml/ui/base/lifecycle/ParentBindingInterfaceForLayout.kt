package kr.open.library.simple_ui.xml.ui.base.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.viewbinding.ViewBinding

/**
 * Interface for layouts that support ViewBinding or DataBinding.
 * Defines the contract for binding initialization and cleanup.
 *
 * ViewBinding 또는 DataBinding을 지원하는 레이아웃을 위한 인터페이스입니다.
 * 바인딩 초기화 및 정리(cleanup)를 위한 계약을 정의합니다.
 *
 * @param BINDING The type of ViewBinding. (ViewBinding 타입)
 */
interface ParentBindingInterfaceForLayout<BINDING : ViewBinding> :
    ParentBindingInterface<BINDING>,
    DefaultLifecycleObserver {
    /**
     * Called when the binding is initialized in onCreate().<br>
     * Implement setup logic that requires binding here.<br><br>
     * onCreate에서 바인딩이 초기화된 후 호출됩니다.<br>
     * 바인딩이 필요한 초기화 로직을 여기서 수행하세요.<br>
     *
     * @param binding The initialized ViewBinding instance.<br><br>
     *                초기화된 ViewBinding 인스턴스.<br>
     * @param savedInstanceState Saved instance state, if available.<br><br>
     *                           저장된 상태가 있다면 해당 Bundle.<br>
     */
    fun onInitBind(binding: BINDING) {}

}
