package kr.open.library.simple_ui.xml.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.open.library.simple_ui.core.logcat.Logx

/**
 * A base Fragment class that uses DataBinding and provides common functionality for data-bound fragments.<br>
 * Extends RootFragment to inherit permission management.<br><br>
 * DataBinding을 사용하고 데이터 바인딩 Fragment에 대한 공통 기능을 제공하는 기본 Fragment 클래스입니다.<br>
 * RootFragment를 확장하여 권한 관리를 상속받습니다.<br>
 *
 * This class handles the following tasks:<br>
 * - Inflates the layout and sets up DataBinding<br>
 * - Sets the lifecycle owner for the binding (viewLifecycleOwner)<br>
 * - Provides a convenient method to obtain a ViewModel<br>
 * - Provides hook methods for view creation and event collection (`eventVmCollect()`)<br>
 * - `eventVmCollect()` is automatically invoked in `onViewCreated()` after `afterOnCreateView()` completes<br>
 * - Proper binding cleanup in onDestroyView<br><br>
 * 이 클래스는 다음과 같은 작업을 처리합니다:<br>
 * - 레이아웃을 인플레이션하고 DataBinding을 설정합니다<br>
 * - 바인딩에 대한 생명주기 소유자를 설정합니다 (viewLifecycleOwner)<br>
 * - ViewModel을 얻는 편리한 메서드를 제공합니다<br>
 * - 뷰 생성 훅과 이벤트 수집 훅(`eventVmCollect()`)을 제공합니다<br>
 * - `eventVmCollect()`는 `afterOnCreateView()` 완료 후 `onViewCreated()`에서 자동으로 호출됩니다<br>
 * - onDestroyView에서 적절한 바인딩 정리<br>
 *
 * Usage example:<br>
 * ```kotlin
 * class HomeFragment : BaseBindingFragment<FragmentHomeBinding>(R.layout.fragment_home) {
 *     private val viewModel: HomeViewModel by lazy { getViewModel() }
 *
 *     override fun afterOnCreateView(rootView: View, savedInstanceState: Bundle?) {
 *         binding.viewModel = viewModel
 *         setupViews()
 *     }
 *
 *     override fun eventVmCollect() {
 *         // Automatically called by BaseBindingFragment in onViewCreated()
 *         viewLifecycleOwner.lifecycleScope.launch {
 *             viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
 *                 viewModel.events.collect { handleEvent(it) }
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
 * @param layoutRes The layout resource ID for the fragment.<br><br>
 *                  Fragment의 레이아웃 리소스 ID.<br>
 *
 * @param isAttachToParent Whether to attach the inflated view to the parent container.<br><br>
 *                         인플레이션된 뷰를 부모 컨테이너에 첨부할지 여부.<br>
 *
 * @see RootFragment For base class with permission features.<br><br>
 *      권한 기능이 있는 기본 클래스는 RootFragment를 참조하세요.<br>
 *
 * @see BaseFragment For simple layout-based Fragment without DataBinding.<br><br>
 *      DataBinding 없이 간단한 레이아웃 기반 Fragment는 BaseFragment를 참조하세요.<br>
 */
public abstract class BaseBindingFragment<BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false,
) : RootFragment() {
    /**
     * Internal backing field for binding.<br><br>
     * binding의 내부 백킹 필드입니다.<br>
     */
    private var _binding: BINDING? = null

    /**
     * The DataBinding object for the fragment.<br>
     * Throws IllegalStateException if accessed after onDestroyView().<br><br>
     * Fragment의 DataBinding 객체입니다.<br>
     * onDestroyView() 이후에 접근하면 IllegalStateException이 발생합니다.<br>
     *
     * @return The DataBinding object for the fragment.<br><br>
     *         Fragment의 DataBinding 객체.<br>
     * @throws IllegalStateException if accessed after onDestroyView().<br><br>
     *                               onDestroyView() 이후에 접근하는 경우.
     */
    public val binding: BINDING
        get() = _binding
            ?: throw IllegalStateException("Binding accessed after onDestroyView()")

    /**
     * Called to have the fragment instantiate its user interface view.
     * Inflates the layout using DataBinding and calls afterOnCreateView hook.<br><br>
     * Fragment가 사용자 인터페이스 뷰를 인스턴스화하기 위해 호출됩니다.<br>
     * DataBinding을 사용하여 레이아웃을 인플레이션하고 afterOnCreateView 훅을 호출합니다.<br>
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.<br><br>
     *                 Fragment의 뷰를 인플레이션하는 데 사용할 수 있는 LayoutInflater 객체.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.<br><br>
     *                  null이 아닌 경우, Fragment의 UI가 첨부될 부모 뷰.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.<br><br>
     *                           null이 아닌 경우, 이 Fragment는 이전에 저장된 상태에서 다시 구성되고 있습니다.
     * @return Return the View for the fragment's UI.<br><br>
     *         Fragment UI의 View를 반환.<br>
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DataBindingUtil.inflate(inflater, layoutRes, container, isAttachToParent)
        return binding.root.also { afterOnCreateView(it, savedInstanceState) }
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored into the view.<br>
     * Override this method to set up views and bind ViewModel to the binding.<br><br>
     * onCreateView()가 반환된 직후에 호출되지만 저장된 상태가 뷰에 복원되기 전에 호출됩니다.<br>
     * 뷰를 설정하고 ViewModel을 바인딩에 연결하려면 이 메서드를 오버라이드하세요.<br>
     *
     * @param rootView The root view of the fragment's layout.<br><br>
     *                 Fragment 레이아웃의 루트 뷰.<br>
     *
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.<br><br>
     *                           null이 아닌 경우 이 Fragment는 이전에 저장된 상태에서 다시 생성됩니다.<br>
     */
    protected open fun afterOnCreateView(rootView: View, savedInstanceState: Bundle?) {}

    /**
     * Called immediately after onCreateView has returned, but before any saved state has been restored in to the view.
     * Sets the lifecycle owner for the binding to viewLifecycleOwner.<br><br>
     * onCreateView가 반환된 직후에 호출되지만 저장된 상태가 뷰에 복원되기 전에 호출됩니다.<br>
     * 바인딩의 생명주기 소유자를 viewLifecycleOwner로 설정합니다.<br>
     *
     * @param view The View returned by onCreateView.<br><br>
     *             onCreateView에서 반환된 View.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.<br><br>
     *                           null이 아닌 경우, 이 Fragment는 이전에 저장된 상태에서 다시 구성되고 있습니다.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        startEventVmCollect()
    }

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
     * Override this method to set up ViewModel event collection.<br>
     * Typically used to collect Flow events from ViewModel using viewLifecycleOwner.lifecycleScope.<br>
     * This method is automatically called in `onViewCreated()` after `afterOnCreateView()` completes,
     * ensuring all child class initialization is finished.<br><br>
     * ViewModel 이벤트 수집을 설정하려면 이 메서드를 오버라이드하세요.<br>
     * 일반적으로 viewLifecycleOwner.lifecycleScope를 사용하여 ViewModel의 Flow 이벤트를 수집하는 데 사용됩니다.<br>
     * 이 메서드는 `afterOnCreateView()` 완료 후 `onViewCreated()`에서 자동으로 호출되어,
     * 모든 자식 클래스 초기화가 완료되도록 보장합니다.<br>
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
    protected inline fun <reified T : ViewModel> Fragment.getViewModel(
        factory: ViewModelProvider.Factory,
    ): T = ViewModelProvider(this, factory)[T::class.java]

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     * Cleans up the binding reference to prevent memory leaks.<br><br>
     * onCreateView에서 생성된 뷰가 Fragment에서 분리될 때 호출됩니다.<br>
     * 메모리 누수를 방지하기 위해 바인딩 참조를 정리합니다.<br>
     */
    override fun onDestroyView() {
        super.onDestroyView()
        eventCollectStarted = false
        binding.lifecycleOwner = null
        binding.unbind()
        _binding = null
    }
}
