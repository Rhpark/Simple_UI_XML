package kr.open.library.simple_ui.xml.ui.components.activity.binding

import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * A base Activity class that uses DataBinding and provides common functionality for data-bound activities.<br>
 * Extends ParentsBindingActivity to provide common binding functionality and permission management.<br><br>
 * DataBinding을 사용하고 데이터 바인딩 Activity에 대한 공통 기능을 제공하는 기본 Activity 클래스입니다.<br>
 * ParentsBindingActivity를 상속받아 공통 바인딩 기능과 권한 관리를 제공합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android's DataBinding requires manual DataBindingUtil.setContentView() and lifecycleOwner setup for each Activity.<br>
 * - This class eliminates boilerplate by accepting a layout resource ID and automatically configuring DataBinding.<br>
 * - Enables two-way data binding with ViewModel and automatic UI updates through LiveData observables (StateFlow requires manual collection).<br><br>
 * - Android의 DataBinding은 각 Activity마다 수동으로 DataBindingUtil.setContentView() 및 lifecycleOwner 설정이 필요합니다.<br>
 * - 이 클래스는 레이아웃 리소스 ID를 받아 자동으로 DataBinding을 구성하여 보일러플레이트를 제거합니다.<br>
 * - ViewModel과의 양방향 데이터 바인딩 및 LiveData 옵저버블을 통한 자동 UI 업데이트를 가능하게 합니다 (StateFlow는 수동 수집 필요).<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses constructor parameter for layout resource ID to enable simple single-line class declaration.<br>
 * - Automatically sets lifecycleOwner in createBinding() to enable LiveData observation in XML layouts.<br>
 * - Implements final createBinding() to prevent subclasses from breaking the DataBinding initialization contract.<br>
 * - Inherits event collection infrastructure from ParentsBindingActivity for consistent ViewModel integration.<br><br>
 * - 간단한 한 줄 클래스 선언을 위해 생성자 파라미터로 레이아웃 리소스 ID를 사용합니다.<br>
 * - XML 레이아웃에서 LiveData 관찰을 가능하게 하기 위해 createBinding()에서 자동으로 lifecycleOwner를 설정합니다.<br>
 * - final createBinding()을 구현하여 하위 클래스가 DataBinding 초기화 계약을 깨는 것을 방지합니다.<br>
 * - 일관된 ViewModel 통합을 위해 ParentsBindingActivity로부터 이벤트 수집 인프라를 상속받습니다.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Extend this class with your Activity and pass the layout resource ID.<br>
 * 2. Bind your ViewModel to the binding object in onInitBind() (binding is passed as parameter).<br>
 * 3. Use XML data binding expressions to bind views to ViewModel properties.<br>
 * 4. Override onEventVmCollect() to collect ViewModel events with repeatOnLifecycle().<br>
 * 5. Access binding in other methods via getBinding() if needed.<br><br>
 * 1. Activity에서 이 클래스를 상속받고 레이아웃 리소스 ID를 전달하세요.<br>
 * 2. onInitBind()에서 ViewModel을 binding 객체에 바인딩하세요 (binding은 파라미터로 전달됩니다).<br>
 * 3. XML 데이터 바인딩 표현식을 사용하여 뷰를 ViewModel 프로퍼티에 바인딩하세요.<br>
 * 4. repeatOnLifecycle()로 ViewModel 이벤트를 수집하려면 onEventVmCollect()를 오버라이드하세요.<br>
 * 5. 필요한 경우 다른 메서드에서 getBinding()을 통해 바인딩에 접근하세요.<br>
 *
 * **Usage example:**<br>
 * ```kotlin
 * class MainActivity : BaseDataBindingActivity<ActivityMainBinding>(R.layout.activity_main) {
 *     private val viewModel: MainViewModel by lazy { getViewModel() }
 *
 *     override fun onInitBind(binding: ActivityMainBinding) {
 *         binding.viewModel = viewModel
 *         setupViews()
 *     }
 *
 *     override fun onEventVmCollect() {
 *         lifecycleScope.launch {
 *             repeatOnLifecycle(Lifecycle.State.STARTED) {
 *                 viewModel.events.collect { event -> handleEvent(event) }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param BINDING The type of the DataBinding class.<br><br>
 *                DataBinding 클래스의 타입.<br>
 * @param layoutRes The layout resource ID for the activity.<br><br>
 *                  Activity의 레이아웃 리소스 ID.<br>
 *
 * @see ParentsBindingActivity For the parent class providing binding lifecycle.<br><br>
 *      바인딩 생명주기를 제공하는 부모 클래스는 ParentsBindingActivity를 참조하세요.<br>
 *
 * @see BaseActivity For simple layout-based Activity without DataBinding.<br><br>
 *      DataBinding 없이 간단한 레이아웃 기반 Activity는 BaseActivity를 참조하세요.<br>
 *
 * @see BaseViewBindingActivity For ViewBinding-enabled Activity.<br><br>
 *      ViewBinding을 사용하는 Activity는 BaseViewBindingActivity를 참조하세요.<br>
 */
public abstract class BaseDataBindingActivity<BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
) : ParentsBindingActivity<BINDING>() {
    /**
     * Creates the DataBinding instance using DataBindingUtil.<br>
     * Also sets the lifecycle owner for the binding to enable LiveData observation in XML.<br><br>
     * DataBindingUtil을 사용하여 DataBinding 인스턴스를 생성합니다.<br>
     * XML에서 LiveData 관찰을 가능하게 하기 위해 바인딩의 라이프사이클 소유자를 설정합니다.<br>
     *
     * @return The initialized DataBinding instance.<br><br>
     *         초기화된 DataBinding 인스턴스.<br>
     */
    final override fun createBinding(): BINDING = DataBindingUtil.setContentView<BINDING>(this, layoutRes).apply {
        lifecycleOwner = this@BaseDataBindingActivity
    }
}
