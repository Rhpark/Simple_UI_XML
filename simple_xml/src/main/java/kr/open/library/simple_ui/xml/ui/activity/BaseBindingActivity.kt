package kr.open.library.simple_ui.xml.ui.activity

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * A base Activity class that uses DataBinding and provides common functionality for data-bound activities.<br>
 * Extends RootActivity to inherit system bar control and permission management.<br><br>
 * DataBinding을 사용하고 데이터 바인딩 Activity에 대한 공통 기능을 제공하는 기본 Activity 클래스입니다.<br>
 * RootActivity를 확장하여 시스템 바 제어와 권한 관리를 상속받습니다.<br>
 *
 * This class handles the following tasks:<br>
 * - Inflates the layout and sets up DataBinding<br>
 * - Sets the lifecycle owner for the binding<br>
 * - Provides a convenient method to obtain a ViewModel<br>
 * - Provides hook methods for view creation and event collection<br><br>
 * 이 클래스는 다음과 같은 작업을 처리합니다:<br>
 * - 레이아웃을 인플레이션하고 DataBinding을 설정합니다<br>
 * - 바인딩에 대한 생명주기 소유자를 설정합니다<br>
 * - ViewModel을 얻는 편리한 메서드를 제공합니다<br>
 * - 뷰 생성 및 이벤트 수집을 위한 훅 메서드를 제공합니다<br>
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
 *     override fun eventVmCollect() {
 *         lifecycleScope.launch {
 *             repeatOnLifecycle(Lifecycle.State.STARTED) {
 *                 viewModel.events.collect { event ->
 *                     handleEvent(event)
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 * <br><br>
 * 사용 예제:<br>
 * ```kotlin
 * class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {
 *     private val viewModel: MainViewModel by lazy { getViewModel() }
 *
 *     override fun onCreateView(rootView: View, savedInstanceState: Bundle?) {
 *         binding.viewModel = viewModel
 *         setupViews()
 *     }
 *
 *     override fun eventVmCollect() {
 *         lifecycleScope.launch {
 *             repeatOnLifecycle(Lifecycle.State.STARTED) {
 *                 viewModel.events.collect { event ->
 *                     handleEvent(event)
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 * <br>
 *
 * @param BINDING The type of the DataBinding class.<br><br>
 *                DataBinding 클래스의 타입.<br>
 *
 * @param layoutRes The layout resource ID for the activity.<br><br>
 *                  Activity의 레이아웃 리소스 ID.<br>
 *
 * @see RootActivity For base class with system bar and permission features.<br><br>
 *      시스템 바와 권한 기능이 있는 기본 클래스는 RootActivity를 참조하세요.<br>
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutRes)
        onCreateView(binding.root, savedInstanceState)
        binding.lifecycleOwner = this
    }

    /**
     * Called after binding is initialized but before lifecycleOwner is set.<br>
     * Override this method to set up views and bind ViewModel to the binding.<br><br>
     * 바인딩이 초기화된 후 lifecycleOwner가 설정되기 전에 호출됩니다.<br>
     * 뷰를 설정하고 ViewModel을 바인딩에 연결하려면 이 메서드를 오버라이드하세요.<br>
     *
     * @param rootView The root view of the inflated layout.<br><br>
     *                 인플레이션된 레이아웃의 루트 뷰.<br>
     *
     * @param savedInstanceState The saved instance state bundle, if any.<br><br>
     *                           저장된 인스턴스 상태 번들 (있는 경우).<br>
     */
    protected open fun onCreateView(
        rootView: View,
        savedInstanceState: Bundle?,
    ) {
    }

    /**
     * Override this method to set up ViewModel event collection.<br>
     * Typically used to collect Flow events from ViewModel using lifecycleScope.<br><br>
     * ViewModel 이벤트 수집을 설정하려면 이 메서드를 오버라이드하세요.<br>
     * 일반적으로 lifecycleScope를 사용하여 ViewModel의 Flow 이벤트를 수집하는 데 사용됩니다.<br>
     */
    protected open fun eventVmCollect() {}

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
}
