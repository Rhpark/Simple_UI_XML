package kr.open.library.simple_ui.xml.ui.dialog.binding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.base.ParentBindingInterface
import kr.open.library.simple_ui.xml.ui.base.helper.ParentBindingFragmentHelper
import kr.open.library.simple_ui.xml.ui.dialog.root.RootDialogFragment

/**
 * Abstract parent class for DialogFragment that supports ViewBinding and ViewModel event collection.<br>
 * Implements the ParentBindingInterface to provide a consistent binding lifecycle.<br><br>
 * ViewBinding 및 ViewModel 이벤트 수집을 지원하는 DialogFragment의 추상 부모 클래스입니다.<br>
 * ParentBindingInterface를 구현하여 일관된 바인딩 생명주기를 제공합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Centralizes common binding initialization logic for both ViewBinding and DataBinding dialog fragments.<br>
 * - Provides a single-invocation event collection hook to prevent duplicate collectors during configuration changes.<br>
 * - Offers convenient ViewModel retrieval methods without repetitive ViewModelProvider boilerplate.<br><br>
 * - ViewBinding과 DataBinding DialogFragment 모두에 대한 공통 바인딩 초기화 로직을 중앙화합니다.<br>
 * - 구성 변경 시 중복 수집을 방지하는 단일 호출 이벤트 수집 훅을 제공합니다.<br>
 * - 반복적인 ViewModelProvider 보일러플레이트 없이 편리한 ViewModel 검색 메서드를 제공합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses ParentBindingFragmentHelper to ensure onEventVmCollect() is called only once, preventing duplicate Flow collectors.<br>
 * - Extends RootDialogFragment to inherit dialog functionality and permission management.<br>
 * - Uses private lateinit binding with protected getBinding() accessor to maintain encapsulation while enabling subclass access.<br>
 * - Offers both default and factory-based ViewModel retrieval methods for flexibility.<br><br>
 * - ParentBindingFragmentHelper를 사용하여 onEventVmCollect()가 1회만 호출되도록 하여 중복 Flow 수집을 방지합니다.<br>
 * - RootDialogFragment를 상속하여 다이얼로그 기능과 권한 관리를 상속받습니다.<br>
 * - 캡슐화를 유지하면서 하위 클래스 접근을 가능하게 하기 위해 private lateinit binding과 protected getBinding() 접근자를 사용합니다.<br>
 * - 유연성을 위해 기본 및 팩토리 기반 ViewModel 검색 메서드를 모두 제공합니다.<br>
 *
 * **Important notes / 주의사항:**<br>
 * - ⚠️ CRITICAL: Always call super.onViewCreated() first when overriding onViewCreated(). Skipping it will cause binding initialization to fail and crash at runtime.<br>
 * - onEventVmCollect() is called only once in onViewCreated() after binding initialization.<br>
 * - Always use repeatOnLifecycle(Lifecycle.State.STARTED) inside onEventVmCollect() to properly handle configuration changes.<br>
 * - Access the binding object via getBinding() method after super.onViewCreated() completes.<br><br>
 * - ⚠️ 중요: onViewCreated()를 오버라이드할 때 반드시 먼저 super.onViewCreated()를 호출하세요. 누락하면 바인딩 초기화가 실패하고 런타임에 크래시가 발생합니다.<br>
 * - onEventVmCollect()는 onViewCreated()에서 바인딩 초기화 후 1회만 호출됩니다.<br>
 * - 구성 변경을 올바르게 처리하려면 onEventVmCollect() 내부에서 항상 repeatOnLifecycle(Lifecycle.State.STARTED)를 사용하세요.<br>
 * - super.onViewCreated() 완료 후 getBinding() 메서드를 통해 바인딩 객체에 접근하세요.<br>
 *
 * @param BINDING The type of ViewBinding to be used.<br><br>
 *                사용될 ViewBinding의 타입.<br>
 *
 * @see RootDialogFragment For base class with dialog and permission features.<br><br>
 *      다이얼로그 및 권한 기능이 있는 기본 클래스는 RootDialogFragment를 참조하세요.<br>
 *
 * @see BaseViewBindingDialogFragment For ViewBinding-enabled DialogFragment.<br><br>
 *      ViewBinding을 사용하는 DialogFragment는 BaseViewBindingDialogFragment를 참조하세요.<br>
 *
 * @see BaseDataBindingDialogFragment For DataBinding-enabled DialogFragment.<br><br>
 *      DataBinding을 사용하는 DialogFragment는 BaseDataBindingDialogFragment를 참조하세요.<br>
 */
abstract class ParentBindingViewDialogFragment<BINDING : ViewBinding>(
    private val isAttachToParent: Boolean
) : RootDialogFragment(),
    ParentBindingInterface<BINDING> {
    /**
     * Internal backing field for binding.<br><br>
     * binding의 내부 백킹 필드입니다.<br>
     */
    private var _binding: BINDING? = null

    /**
     * Returns the binding instance for this DialogFragment.<br>
     * Only accessible from subclasses (protected visibility).<br><br>
     * 이 DialogFragment의 바인딩 인스턴스를 반환합니다.<br>
     * 하위 클래스에서만 접근 가능합니다 (protected 가시성).<br>
     *
     * **Usage / 사용법:**<br>
     * Access views in lifecycle methods (onViewCreated, onResume, etc.) or helper methods within the DialogFragment subclass.<br><br>
     * DialogFragment 하위 클래스 내부의 생명주기 메서드(onViewCreated, onResume 등) 또는 헬퍼 메서드에서 뷰에 접근할 때 사용합니다.<br>
     *
     * **Important / 주의사항:**<br>
     * - Only available after super.onViewCreated() completes - accessing before will throw UninitializedPropertyAccessException<br>
     * - Must be called from UI thread only (View access restriction)<br><br>
     * - super.onViewCreated() 완료 후에만 사용 가능 - 이전 접근 시 UninitializedPropertyAccessException 발생<br>
     * - UI 스레드에서만 호출 필요 (View 접근 제약)<br>
     *
     * **Example / 예시:**<br>
     * ```kotlin
     * class HomeDialogFragment : BaseViewBindingDialogFragment<DialogHomeBinding>(DialogHomeBinding::inflate) {
     *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
     *         super.onViewCreated(view, savedInstanceState)
     *         getBinding().textView.text = "Hello"
     *     }
     *
     *     private fun setupViews() {
     *         getBinding().button.setOnClickListener { /* ... */ }
     *     }
     * }
     * ```
     *
     * @return The initialized ViewBinding instance.<br><br>
     *         초기화된 ViewBinding 인스턴스.<br>
     */
    protected fun getBinding(): BINDING = _binding
        ?: throw IllegalStateException("Binding accessed after onDestroyView()")

    /**
     * Helper that ensures onEventVmCollect() is invoked only once to prevent duplicate Flow collectors.<br><br>
     * onEventVmCollect()가 1회만 호출되도록 보장하여 중복 Flow 수집을 방지하는 헬퍼입니다.<br>
     */
    private val helper = ParentBindingFragmentHelper()

    /**
     * Creates the ViewBinding instance for this DialogFragment.<br>
     * This method must be implemented by subclasses to provide the specific binding instance.<br><br>
     * 이 DialogFragment에 대한 ViewBinding 인스턴스를 생성합니다.<br>
     * 하위 클래스는 이 메서드를 구현하여 특정 바인딩 인스턴스를 제공해야 합니다.<br>
     *
     * @param inflater The LayoutInflater object that can be used to inflate views.<br><br>
     *                 뷰를 인플레이션하는 데 사용할 수 있는 LayoutInflater 객체.<br>
     * @param container The parent view that the DialogFragment's UI should be attached to.<br><br>
     *                  DialogFragment의 UI가 첨부될 부모 뷰.<br>
     * @param isAttachToParent Whether to attach the inflated view to the parent container.<br><br>
     *                         인플레이션된 뷰를 부모 컨테이너에 첨부할지 여부.<br>
     * @return The initialized ViewBinding instance.<br><br>
     *         초기화된 ViewBinding 인스턴스.<br>
     */
    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?, isAttachToParent: Boolean): BINDING

    /**
     * Called to have the DialogFragment instantiate its user interface view.<br>
     * Inflates the layout using ViewBinding and returns the root view.<br><br>
     * DialogFragment가 사용자 인터페이스 뷰를 인스턴스화하기 위해 호출됩니다.<br>
     * ViewBinding을 사용하여 레이아웃을 인플레이션하고 루트 뷰를 반환합니다.<br>
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the DialogFragment.<br><br>
     *                 DialogFragment의 뷰를 인플레이션하는 데 사용할 수 있는 LayoutInflater 객체.<br>
     * @param container If non-null, this is the parent view that the DialogFragment's UI should be attached to.<br><br>
     *                  null이 아닌 경우, DialogFragment의 UI가 첨부될 부모 뷰.<br>
     * @param savedInstanceState If non-null, this DialogFragment is being re-constructed from a previous saved state.<br><br>
     *                           null이 아닌 경우, 이 DialogFragment는 이전에 저장된 상태에서 다시 구성되고 있습니다.<br>
     * @return Return the View for the DialogFragment's UI.<br><br>
     *         DialogFragment UI의 View를 반환.<br>
     */
    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = createBinding(inflater, container, isAttachToParent)
        return getBinding().root
    }

    /**
     * Called immediately after onCreateView() has returned.<br>
     * Initializes binding and starts ViewModel event collection.<br><br>
     * onCreateView()가 반환된 직후 호출됩니다.<br>
     * 바인딩을 초기화하고 ViewModel 이벤트 수집을 시작합니다.<br>
     *
     * @param view The View returned by onCreateView().<br><br>
     *             onCreateView()가 반환한 View.<br>
     * @param savedInstanceState If non-null, this DialogFragment is being re-constructed from a previous saved state.<br><br>
     *                           null이 아닌 경우, 이 DialogFragment는 이전에 저장된 상태에서 다시 구성되고 있습니다.<br>
     */
    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onInitBind(getBinding())
        getBackgroundColor()?.let { setBackgroundColor(it) }
        getBackgroundResId()?.let { setBackgroundResource(it) }
        // Starts ViewModel event collection only once after binding initialization, preventing duplicate collectors.<br><br>
        // 바인딩 초기화 후 ViewModel 이벤트 수집을 1회만 시작하여 중복 수집을 방지합니다.<br>
        helper.startEventVmCollect { onEventVmCollect() }
    }

    override fun setBackgroundColor(color: Int) {
        _binding?.root?.let { setBackgroundColor(it, color) }
    }

    override fun setBackgroundResource(resId: Int) {
        _binding?.root?.let { setBackgroundResource(it, resId) }
    }

    /**
     * Called after the binding has been initialized.<br>
     * Override this method to perform additional initialization with the binding.<br><br>
     * 바인딩이 초기화된 후 호출됩니다.<br>
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
     * use `repeatOnLifecycle(Lifecycle.State.STARTED)` to automatically cancel and restart collection.<br><br>
     *
     * ViewModel 이벤트를 수집하려면 이 메서드를 오버라이드하세요.<br>
     * 이 훅은 `onViewCreated()`에서 바인딩 초기화 후 `startEventVmCollect()`를 통해 1회 호출됩니다.<br>
     *
     * **중요**: 구성 변경(예: 화면 회전) 시 중복 수집을 방지하려면
     * `repeatOnLifecycle(Lifecycle.State.STARTED)`를 사용하여 자동으로 수집을 취소하고 재시작하세요.<br>
     *
     * **Best Practice Example:**<br>
     * ```kotlin
     * override fun onEventVmCollect() {
     *     viewLifecycleOwner.lifecycleScope.launch {
     *         repeatOnLifecycle(Lifecycle.State.STARTED) {  // ✅ Recommended
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
     *          가져올 ViewModel의 타입.<br>
     * @return The ViewModel instance of type T.<br><br>
     *         타입 T의 ViewModel 인스턴스.<br>
     */
    protected inline fun <reified T : ViewModel> DialogFragment.getViewModel(): T = ViewModelProvider(this)[T::class.java]

    /**
     * Obtains a ViewModel of the specified type using ViewModelProvider with a custom factory.<br><br>
     * 커스텀 팩토리를 사용하여 ViewModelProvider로 지정된 타입의 ViewModel을 가져옵니다.<br>
     *
     * @param T The type of the ViewModel to obtain.<br><br>
     *          가져올 ViewModel의 타입.<br>
     * @param factory The Factory to use for creating the ViewModel instance.<br><br>
     *                ViewModel 인스턴스 생성에 사용할 Factory.<br>
     * @return The ViewModel instance of type T.<br><br>
     *         타입 T의 ViewModel 인스턴스.<br>
     */
    protected inline fun <reified T : ViewModel> DialogFragment.getViewModel(factory: ViewModelProvider.Factory): T =
        ViewModelProvider(this, factory)[T::class.java]

    /**
     * Called when the view previously created by onCreateView has been detached from the DialogFragment.<br>
     * Cleans up the binding reference and resets helper to prevent memory leaks.<br><br>
     * onCreateView에서 생성된 뷰가 DialogFragment에서 분리될 때 호출됩니다.<br>
     * 메모리 누수를 방지하기 위해 바인딩 참조를 정리하고 helper를 재설정합니다.<br>
     */
    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        helper.reset()
        _binding = null
    }
}
