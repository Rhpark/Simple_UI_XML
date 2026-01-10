package kr.open.library.simple_ui.xml.ui.components.dialog.binding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.ui.components.dialog.root.RootDialogFragment

/**
 * A base DialogFragment class that uses DataBinding and provides common functionality for data-bound dialogs.<br>
 * Extends ParentBindingViewDialogFragment to inherit binding lifecycle and ViewModel event collection.<br><br>
 * DataBinding을 사용하고 데이터 바인딩 다이얼로그에 대한 공통 기능을 제공하는 기본 DialogFragment 클래스입니다.<br>
 * ParentBindingViewDialogFragment를 확장하여 바인딩 생명주기와 ViewModel 이벤트 수집을 상속받습니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android's DataBinding requires manual DataBindingUtil.inflate() calls for each DialogFragment.<br>
 * - DataBinding enables automatic UI updates when LiveData values change, reducing boilerplate update code (StateFlow requires manual collection).<br>
 * - This class automatically sets lifecycleOwner to enable LiveData observation, which developers often forget.<br>
 * - Provides a centralized place for DataBinding-specific cleanup (unbind, null lifecycleOwner).<br><br>
 * - Android의 DataBinding은 각 DialogFragment마다 수동으로 DataBindingUtil.inflate() 호출이 필요합니다.<br>
 * - DataBinding은 LiveData 값이 변경될 때 자동 UI 업데이트를 가능하게 하여 보일러플레이트 업데이트 코드를 줄입니다 (StateFlow는 수동 수집 필요).<br>
 * - 이 클래스는 개발자가 자주 잊는 LiveData 관찰을 활성화하기 위해 자동으로 lifecycleOwner를 설정합니다.<br>
 * - DataBinding 전용 정리(unbind, null lifecycleOwner)를 위한 중앙화된 장소를 제공합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses constructor parameter for layout resource ID to match Android's standard DataBinding pattern.<br>
 * - Automatically sets binding.lifecycleOwner in onViewCreated() to viewLifecycleOwner for proper view lifecycle binding.<br>
 * - Implements final createBinding() to prevent subclasses from breaking the DataBinding initialization contract.<br>
 * - Calls unbind() and nulls lifecycleOwner in onDestroyView() to prevent memory leaks from LiveData observers.<br><br>
 * - Android의 표준 DataBinding 패턴과 일치하도록 생성자 파라미터로 레이아웃 리소스 ID를 사용합니다.<br>
 * - 적절한 뷰 생명주기 바인딩을 위해 onViewCreated()에서 자동으로 binding.lifecycleOwner를 viewLifecycleOwner로 설정합니다.<br>
 * - final createBinding()을 구현하여 하위 클래스가 DataBinding 초기화 계약을 깨는 것을 방지합니다.<br>
 * - LiveData 옵저버로 인한 메모리 누수를 방지하기 위해 onDestroyView()에서 unbind()를 호출하고 lifecycleOwner를 null로 설정합니다.<br>
 *
 * **Important notes / 주의사항:**<br>
 * - ⚠️ CRITICAL: lifecycleOwner is automatically set to viewLifecycleOwner in onViewCreated(). Unlike Activity's BaseDataBindingActivity, you don't need to set it manually.<br>
 * - DataBinding automatically updates UI when LiveData values change - no need for manual observe() calls in XML-bound properties (StateFlow requires manual collection).<br>
 * - Always use repeatOnLifecycle(Lifecycle.State.STARTED) inside onEventVmCollect() to properly handle configuration changes.<br>
 * - Access the binding object via getBinding() method after super.onViewCreated() completes.<br><br>
 * - ⚠️ 중요: lifecycleOwner는 onViewCreated()에서 viewLifecycleOwner로 자동 설정됩니다. Activity의 BaseDataBindingActivity와 달리 수동으로 설정할 필요가 없습니다.<br>
 * - DataBinding은 LiveData 값이 변경될 때 자동으로 UI를 업데이트합니다 - XML에 바인딩된 프로퍼티에 대해 수동 observe() 호출이 필요하지 않습니다 (StateFlow는 수동 수집 필요).<br>
 * - 구성 변경을 올바르게 처리하려면 onEventVmCollect() 내부에서 항상 repeatOnLifecycle(Lifecycle.State.STARTED)를 사용하세요.<br>
 * - super.onViewCreated() 완료 후 getBinding() 메서드를 통해 바인딩 객체에 접근하세요.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Extend this class with your DialogFragment and pass the layout resource ID.<br>
 * 2. Access views through getBinding() in onViewCreated() or override onInitBind() for initialization.<br>
 * 3. Override onEventVmCollect() to collect ViewModel events with repeatOnLifecycle.<br>
 * 4. LiveData properties bound in XML will automatically update - no manual observation needed.<br><br>
 * 1. DialogFragment에서 이 클래스를 상속받고 레이아웃 리소스 ID를 전달하세요.<br>
 * 2. onViewCreated()에서 getBinding()을 통해 뷰에 접근하거나 초기화를 위해 onInitBind()를 오버라이드하세요.<br>
 * 3. repeatOnLifecycle과 함께 ViewModel 이벤트를 수집하려면 onEventVmCollect()를 오버라이드하세요.<br>
 * 4. XML에 바인딩된 LiveData 프로퍼티는 자동으로 업데이트됩니다 - 수동 관찰이 필요하지 않습니다.<br>
 *
 * **Usage example:**<br>
 * ```kotlin
 * class ConfirmDialog : BaseDataBindingDialogFragment<DialogConfirmBinding>(R.layout.dialog_confirm) {
 *     private val viewModel: ConfirmViewModel by lazy { getViewModel() }
 *
 *     override fun onInitBind(binding: DialogConfirmBinding) {
 *         binding.viewModel = viewModel
 *         // lifecycleOwner is already set automatically - no need to set it manually
 *     }
 *
 *     override fun onEventVmCollect() {
 *         viewLifecycleOwner.lifecycleScope.launch {
 *             repeatOnLifecycle(Lifecycle.State.STARTED) {
 *                 viewModel.dismissEvent.collect { safeDismiss() }
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
 * @param layoutRes The layout resource ID for the dialog.<br><br>
 *                  다이얼로그의 레이아웃 리소스 ID.<br>
 *
 * @param isAttachToParent Whether to attach the inflated view to the parent container.<br><br>
 *                         인플레이션된 뷰를 부모 컨테이너에 첨부할지 여부.<br>
 *
 * @see ParentBindingViewDialogFragment For the abstract parent class of all binding-enabled dialog fragments.<br><br>
 *      모든 바인딩 지원 DialogFragment의 추상 부모 클래스는 ParentBindingViewDialogFragment를 참조하세요.<br>
 *
 * @see RootDialogFragment For base class with dialog and permission features.<br><br>
 *      다이얼로그 및 권한 기능이 있는 기본 클래스는 RootDialogFragment를 참조하세요.<br>
 *
 * @see BaseDialogFragment For simple layout-based DialogFragment without DataBinding.<br><br>
 *      DataBinding 없이 간단한 레이아웃 기반 DialogFragment는 BaseDialogFragment를 참조하세요.<br>
 *
 * @see BaseViewBindingDialogFragment For ViewBinding-enabled DialogFragment.<br><br>
 *      ViewBinding을 사용하는 DialogFragment는 BaseViewBindingDialogFragment를 참조하세요.<br>
 */
public abstract class BaseDataBindingDialogFragment<BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    isAttachToParent: Boolean = false,
) : ParentBindingViewDialogFragment<BINDING>(isAttachToParent) {
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
     * Called immediately after onCreateView() has returned.<br>
     * Sets the binding's lifecycleOwner to viewLifecycleOwner for proper LiveData observation tied to the view lifecycle.<br><br>
     * onCreateView()가 반환된 직후 호출됩니다.<br>
     * 뷰 생명주기에 연결된 적절한 LiveData 관찰을 위해 바인딩의 lifecycleOwner를 viewLifecycleOwner로 설정합니다.<br>
     *
     * **Important / 중요:**<br>
     * - Using viewLifecycleOwner instead of Fragment's lifecycle ensures LiveData subscriptions are automatically cleaned up in onDestroyView().<br>
     * - This prevents memory leaks and stale observations when Fragment's view is destroyed but Fragment instance persists (e.g., in back stack).<br><br>
     * - Fragment의 생명주기 대신 viewLifecycleOwner를 사용하면 onDestroyView()에서 LiveData 구독이 자동으로 정리됩니다.<br>
     * - 이는 Fragment의 뷰가 파괴되었지만 Fragment 인스턴스가 유지될 때(예: 백 스택) 메모리 누수와 오래된 관찰을 방지합니다.<br>
     *
     * @param view The View returned by onCreateView().<br><br>
     *             onCreateView()가 반환한 View.<br>
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.<br><br>
     *                           null이 아닌 경우, 이 Fragment는 이전에 저장된 상태에서 다시 구성되고 있습니다.<br>
     */
    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // viewLifecycleOwner가 준비된 시점에 설정
        getBinding().lifecycleOwner = viewLifecycleOwner
        super.onViewCreated(view, savedInstanceState)
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
