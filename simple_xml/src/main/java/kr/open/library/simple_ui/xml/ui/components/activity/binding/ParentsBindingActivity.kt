package kr.open.library.simple_ui.xml.ui.components.activity.binding

import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.components.activity.root.RootActivity
import kr.open.library.simple_ui.xml.ui.components.base.ParentBindingInterface
import kr.open.library.simple_ui.xml.ui.components.base.helper.ParentBindingActivityHelper

/**
 * Abstract parent class for Activity that supports ViewBinding and ViewModel event collection.<br>
 * Implements the ParentBindingInterface to provide a consistent binding lifecycle.<br><br>
 * ViewBinding 및 ViewModel 이벤트 수집을 지원하는 Activity의 추상 부모 클래스입니다.<br>
 * ParentBindingInterface를 구현하여 일관된 바인딩 생명주기를 제공합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Centralizes common binding initialization logic for both ViewBinding and DataBinding activities.<br>
 * - Provides a single-invocation event collection hook to prevent duplicate collectors during configuration changes.<br>
 * - Offers convenient ViewModel retrieval methods without repetitive ViewModelProvider boilerplate.<br><br>
 * - ViewBinding과 DataBinding Activity 모두에 대한 공통 바인딩 초기화 로직을 중앙화합니다.<br>
 * - 구성 변경 시 중복 수집을 방지하는 단일 호출 이벤트 수집 훅을 제공합니다.<br>
 * - 반복적인 ViewModelProvider 보일러플레이트 없이 편리한 ViewModel 검색 메서드를 제공합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses ParentBindingActivityHelper to ensure onEventVmCollect() is called only once, preventing duplicate Flow collectors.<br>
 * - Extends RootActivity to inherit permission management and early initialization hooks (beforeOnCreated).<br>
 * - Uses private lateinit binding with protected getBinding() accessor to maintain encapsulation while enabling subclass access.<br>
 * - Offers both default and factory-based ViewModel retrieval methods for flexibility.<br><br>
 * - ParentBindingActivityHelper를 사용하여 onEventVmCollect()가 1회만 호출되도록 하여 중복 Flow 수집을 방지합니다.<br>
 * - RootActivity를 상속하여 권한 관리 및 조기 초기화 훅(beforeOnCreated)을 상속받습니다.<br>
 * - 캡슐화를 유지하면서 하위 클래스 접근을 가능하게 하기 위해 private lateinit binding과 protected getBinding() 접근자를 사용합니다.<br>
 * - 유연성을 위해 기본 및 팩토리 기반 ViewModel 검색 메서드를 모두 제공합니다.<br>
 *
 * **Important notes / 주의사항:**<br>
 * - ⚠️ CRITICAL: Always call super.onCreate() first when overriding onCreate(). Skipping it will cause binding initialization to fail and crash at runtime.<br>
 * - onEventVmCollect() is called only once in onCreate() after binding initialization.<br>
 * - Always use repeatOnLifecycle(Lifecycle.State.STARTED) inside onEventVmCollect() to properly handle configuration changes.<br>
 * - Access the binding object via getBinding() method after super.onCreate() completes.<br><br>
 * - ⚠️ 중요: onCreate()를 오버라이드할 때 반드시 먼저 super.onCreate()를 호출하세요. 누락하면 바인딩 초기화가 실패하고 런타임에 크래시가 발생합니다.<br>
 * - onEventVmCollect()는 onCreate()에서 바인딩 초기화 후 1회만 호출됩니다.<br>
 * - 구성 변경을 올바르게 처리하려면 onEventVmCollect() 내부에서 항상 repeatOnLifecycle(Lifecycle.State.STARTED)를 사용하세요.<br>
 * - super.onCreate() 완료 후 getBinding() 메서드를 통해 바인딩 객체에 접근하세요.<br>
 *
 * @param BINDING The type of ViewBinding to be used.<br><br>
 *                사용될 ViewBinding의 타입.<br>
 */
abstract class ParentsBindingActivity<BINDING : ViewBinding> :
    RootActivity(),
    ParentBindingInterface<BINDING> {
    /**
     * The binding object for the activity.<br>
     * Initialized in onCreate and available for use afterwards.<br><br>
     * Activity의 바인딩 객체입니다.<br>
     * onCreate에서 초기화되며 그 이후부터 사용 가능합니다.<br>
     */
    private lateinit var binding: BINDING

    /**
     * Returns the binding instance for this Activity.<br>
     * Only accessible from subclasses (protected visibility).<br><br>
     * 이 Activity의 바인딩 인스턴스를 반환합니다.<br>
     * 하위 클래스에서만 접근 가능합니다 (protected 가시성).<br>
     *
     * **Usage / 사용법:**<br>
     * Access views in lifecycle methods (onCreate, onResume, etc.) or helper methods within the Activity subclass.<br><br>
     * Activity 하위 클래스 내부의 생명주기 메서드(onCreate, onResume 등) 또는 헬퍼 메서드에서 뷰에 접근할 때 사용합니다.<br>
     *
     * **Important / 주의사항:**<br>
     * - Only available after super.onCreate() completes - accessing before will throw UninitializedPropertyAccessException<br>
     * - Must be called from UI thread only (View access restriction)<br><br>
     * - super.onCreate() 완료 후에만 사용 가능 - 이전 접근 시 UninitializedPropertyAccessException 발생<br>
     * - UI 스레드에서만 호출 필요 (View 접근 제약)<br>
     *
     * **Example / 예시:**<br>
     * ```kotlin
     * class MainActivity : BaseViewBindingActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
     *     override fun onCreate(savedInstanceState: Bundle?) {
     *         super.onCreate(savedInstanceState)
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
    protected fun getBinding(): BINDING {
        check(::binding.isInitialized) {
            "Binding is not initialized. Please call super.onCreate() first."
        }
        return binding
    }

    /**
     * Helper that ensures onEventVmCollect() is invoked only once to prevent duplicate Flow collectors.<br><br>
     * onEventVmCollect()가 1회만 호출되도록 보장하여 중복 Flow 수집을 방지하는 헬퍼입니다.<br>
     */
    private val helper = ParentBindingActivityHelper()

    /**
     * Creates the ViewBinding instance for this Activity.<br>
     * This method must be implemented by subclasses to provide the specific binding instance.<br><br>
     * 이 Activity에 대한 ViewBinding 인스턴스를 생성합니다.<br>
     * 하위 클래스는 이 메서드를 구현하여 특정 바인딩 인스턴스를 제공해야 합니다.<br>
     *
     * @return The initialized ViewBinding instance.<br><br>
     *         초기화된 ViewBinding 인스턴스.<br>
     */
    protected abstract fun createBinding(): BINDING

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createInitData(savedInstanceState)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        createInitData(savedInstanceState)
    }

    private fun createInitData(savedInstanceState: Bundle?) {
        binding = createBinding()
        onViewCreate(binding, savedInstanceState)
        // Starts ViewModel event collection only once after binding initialization, preventing duplicate collectors.<br><br>
        // 바인딩 초기화 후 ViewModel 이벤트 수집을 1회만 시작하여 중복 수집을 방지합니다.<br>
        helper.startEventVmCollect { onEventVmCollect() }
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
    override fun onCreateView(binding: BINDING, savedInstanceState: Bundle?) {}

    /**
     * Override this method to collect ViewModel events.<br>
     * This hook is invoked once via `startEventVmCollect()` after binding initialization in `onCreate()`.<br>
     *
     * **Important**: To avoid duplicate collectors during configuration changes (e.g., screen rotation),
     * use `repeatOnLifecycle(Lifecycle.State.STARTED)` to automatically cancel and restart collection.<br><br>
     *
     * ViewModel 이벤트를 수집하려면 이 메서드를 오버라이드하세요.<br>
     * 이 훅은 `onCreate()`에서 바인딩 초기화 후 `startEventVmCollect()`를 통해 1회 호출됩니다.<br>
     *
     * **중요**: 구성 변경(예: 화면 회전) 시 중복 수집을 방지하려면
     * `repeatOnLifecycle(Lifecycle.State.STARTED)`를 사용하여 자동으로 수집을 취소하고 재시작하세요.<br>
     *
     * **Best Practice Example:**<br>
     * ```kotlin
     * override fun onEventVmCollect() {
     *     lifecycleScope.launch {
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
     *     lifecycleScope.launch {
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
     * **Note / 참고:**<br>
     * - This method is provided for users who cannot use Jetpack's `by viewModels()` delegate.<br>
     * - If you have `androidx.activity:activity-ktx` dependency, prefer using `by viewModels()` for lazy initialization.<br>
     * - This method creates the ViewModel immediately when called (not lazy).<br><br>
     * - 이 메서드는 Jetpack의 `by viewModels()` delegate를 사용할 수 없는 사용자를 위해 제공됩니다.<br>
     * - `androidx.activity:activity-ktx` 의존성이 있다면 lazy 초기화를 위해 `by viewModels()` 사용을 권장합니다.<br>
     * - 이 메서드는 호출 시 즉시 ViewModel을 생성합니다 (lazy 아님).<br>
     *
     * **Recommended (권장):**<br>
     * ```kotlin
     * private val viewModel: MainViewModel by viewModels()  // ✅ Lazy initialization
     * ```
     *
     * **Alternative (대안):**<br>
     * ```kotlin
     * private val viewModel: MainViewModel by lazy { getViewModel() }  // ✅ Manual lazy
     * ```
     *
     * @param T The type of the ViewModel to obtain.<br><br>
     *          가져올 ViewModel의 타입.<br>
     * @return The ViewModel instance of type T.<br><br>
     *         타입 T의 ViewModel 인스턴스.<br>
     */
    protected inline fun <reified T : ViewModel> getViewModel(): T = ViewModelProvider(this)[T::class.java]

    /**
     * Obtains a ViewModel of the specified type using ViewModelProvider with a custom factory.<br><br>
     * 커스텀 팩토리를 사용하여 ViewModelProvider로 지정된 타입의 ViewModel을 가져옵니다.<br>
     *
     * **Note / 참고:**<br>
     * - This method is provided for users who cannot use Jetpack's `by viewModels { factory }` delegate.<br>
     * - If you have `androidx.activity:activity-ktx` dependency, prefer using `by viewModels()` with factory lambda.<br><br>
     * - 이 메서드는 Jetpack의 `by viewModels { factory }` delegate를 사용할 수 없는 사용자를 위해 제공됩니다.<br>
     * - `androidx.activity:activity-ktx` 의존성이 있다면 팩토리 람다와 함께 `by viewModels()` 사용을 권장합니다.<br>
     *
     * **Recommended (권장):**<br>
     * ```kotlin
     * private val viewModel: MainViewModel by viewModels { myFactory }  // ✅ Lazy with factory
     * ```
     *
     * @param T The type of the ViewModel to obtain.<br><br>
     *          가져올 ViewModel의 타입.<br>
     * @param factory The Factory to use for creating the ViewModel instance.<br><br>
     *                ViewModel 인스턴스 생성에 사용할 Factory.<br>
     * @return The ViewModel instance of type T.<br><br>
     *         타입 T의 ViewModel 인스턴스.<br>
     */
    protected inline fun <reified T : ViewModel> getViewModel(factory: ViewModelProvider.Factory): T =
        ViewModelProvider(this, factory)[T::class.java]
}
