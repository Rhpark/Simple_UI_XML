package kr.open.library.simple_ui.xml.ui.components.fragment.binding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * A base Fragment class that uses DataBinding and provides common functionality for data-bound fragments.<br>
 * Extends ParentsBindingFragment to provide common binding functionality and permission management.<br><br>
 * DataBinding을 사용하고 데이터 바인딩 Fragment에 대한 공통 기능을 제공하는 기본 Fragment 클래스입니다.<br>
 * ParentsBindingFragment를 상속받아 공통 바인딩 기능과 권한 관리를 제공합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android's DataBinding requires manual DataBindingUtil.inflate() and lifecycleOwner setup for each Fragment.<br>
 * - This class eliminates boilerplate by accepting a layout resource ID and automatically configuring DataBinding.<br>
 * - Enables two-way data binding with ViewModel and automatic UI updates through LiveData observables (StateFlow requires manual collection).<br><br>
 * - Android의 DataBinding은 각 Fragment마다 수동으로 DataBindingUtil.inflate() 및 lifecycleOwner 설정이 필요합니다.<br>
 * - 이 클래스는 레이아웃 리소스 ID를 받아 자동으로 DataBinding을 구성하여 보일러플레이트를 제거합니다.<br>
 * - ViewModel과의 양방향 데이터 바인딩 및 LiveData 옵저버블을 통한 자동 UI 업데이트를 가능하게 합니다 (StateFlow는 수동 수집 필요).<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses constructor parameter for layout resource ID to enable simple single-line class declaration.<br>
 * - Automatically sets lifecycleOwner to viewLifecycleOwner in onViewCreated() for LiveData observation tied to the view lifecycle.<br>
 * - Implements final createBinding() to prevent subclasses from breaking the DataBinding initialization contract.<br>
 * - Inherits event collection infrastructure from ParentsBindingFragment for consistent ViewModel integration.<br>
 * - Properly cleans up binding in onDestroyView() by setting lifecycleOwner to null and calling unbind().<br><br>
 * - 간단한 한 줄 클래스 선언을 위해 생성자 파라미터로 레이아웃 리소스 ID를 사용합니다.<br>
 * - onViewCreated()에서 뷰 생명주기에 연결된 LiveData 관찰을 위해 lifecycleOwner를 viewLifecycleOwner로 자동 설정합니다.<br>
 * - final createBinding()을 구현하여 하위 클래스가 DataBinding 초기화 계약을 깨는 것을 방지합니다.<br>
 * - 일관된 ViewModel 통합을 위해 ParentsBindingFragment로부터 이벤트 수집 인프라를 상속받습니다.<br>
 * - onDestroyView()에서 lifecycleOwner를 null로 설정하고 unbind()를 호출하여 바인딩을 적절히 정리합니다.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Extend this class with your Fragment and pass the layout resource ID.<br>
 * 2. The lifecycleOwner is automatically set to viewLifecycleOwner in onViewCreated() - no manual setup required for LiveData observation.<br>
 * 3. Bind your ViewModel to the binding object in onInitBind() (binding is accessible via binding property).<br>
 * 4. Use XML data binding expressions to bind views to ViewModel properties.<br>
 * 5. Override onEventVmCollect(binding:BINDING) to collect ViewModel events with repeatOnLifecycle().<br><br>
 * 1. Fragment에서 이 클래스를 상속받고 레이아웃 리소스 ID를 전달하세요.<br>
 * 2. lifecycleOwner는 onViewCreated()에서 viewLifecycleOwner로 자동 설정됨 - LiveData 관찰을 위한 수동 설정이 필요하지 않습니다.<br>
 * 3. onInitBind()에서 ViewModel을 binding 객체에 바인딩하세요 (binding은 binding 프로퍼티를 통해 접근 가능).<br>
 * 4. XML 데이터 바인딩 표현식을 사용하여 뷰를 ViewModel 프로퍼티에 바인딩하세요.<br>
 * 5. repeatOnLifecycle()로 ViewModel 이벤트를 수집하려면 onEventVmCollect(binding:BINDING)를 오버라이드하세요.<br>
 *
 * **Usage example:**<br>
 * ```kotlin
 * class HomeFragment : BaseDataBindingFragment<FragmentHomeBinding>(R.layout.fragment_home) {
 *     private val viewModel: HomeViewModel by lazy { getViewModel() }
 *
 *     override fun onInitBind(binding: FragmentHomeBinding) {
 *         binding.viewModel = viewModel
 *         // lifecycleOwner is already set automatically - no manual setup needed
 *         setupViews()
 *     }
 *
 *     override fun onEventVmCollect(binding:BINDING) {
 *         viewLifecycleOwner.lifecycleScope.launch {
 *             viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
 *                 viewModel.events.collect { handleEvent(it) }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param BINDING The type of the DataBinding class.<br><br>
 *                DataBinding 클래스의 타입.<br>
 * @param layoutRes The layout resource ID for the fragment.<br><br>
 *                  Fragment의 레이아웃 리소스 ID.<br>
 * @param isAttachToParent Whether to attach the inflated view to the parent container.<br><br>
 *                         인플레이션된 뷰를 부모 컨테이너에 첨부할지 여부.<br>
 *
 * @see ParentsBindingFragment For the parent class providing binding lifecycle.<br><br>
 *      바인딩 생명주기를 제공하는 부모 클래스는 ParentsBindingFragment를 참조하세요.<br>
 *
 * @see BaseViewBindingFragment For ViewBinding-enabled Fragment.<br><br>
 *      ViewBinding을 사용하는 Fragment는 BaseViewBindingFragment를 참조하세요.<br>
 */
public abstract class BaseDataBindingFragment<BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    isAttachToParent: Boolean = false,
) : ParentsBindingFragment<BINDING>(isAttachToParent) {
    /**
     * Creates the DataBinding instance using DataBindingUtil.<br>
     * Note: lifecycleOwner is set later in onViewCreated() to viewLifecycleOwner for proper view lifecycle binding.<br><br>
     * DataBindingUtil을 사용하여 DataBinding 인스턴스를 생성합니다.<br>
     * 참고: lifecycleOwner는 적절한 뷰 생명주기 바인딩을 위해 onViewCreated()에서 viewLifecycleOwner로 설정됩니다.<br>
     *
     * @param inflater The LayoutInflater object to inflate views.<br><br>
     *                 뷰를 인플레이션할 LayoutInflater 객체.<br>
     * @param container The parent view container.<br><br>
     *                  부모 뷰 컨테이너.<br>
     * @param isAttachToParent Whether to attach to parent.<br><br>
     *                         부모에 첨부할지 여부.<br>
     * @return The initialized DataBinding instance (lifecycleOwner will be set in onViewCreated).<br><br>
     *         초기화된 DataBinding 인스턴스 (lifecycleOwner는 onViewCreated에서 설정됨).<br>
     */
    final override fun createBinding(inflater: LayoutInflater, container: ViewGroup?, isAttachToParent: Boolean): BINDING =
        DataBindingUtil.inflate<BINDING>(inflater, layoutRes, container, isAttachToParent)

    /**
     * Called immediately after onViewCreate() has returned.<br>
     * Sets the binding's lifecycleOwner to viewLifecycleOwner for proper LiveData observation tied to the view lifecycle.<br><br>
     * onViewCreate()가 반환된 직후 호출됩니다.<br>
     * 뷰 생명주기에 연결된 적절한 LiveData 관찰을 위해 바인딩의 lifecycleOwner를 viewLifecycleOwner로 설정합니다.<br>
     *
     * **Important / 중요:**<br>
     * - Using viewLifecycleOwner instead of Fragment's lifecycle ensures LiveData subscriptions are automatically cleaned up in onDestroyView().<br>
     * - This prevents memory leaks and stale observations when Fragment's view is destroyed but Fragment instance persists (e.g., in back stack).<br><br>
     * - Fragment의 생명주기 대신 viewLifecycleOwner를 사용하면 onDestroyView()에서 LiveData 구독이 자동으로 정리됩니다.<br>
     * - 이는 Fragment의 뷰가 파괴되었지만 Fragment 인스턴스가 유지될 때(예: 백 스택) 메모리 누수와 오래된 관찰을 방지합니다.<br>
     */
    @CallSuper
    override fun onCreateView(binding: BINDING, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.<br>
     * Cleans up the binding reference to prevent memory leaks by setting lifecycleOwner to null and calling unbind().<br><br>
     * onCreateView에서 생성된 뷰가 Fragment에서 분리될 때 호출됩니다.<br>
     * lifecycleOwner를 null로 설정하고 unbind()를 호출하여 메모리 누수를 방지하기 위해 바인딩 참조를 정리합니다.<br>
     */
    @CallSuper
    override fun onDestroyView() {
        getBinding().lifecycleOwner = null
        getBinding().unbind()
        super.onDestroyView()
    }
}
