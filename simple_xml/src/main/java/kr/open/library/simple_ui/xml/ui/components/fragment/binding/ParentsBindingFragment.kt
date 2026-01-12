package kr.open.library.simple_ui.xml.ui.components.fragment.binding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.components.base.ParentBindingInterface
import kr.open.library.simple_ui.xml.ui.components.base.helper.ParentBindingFragmentHelper
import kr.open.library.simple_ui.xml.ui.components.fragment.root.RootFragment

/**
 * Abstract parent class for Fragment that supports ViewBinding and ViewModel event collection.<br>
 * Implements the ParentBindingInterface to provide a consistent binding lifecycle.<br><br>
 * ViewBinding 및 ViewModel 이벤트 수집을 지원하는 Fragment의 추상 부모 클래스입니다.<br>
 * ParentBindingInterface를 구현하여 일관된 바인딩 생명주기를 제공합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Centralizes common binding initialization logic for both ViewBinding and DataBinding fragments.<br>
 * - Provides a single-invocation event collection hook to prevent duplicate collectors during configuration changes.<br>
 * - Offers convenient ViewModel retrieval methods without repetitive ViewModelProvider boilerplate.<br>
 * - Handles proper binding cleanup in onDestroyView to prevent memory leaks.<br><br>
 * - ViewBinding과 DataBinding Fragment 모두에 대한 공통 바인딩 초기화 로직을 중앙화합니다.<br>
 * - 구성 변경 시 중복 수집을 방지하는 단일 호출 이벤트 수집 훅을 제공합니다.<br>
 * - 반복적인 ViewModelProvider 보일러플레이트 없이 편리한 ViewModel 검색 메서드를 제공합니다.<br>
 * - 메모리 누수를 방지하기 위해 onDestroyView에서 적절한 바인딩 정리를 처리합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses ParentBindingFragmentHelper to ensure onEventVmCollect() is called only once, preventing duplicate Flow collectors.<br>
 * - Extends RootFragment to inherit permission management features.<br>
 * - Uses nullable binding field with protected getBinding() method that throws exception after onDestroyView() for safe access.<br>
 * - Offers both default and factory-based ViewModel retrieval methods for flexibility.<br>
 * - Automatically resets helper in onDestroyView() to prevent memory leaks and stale collectors.<br><br>
 * - ParentBindingFragmentHelper를 사용하여 onEventVmCollect()가 1회만 호출되도록 하여 중복 Flow 수집을 방지합니다.<br>
 * - RootFragment를 상속하여 권한 관리 기능을 상속받습니다.<br>
 * - onDestroyView() 이후 안전한 접근을 위해 nullable binding 필드와 예외를 던지는 protected getBinding() 메서드를 사용합니다.<br>
 * - 유연성을 위해 기본 및 팩토리 기반 ViewModel 검색 메서드를 모두 제공합니다.<br>
 * - 메모리 누수 및 오래된 수집기를 방지하기 위해 onDestroyView()에서 자동으로 helper를 재설정합니다.<br>
 *
 * **Important notes / 주의사항:**<br>
 * - ⚠️ CRITICAL: Always call super.onCreateView() and super.onViewCreated() when overriding. Skipping will cause binding initialization to fail.<br>
 * - onEventVmCollect() is called only once in onViewCreated() after binding initialization.<br>
 * - Always use viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) inside onEventVmCollect() to properly handle configuration changes.<br>
 * - Access binding via getBinding() method only between onViewCreated() and onDestroyView() - accessing after onDestroyView() throws IllegalStateException.<br>
 * - The binding is automatically set to null in onDestroyView() to prevent memory leaks.<br><br>
 * - ⚠️ 중요: 오버라이드할 때 반드시 super.onCreateView()와 super.onViewCreated()를 호출하세요. 누락하면 바인딩 초기화가 실패합니다.<br>
 * - onEventVmCollect()는 onViewCreated()에서 바인딩 초기화 후 1회만 호출됩니다.<br>
 * - 구성 변경을 올바르게 처리하려면 onEventVmCollect() 내부에서 항상 viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)를 사용하세요.<br>
 * - getBinding() 메서드를 통한 바인딩 접근은 onViewCreated()와 onDestroyView() 사이에서만 가능 - onDestroyView() 이후 접근 시 IllegalStateException 발생.<br>
 * - 메모리 누수를 방지하기 위해 onDestroyView()에서 바인딩이 자동으로 null로 설정됩니다.<br>
 *
 * @param BINDING The type of ViewBinding to be used.<br><br>
 *                사용될 ViewBinding의 타입.<br>
 * @param isAttachToParent Whether to attach the inflated view to the parent container.<br><br>
 *                         인플레이션된 뷰를 부모 컨테이너에 첨부할지 여부.<br>
 */
abstract class ParentsBindingFragment<BINDING : ViewBinding>(
    private val isAttachToParent: Boolean = false
) : RootFragment(),
    ParentBindingInterface<BINDING> {
    private var binding: BINDING? = null

    /**
     * Returns the ViewBinding object for the Fragment.<br><br>
     * Fragment의 ViewBinding 객체를 반환합니다.<br>
     *
     * @return The ViewBinding object for the Fragment.<br><br>
     *         Fragment의 ViewBinding 객체.<br>
     * @throws IllegalStateException if accessed after onDestroyView().<br><br>
     *                               onDestroyView() 이후에 접근하는 경우.<br>
     */
    protected fun getBinding(): BINDING {
        check(binding != null) {
            "Binding accessed after onDestroyView()"
        }
        return binding!!
    }

    private val helper = ParentBindingFragmentHelper()

    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?, isAttachToParent: Boolean): BINDING

    @CallSuper
    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = createBinding(inflater, container, isAttachToParent)
        onCreateView(getBinding(), savedInstanceState)
        return getBinding().root
    }

    @CallSuper
    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreate(getBinding(), savedInstanceState)
        helper.startEventVmCollect { onEventVmCollect() }
    }

    /**
     * **Important notes / 주의사항:**<br>
     * - Always call super.onDestroyView(). Skipping it prevents binding cleanup and event reset, leading to leaks or duplicate collectors.<br>
     * - This method must run even when you override onDestroyView().<br><br>
     * - 반드시 super.onDestroyView()를 호출하세요. 누락 시 binding 정리와 이벤트 리셋이 수행되지 않아 메모리 누수/중복 수집이 발생할 수 있습니다.<br>
     * - onDestroyView()를 오버라이드하더라도 이 메서드는 반드시 실행되어야 합니다.<br>
     */
    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        helper.reset()
        binding = null
    }

    override fun onEventVmCollect() {}

    protected inline fun <reified T : ViewModel> getViewModel(): T = ViewModelProvider(this)[T::class.java]

    protected inline fun <reified T : ViewModel> getViewModel(factory: ViewModelProvider.Factory): T =
        ViewModelProvider(this, factory)[T::class.java]
}
