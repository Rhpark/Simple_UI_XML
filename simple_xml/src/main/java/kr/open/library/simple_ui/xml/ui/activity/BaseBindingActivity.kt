package kr.open.library.simple_ui.xml.ui.activity

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.open.library.simple_ui.core.logcat.Logx

/**
 * A base Activity class that uses DataBinding and provides common functionality for data-bound activities.<br>
 * Extends RootActivity to inherit permission management.<br><br>
 * DataBinding을 사용하고 데이터 바인딩 Activity에 대한 공통 기능을 제공하는 기본 Activity 클래스입니다.<br>
 * RootActivity를 확장하여 권한 관리를 상속받습니다.<br>
 *
 * This class handles the following tasks:<br>
 * - Inflates the layout and sets up DataBinding<br>
 * - Sets the lifecycle owner for the binding<br>
 * - Provides a convenient method to obtain a ViewModel<br>
 * - Provides hook methods for view creation (`onCreateView()`) and event collection (`onEventVmCollect()`)<br>
 * - Starts event collection once via `startEventVmCollect()` in `onCreate()` after `onCreateView()` completes<br><br>
 * 이 클래스는 다음과 같은 작업을 처리합니다:<br>
 * - 레이아웃을 인플레이션하고 DataBinding을 설정합니다<br>
 * - 바인딩에 대한 생명주기 소유자를 설정합니다<br>
 * - ViewModel을 얻는 편리한 메서드를 제공합니다<br>
 * - 뷰 생성 훅(`onCreateView()`)과 이벤트 수집 훅(`onEventVmCollect()`)을 제공합니다<br>
 * - `onCreateView()` 완료 후 `onCreate()`에서 `startEventVmCollect()`로 이벤트 수집을 1회 시작합니다<br>
 *
 * Usage example:<br>
 * ```kotlin
 * class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {
 *     private val viewModel: MainViewModel by lazy { getViewModel() }
 *
 *     override fun onCreateView(rootView: View, savedInstanceState: Bundle?) {
 *         binding.viewModel = viewModel
 *         setupViews()
 *     }
 *
 *     override fun onEventVmCollect() {
 *         // Automatically started once by BaseBindingActivity after onCreateView().<br><br>
 *         // BaseBindingActivity가 onCreateView() 이후 1회 자동 시작합니다.<br>
 *         lifecycleScope.launch {
 *             repeatOnLifecycle(Lifecycle.State.STARTED) {
 *                 viewModel.events.collect { event -> handleEvent(event) }
 *             }
 *         }
 *     }
 * }
 * ```
 * <br><br>
 *
 * @param BINDING The type of the DataBinding class.<br><br>
 *                DataBinding 클래스의 타입.<br>
 *
 * @param layoutRes The layout resource ID for the activity.<br><br>
 *                  Activity의 레이아웃 리소스 ID.<br>
 *
 * @see BaseActivity For simple layout-based Activity without DataBinding.<br><br>
 *      DataBinding 없이 간단한 레이아웃 기반 Activity는 BaseActivity를 참조하세요.<br>
 */
public abstract class BaseBindingActivity<BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
) : RootActivity() {
    /**
     * The DataBinding object for the activity.<br>
     * Initialized in onCreate and available for use in onCreateView and afterwards.<br><br>
     * Activity의 DataBinding 객체입니다.<br>
     * onCreate에서 초기화되며 onCreateView 이후부터 사용 가능합니다.<br>
     */
    protected lateinit var binding: BINDING
        private set

    /**
     * Called when the activity is starting.
     * Initializes DataBinding, sets up the layout, and calls onCreateView hook.<br><br>
     * 액티비티가 시작될 때 호출됩니다.<br>
     * DataBinding을 초기화하고, 레이아웃을 설정하며, onCreateView 훅을 호출합니다.<br>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState.<br><br>
     *                           액티비티가 이전에 종료된 후 다시 초기화되는 경우,
     *                           이 Bundle에는 onSaveInstanceState에서 가장 최근에 제공된 데이터가 포함됩니다.
     */
    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutRes)
        binding.lifecycleOwner = this
        onCreateView(binding.root, savedInstanceState)
        // Starts ViewModel event collection only once after onCreateView() completes, preventing duplicate collectors.<br><br>
        // onCreateView() 완료 후 ViewModel 이벤트 수집을 1회만 시작하여 중복 수집을 방지합니다.<br>
        startEventVmCollect()
    }

    /**
     * Called after binding is initialized.<br>
     * Override this method to set up views and bind ViewModel to the binding.<br><br>
     * 바인딩이 초기화된 후 호출됩니다.<br>
     * 뷰를 설정하고 ViewModel을 바인딩에 연결하려면 이 메서드를 오버라이드하세요.<br>
     *
     * @param rootView The root view of the inflated layout.<br><br>
     *                 인플레이션된 레이아웃의 루트 뷰.<br>
     *
     * @param savedInstanceState The saved instance state bundle, if any.<br><br>
     *                           저장된 인스턴스 상태 번들 (있는 경우).<br>
     */
    protected open fun onCreateView(rootView: View, savedInstanceState: Bundle?) {}

    /**
     * Tracks whether ViewModel event collection has already started for this Activity instance.<br><br>
     * 현재 Activity 인스턴스에서 ViewModel 이벤트 수집이 이미 시작되었는지 여부를 추적합니다.<br>
     */
    private var eventCollectStarted = false

    /**
     * Starts ViewModel event collection only once per Activity instance.<br>
     * Called from `onCreate()` after `onCreateView()` to prevent duplicate collectors.<br>
     * Subsequent calls are ignored and logged as warnings.<br><br>
     * Activity 인스턴스당 ViewModel 이벤트 수집을 1회만 시작합니다.<br>
     * `onCreateView()` 이후 `onCreate()`에서 호출되어 중복 수집을 방지합니다.<br>
     * 이후 호출은 무시되며 경고 로그를 남깁니다.<br>
     */
    private fun startEventVmCollect() {
        if (eventCollectStarted) {
            Logx.w("Already started event collection.")
            return
        }
        eventCollectStarted = true
        onEventVmCollect()
    }

    /**
     * Override this method to collect ViewModel events.<br>
     * This hook is invoked once via `startEventVmCollect()` after `onCreateView()` completes.<br>
     *
     * **Important**: To avoid duplicate collectors during configuration changes (e.g., screen rotation),
     * use `repeatOnLifecycle(Lifecycle.State.STARTED)` to automatically cancel and restart collection.<br><br>
     *
     * ViewModel 이벤트를 수집하려면 이 메서드를 오버라이드하세요.<br>
     * 이 훅은 `onCreateView()` 완료 후 `startEventVmCollect()`를 통해 1회 호출됩니다.<br>
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
    protected open fun onEventVmCollect() {}

    /**
     * Obtains a ViewModel of the specified type using ViewModelProvider.<br><br>
     * ViewModelProvider를 사용하여 지정된 타입의 ViewModel을 가져옵니다.<br>
     *
     * @param T The type of the ViewModel to obtain.<br><br>
     *          가져올 ViewModel의 타입.
     * @return The ViewModel instance of type T.<br><br>
     *         타입 T의 ViewModel 인스턴스.<br>
     */
    protected inline fun <reified T : ViewModel> getViewModel(): T = ViewModelProvider(this)[T::class.java]

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
    protected inline fun <reified T : ViewModel> getViewModel(
        factory: ViewModelProvider.Factory,
    ): T = ViewModelProvider(this, factory)[T::class.java]
}
