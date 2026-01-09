package kr.open.library.simple_ui.xml.ui.fragment.binding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.base.ParentBindingInterface
import kr.open.library.simple_ui.xml.ui.base.helper.ParentBindingFragmentHelper
import kr.open.library.simple_ui.xml.ui.fragment.root.RootFragment

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
 * - Uses nullable _binding with public binding accessor that throws exception after onDestroyView() for safe access.<br>
 * - Offers both default and factory-based ViewModel retrieval methods for flexibility.<br>
 * - Automatically resets helper in onDestroyView() to prevent memory leaks and stale collectors.<br><br>
 * - ParentBindingFragmentHelper를 사용하여 onEventVmCollect()가 1회만 호출되도록 하여 중복 Flow 수집을 방지합니다.<br>
 * - RootFragment를 상속하여 권한 관리 기능을 상속받습니다.<br>
 * - onDestroyView() 이후 안전한 접근을 위해 nullable _binding과 예외를 던지는 public binding 접근자를 사용합니다.<br>
 * - 유연성을 위해 기본 및 팩토리 기반 ViewModel 검색 메서드를 모두 제공합니다.<br>
 * - 메모리 누수 및 오래된 수집기를 방지하기 위해 onDestroyView()에서 자동으로 helper를 재설정합니다.<br>
 *
 * **Important notes / 주의사항:**<br>
 * - ⚠️ CRITICAL: Always call super.onCreateView() and super.onViewCreated() when overriding. Skipping will cause binding initialization to fail.<br>
 * - onEventVmCollect() is called only once in onViewCreated() after binding initialization.<br>
 * - Always use viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) inside onEventVmCollect() to properly handle configuration changes.<br>
 * - Access binding property only between onViewCreated() and onDestroyView() - accessing after onDestroyView() throws IllegalStateException.<br>
 * - The binding is automatically set to null in onDestroyView() to prevent memory leaks.<br><br>
 * - ⚠️ 중요: 오버라이드할 때 반드시 super.onCreateView()와 super.onViewCreated()를 호출하세요. 누락하면 바인딩 초기화가 실패합니다.<br>
 * - onEventVmCollect()는 onViewCreated()에서 바인딩 초기화 후 1회만 호출됩니다.<br>
 * - 구성 변경을 올바르게 처리하려면 onEventVmCollect() 내부에서 항상 viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)를 사용하세요.<br>
 * - binding 프로퍼티는 onViewCreated()와 onDestroyView() 사이에서만 접근 - onDestroyView() 이후 접근 시 IllegalStateException 발생.<br>
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
    /**
     * Internal backing field for binding.<br><br>
     * binding의 내부 백킹 필드입니다.<br>
     */
    private var _binding: BINDING? = null

    /**
     * The ViewBinding object for the fragment.<br>
     * Throws IllegalStateException if accessed after onDestroyView().<br><br>
     * Fragment의 ViewBinding 객체입니다.<br>
     * onDestroyView() 이후에 접근하면 IllegalStateException이 발생합니다.<br>
     *
     * **Usage / 사용법:**<br>
     * Access views in lifecycle methods between onViewCreated() and onDestroyView().<br><br>
     * onViewCreated()와 onDestroyView() 사이의 생명주기 메서드에서 뷰에 접근할 때 사용합니다.<br>
     *
     * **Example / 예시:**<br>
     * ```kotlin
     * override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
     *     super.onViewCreated(view, savedInstanceState)
     *     binding.textView.text = "Hello"
     * }
     * ```
     *
     * @return The ViewBinding object for the fragment.<br><br>
     *         Fragment의 ViewBinding 객체.<br>
     * @throws IllegalStateException if accessed after onDestroyView().<br><br>
     *                               onDestroyView() 이후에 접근하는 경우.<br>
     */
    public val binding: BINDING
        get() = _binding
            ?: throw IllegalStateException("Binding accessed after onDestroyView()")

    /**
     * Helper that ensures onEventVmCollect() is invoked only once to prevent duplicate Flow collectors.<br><br>
     * onEventVmCollect()가 1회만 호출되도록 보장하여 중복 Flow 수집을 방지하는 헬퍼입니다.<br>
     */
    private val helper = ParentBindingFragmentHelper()

    /**
     * Creates the ViewBinding instance for this Fragment.<br>
     * This method must be implemented by subclasses to provide the specific binding instance.<br><br>
     * 이 Fragment에 대한 ViewBinding 인스턴스를 생성합니다.<br>
     * 하위 클래스는 이 메서드를 구현하여 특정 바인딩 인스턴스를 제공해야 합니다.<br>
     *
     * @param inflater The LayoutInflater object that can be used to inflate views.<br><br>
     *                 뷰를 인플레이션하는 데 사용할 수 있는 LayoutInflater 객체.<br>
     * @param container The parent view that the fragment's UI should be attached to.<br><br>
     *                  Fragment의 UI가 첨부될 부모 뷰.<br>
     * @param isAttachToParent Whether to attach the inflated view to the parent container.<br><br>
     *                         인플레이션된 뷰를 부모 컨테이너에 첨부할지 여부.<br>
     * @return The initialized ViewBinding instance.<br><br>
     *         초기화된 ViewBinding 인스턴스.<br>
     */
    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?, isAttachToParent: Boolean): BINDING

    /**
     * Called to have the fragment instantiate its user interface view.<br>
     * Inflates the layout using ViewBinding and returns the root view.<br><br>
     * Fragment가 사용자 인터페이스 뷰를 인스턴스화하기 위해 호출됩니다.<br>
     * ViewBinding을 사용하여 레이아웃을 인플레이션하고 루트 뷰를 반환합니다.<br>
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.<br><br>
     *                 Fragment의 뷰를 인플레이션하는 데 사용할 수 있는 LayoutInflater 객체.<br>
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.<br><br>
     *                  null이 아닌 경우, Fragment의 UI가 첨부될 부모 뷰.<br>
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.<br><br>
     *                           null이 아닌 경우, 이 Fragment는 이전에 저장된 상태에서 다시 구성되고 있습니다.<br>
     * @return Return the View for the fragment's UI.<br><br>
     *         Fragment UI의 View를 반환.<br>
     */
    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = createBinding(inflater, container, isAttachToParent)
        return binding.root
    }

    /**
     * Called immediately after onCreateView() has returned.<br>
     * Initializes binding and starts ViewModel event collection.<br><br>
     * onCreateView()가 반환된 직후 호출됩니다.<br>
     * 바인딩을 초기화하고 ViewModel 이벤트 수집을 시작합니다.<br>
     *
     * @param view The View returned by onCreateView().<br><br>
     *             onCreateView()가 반환한 View.<br>
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.<br><br>
     *                           null이 아닌 경우, 이 Fragment는 이전에 저장된 상태에서 다시 구성되고 있습니다.<br>
     */
    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onInitBind(binding)
        // Starts ViewModel event collection only once after binding initialization, preventing duplicate collectors.<br><br>
        // 바인딩 초기화 후 ViewModel 이벤트 수집을 1회만 시작하여 중복 수집을 방지합니다.<br>
        helper.startEventVmCollect { onEventVmCollect() }
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.<br>
     * Cleans up the binding reference and resets helper to prevent memory leaks.<br><br>
     * onCreateView에서 생성된 뷰가 Fragment에서 분리될 때 호출됩니다.<br>
     * 메모리 누수를 방지하기 위해 바인딩 참조를 정리하고 helper를 재설정합니다.<br>
     */
    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        helper.reset()
        _binding = null
    }

    /**
     * Called after the binding has been initialized in onViewCreated().<br>
     * Override this method to perform additional initialization with the binding.<br><br>
     * onViewCreated()에서 바인딩이 초기화된 후 호출됩니다.<br>
     * 바인딩을 사용한 추가 초기화를 수행하려면 이 메서드를 오버라이드하세요.<br>
     *
     * @param binding The initialized ViewBinding instance.<br><br>
     *                초기화된 ViewBinding 인스턴스.<br>
     */
    override fun onInitBind(binding: BINDING) {}

    /**
     * Override this method to collect ViewModel events.<br>
     * This hook is invoked once via `startEventVmCollect()` after binding initialization in `onViewCreated()`.<br>
     *
     * **Important**: To avoid duplicate collectors during configuration changes (e.g., screen rotation),
     * use `viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)` to automatically cancel and restart collection.<br><br>
     *
     * ViewModel 이벤트를 수집하려면 이 메서드를 오버라이드하세요.<br>
     * 이 훅은 `onViewCreated()`에서 바인딩 초기화 후 `startEventVmCollect()`를 통해 1회 호출됩니다.<br>
     *
     * **중요**: 구성 변경(예: 화면 회전) 시 중복 수집을 방지하려면
     * `viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)`를 사용하여 자동으로 수집을 취소하고 재시작하세요.<br>
     *
     * **Best Practice Example:**<br>
     * ```kotlin
     * override fun onEventVmCollect() {
     *     viewLifecycleOwner.lifecycleScope.launch {
     *         viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {  // ✅ Recommended
     *             viewModel.events.collect { event ->
     *                 when (event) {
     *                     is MyEvent.ShowToast -> showToast(event.message)
     *                 }
     *             }
     *         }
     *     }
     * }
     * ```
     *
     * **Anti-Pattern (avoid this):**<br>
     * ```kotlin
     * override fun onEventVmCollect() {
     *     viewLifecycleOwner.lifecycleScope.launch {
     *         viewModel.events.collect { event -> ... }  // ❌ May cause duplicate collectors
     *     }
     * }
     * ```
     */
    override fun onEventVmCollect() {}

    /**
     * Obtains a ViewModel of the specified type using ViewModelProvider.<br><br>
     * ViewModelProvider를 사용하여 지정된 타입의 ViewModel을 가져옵니다.<br>
     *
     * @param T The type of the ViewModel to obtain.<br><br>
     *          가져올 ViewModel의 타입.
     * @return The ViewModel instance of type T.<br><br>
     *         타입 T의 ViewModel 인스턴스.<br>
     */
    protected inline fun <reified T : ViewModel> Fragment.getViewModel(): T = ViewModelProvider(this)[T::class.java]

    /**
     * Obtains a ViewModel of the specified type using ViewModelProvider with a custom factory.<br><br>
     * 커스텀 팩토리를 사용하여 ViewModelProvider로 지정된 타입의 ViewModel을 가져옵니다.<br>
     *
     * @param T The type of the ViewModel to obtain.<br><br>
     *          가져올 ViewModel의 타입.
     * @param factory The Factory to use for creating the ViewModel instance.<br><br>
     *                ViewModel 인스턴스 생성에 사용할 Factory.
     * @return The ViewModel instance of type T.<br><br>
     *         타입 T의 ViewModel 인스턴스.<br>
     */
    protected inline fun <reified T : ViewModel> Fragment.getViewModel(factory: ViewModelProvider.Factory): T =
        ViewModelProvider(this, factory)[T::class.java]
}
