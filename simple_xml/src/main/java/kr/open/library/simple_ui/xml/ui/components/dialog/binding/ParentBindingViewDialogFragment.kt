package kr.open.library.simple_ui.xml.ui.components.dialog.binding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.base.helper.ParentBindingHelperForFragment
import kr.open.library.simple_ui.xml.ui.base.lifecycle.ParentBindingInterfaceForFragment
import kr.open.library.simple_ui.xml.ui.components.dialog.root.RootDialogFragment

/**
 * Base DialogFragment with ViewBinding support and single-shot ViewModel event collection.<br>
 * Manages binding lifecycle safely and resets onDestroyView().<br><br>
 * ViewBinding 지원과 단일 ViewModel 이벤트 수집을 제공하는 기본 DialogFragment입니다.<br>
 * 바인딩 생명주기를 안전하게 관리하고 onDestroyView()에서 초기화합니다.<br>
 *
 * **Key points:**<br>
 * - Call super.onViewCreated().<br>
 * - onEventVmCollect() is invoked once after binding initialization.<br><br>
 * **핵심 포인트:**<br>
 * - super.onViewCreated()를 반드시 호출하세요.<br>
 * - onEventVmCollect()는 바인딩 초기화 이후 1회 호출됩니다.<br>
 *
 * @param BINDING The type of ViewBinding to be used.<br><br>
 *                사용할 ViewBinding 타입.<br>
 */
abstract class ParentBindingViewDialogFragment<BINDING : ViewBinding>(
    private val isAttachToParent: Boolean,
) : RootDialogFragment(),
    ParentBindingInterfaceForFragment<BINDING> {
    /**
     * Holds the ViewBinding instance for this DialogFragment.<br><br>
     * 이 DialogFragment의 ViewBinding 인스턴스를 보관합니다.<br>
     */
    private var binding: BINDING? = null

    /**
     * Returns the initialized binding instance.<br>
     * Accessible only between onViewCreated() and onDestroyView().<br><br>
     * 초기화된 바인딩 인스턴스를 반환합니다.<br>
     * onViewCreated()와 onDestroyView() 사이에서만 접근하세요.<br>
     *
     * @return The initialized ViewBinding instance.<br><br>
     *         초기화된 ViewBinding 인스턴스.<br>
     *
     * @throws IllegalStateException If accessed after onDestroyView().<br><br>
     *                               onDestroyView() 이후 접근하면 예외가 발생합니다.<br>
     */
    protected fun getBinding(): BINDING {
        check(binding != null) {
            "Binding accessed after onDestroyView()"
        }
        return binding!!
    }

    /**
     * Helper to ensure one-time event collection for this DialogFragment instance.<br><br>
     * 이 DialogFragment 인스턴스에서 이벤트 수집을 1회만 보장하는 헬퍼입니다.<br>
     */
    private val helper = ParentBindingHelperForFragment()

    /**
     * Creates the ViewBinding instance.<br><br>
     * ViewBinding 인스턴스를 생성합니다.<br>
     *
     * @param inflater The LayoutInflater to inflate the binding.<br><br>
     *                 바인딩을 inflate할 LayoutInflater.<br>
     * @param container The parent container, if any.<br><br>
     *                  부모 컨테이너(있다면).<br>
     * @param isAttachToParent Whether to attach to the parent.<br><br>
     *                         부모에 attach할지 여부.<br>
     * @return The initialized ViewBinding instance.<br><br>
     *         초기화된 ViewBinding 인스턴스.<br>
     */
    protected abstract fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        isAttachToParent: Boolean,
    ): BINDING

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = createBinding(inflater, container, isAttachToParent)
        onCreateView(getBinding(), savedInstanceState)
        return getBinding().root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super<RootDialogFragment>.onViewCreated(view, savedInstanceState)
        onViewCreated(getBinding(), savedInstanceState)
        config.updateBackgroundColor(getBinding().root)
        helper.startEventVmCollect { onEventVmCollect(getBinding()) }
    }

    /**
     * Obtains a ViewModel of the specified type using ViewModelProvider.<br><br>
     * ViewModelProvider로 지정한 타입의 ViewModel을 가져옵니다.<br>
     *
     * @param T The type of the ViewModel to obtain.<br><br>
     *          가져올 ViewModel 타입.<br>
     * @return The ViewModel instance of type T.<br><br>
     *         T 타입의 ViewModel 인스턴스.<br>
     */
    protected inline fun <reified T : ViewModel> DialogFragment.getViewModel(): T =
        ViewModelProvider(this)[T::class.java]

    /**
     * Obtains a ViewModel of the specified type using ViewModelProvider with a custom factory.<br><br>
     * 커스텀 Factory로 지정한 타입의 ViewModel을 가져옵니다.<br>
     *
     * @param T The type of the ViewModel to obtain.<br><br>
     *          가져올 ViewModel 타입.<br>
     * @param factory The Factory to use for creating the ViewModel instance.<br><br>
     *                ViewModel 생성에 사용할 Factory.<br>
     * @return The ViewModel instance of type T.<br><br>
     *         T 타입의 ViewModel 인스턴스.<br>
     */
    protected inline fun <reified T : ViewModel> DialogFragment.getViewModel(factory: ViewModelProvider.Factory): T =
        ViewModelProvider(this, factory)[T::class.java]

    /**
     * Cleans up binding and resets helper state.<br>
     * Always call super.onDestroyView().<br><br>
     * 바인딩을 정리하고 헬퍼 상태를 초기화합니다.<br>
     * 반드시 super.onDestroyView()를 호출하세요.<br>
     */
    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        helper.reset()
        binding = null
    }
}
