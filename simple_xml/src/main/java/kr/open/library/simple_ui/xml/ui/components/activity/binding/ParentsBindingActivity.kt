package kr.open.library.simple_ui.xml.ui.components.activity.binding

import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.base.helper.ParentBindingHelperForActivity
import kr.open.library.simple_ui.xml.ui.base.lifecycle.ParentBindingInterfaceForActivity
import kr.open.library.simple_ui.xml.ui.components.activity.root.RootActivity

/**
 * Base Activity with ViewBinding support and single-shot ViewModel event collection.<br>
 * Provides a consistent binding lifecycle and helper ViewModel accessors.<br><br>
 * ViewBinding 지원과 단일 ViewModel 이벤트 수집을 제공하는 기본 Activity입니다.<br>
 * 일관된 바인딩 생명주기와 ViewModel 접근 헬퍼를 제공합니다.<br>
 *
 * **Key points:**<br>
 * - Call super.onCreate() first.<br>
 * - onEventVmCollect() is invoked once after binding initialization.<br><br>
 * **핵심 포인트:**<br>
 * - 반드시 super.onCreate()를 먼저 호출하세요.<br>
 * - onEventVmCollect()는 바인딩 초기화 이후 1회 호출됩니다.<br>
 *
 * @param BINDING The type of ViewBinding to be used.<br><br>
 *                사용할 ViewBinding 타입.<br>
 */
abstract class ParentsBindingActivity<BINDING : ViewBinding> :
    RootActivity(),
    ParentBindingInterfaceForActivity<BINDING> {
    /**
     * Holds the ViewBinding instance for this Activity.<br><br>
     * 이 Activity의 ViewBinding 인스턴스를 보관합니다.<br>
     */
    private lateinit var binding: BINDING

    /**
     * Returns the initialized binding instance.<br>
     * Call only after super.onCreate() completes.<br><br>
     * 초기화된 바인딩 인스턴스를 반환합니다.<br>
     * super.onCreate() 완료 이후에만 호출하세요.<br>
     *
     * @return The initialized ViewBinding instance.<br><br>
     *         초기화된 ViewBinding 인스턴스.<br>
     *
     * @throws IllegalStateException If binding is accessed before initialization.<br><br>
     *                               바인딩 초기화 전에 접근하면 예외가 발생합니다.<br>
     */
    protected fun getBinding(): BINDING {
        check(::binding.isInitialized) {
            "Binding is not initialized. Please call super.onCreate() first."
        }
        return binding
    }

    /**
     * Helper to ensure one-time event collection for this Activity instance.<br><br>
     * 이 Activity 인스턴스에서 이벤트 수집을 1회만 보장하는 헬퍼입니다.<br>
     */
    private val helper = ParentBindingHelperForActivity()

    /**
     * Creates the ViewBinding instance.<br><br>
     * ViewBinding 인스턴스를 생성합니다.<br>
     *
     * @return The initialized ViewBinding instance.<br><br>
     *         초기화된 ViewBinding 인스턴스.<br>
     */
    protected abstract fun createBinding(): BINDING

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super<RootActivity>.onCreate(savedInstanceState)
        createInitData(savedInstanceState)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super<RootActivity>.onCreate(savedInstanceState, persistentState)
        createInitData(savedInstanceState)
    }

    private fun createInitData(savedInstanceState: Bundle?) {
        binding = createBinding()
        onCreate(binding, savedInstanceState)
        helper.startEventVmCollect { onEventVmCollect(binding) }
    }

    /**
     * Collect ViewModel events here.<br>
     * Use repeatOnLifecycle(Lifecycle.State.STARTED) to avoid duplicate collectors.<br><br>
     * ViewModel 이벤트를 수집하는 훅입니다.<br>
     * 중복 수집 방지를 위해 repeatOnLifecycle(Lifecycle.State.STARTED)를 사용하세요.<br>
     *
     * @param binding The initialized ViewBinding instance.<br><br>
     *                초기화된 ViewBinding 인스턴스.<br>
     */
    override fun onEventVmCollect(binding: BINDING) {}

    /**
     * Obtains a ViewModel of the specified type using ViewModelProvider.<br><br>
     * ViewModelProvider로 지정한 타입의 ViewModel을 가져옵니다.<br>
     *
     * @param T The type of the ViewModel to obtain.<br><br>
     *          가져올 ViewModel 타입.<br>
     * @return The ViewModel instance of type T.<br><br>
     *         T 타입의 ViewModel 인스턴스.<br>
     */
    protected inline fun <reified T : ViewModel> getViewModel(): T = ViewModelProvider(this)[T::class.java]

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
    protected inline fun <reified T : ViewModel> getViewModel(factory: ViewModelProvider.Factory): T =
        ViewModelProvider(this, factory)[T::class.java]
}
