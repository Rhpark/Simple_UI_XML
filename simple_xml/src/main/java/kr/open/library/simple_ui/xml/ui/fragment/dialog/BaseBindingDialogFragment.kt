package kr.open.library.simple_ui.xml.ui.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * A base DialogFragment class that uses DataBinding and provides common functionality for data-bound dialogs.<br>
 * Extends RootDialogFragment to inherit dialog functionality and permission management.<br><br>
 * DataBinding을 사용하고 데이터 바인딩 다이얼로그에 대한 공통 기능을 제공하는 기본 DialogFragment 클래스입니다.<br>
 * RootDialogFragment를 확장하여 다이얼로그 기능과 권한 관리를 상속받습니다.<br>
 *
 * Features:<br>
 * - Inflates the layout and sets up DataBinding<br>
 * - Sets the lifecycle owner for the binding (viewLifecycleOwner)<br>
 * - Provides a convenient method to obtain a ViewModel<br>
 * - Provides hook methods for view creation and event collection<br>
 * - Background color and drawable customization<br>
 * - Proper binding cleanup in onDestroyView<br>
 * - All RootDialogFragment features (animation, gravity, permissions)<br><br>
 * 기능:<br>
 * - 레이아웃을 인플레이션하고 DataBinding을 설정합니다<br>
 * - 바인딩에 대한 생명주기 소유자를 설정합니다 (viewLifecycleOwner)<br>
 * - ViewModel을 얻는 편리한 메서드를 제공합니다<br>
 * - 뷰 생성 및 이벤트 수집을 위한 훅 메서드를 제공합니다<br>
 * - 배경색 및 drawable 커스터마이징<br>
 * - onDestroyView에서 적절한 바인딩 정리<br>
 * - 모든 RootDialogFragment 기능 (애니메이션, gravity, 권한)<br>
 *
 * Usage example:<br>
 * ```kotlin
 * class ConfirmDialog : BaseBindingDialogFragment<DialogConfirmBinding>(R.layout.dialog_confirm) {
 *     private val viewModel: ConfirmViewModel by lazy { getViewModel() }
 *
 *     override fun afterOnCreateView(rootView: View, savedInstanceState: Bundle?) {
 *         binding.viewModel = viewModel
 *         binding.btnOk.setOnClickListener { safeDismiss() }
 *     }
 *
 *     override fun eventVmCollect() {
 *         viewLifecycleOwner.lifecycleScope.launch {
 *             viewModel.result.collect { handleResult(it) }
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
 * @see RootDialogFragment For base class with dialog and permission features.<br><br>
 *      다이얼로그 및 권한 기능이 있는 기본 클래스는 RootDialogFragment를 참조하세요.<br>
 *
 * @see BaseDialogFragment For simple layout-based DialogFragment without DataBinding.<br><br>
 *      DataBinding 없이 간단한 레이아웃 기반 DialogFragment는 BaseDialogFragment를 참조하세요.<br>
 */
public abstract class BaseBindingDialogFragment<BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false,
) : RootDialogFragment() {
    /**
     * Internal backing field for binding.<br><br>
     * binding의 내부 백킹 필드입니다.<br>
     */
    private var _binding: BINDING? = null

    /**
     * The DataBinding object for the dialog.<br>
     * Throws IllegalStateException if accessed after onDestroyView().<br><br>
     * 다이얼로그의 DataBinding 객체입니다.<br>
     * onDestroyView() 이후에 접근하면 IllegalStateException이 발생합니다.<br>
     */
    public val binding: BINDING
        get() = _binding
            ?: throw IllegalStateException("Binding accessed after onDestroyView()")

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
     * @param rootView The root view of the dialog's layout.<br><br>
     *                 다이얼로그 레이아웃의 루트 뷰.<br>
     *
     * @param savedInstanceState If non-null, this dialog is being re-constructed from a previous saved state.<br><br>
     *                           null이 아닌 경우 이 다이얼로그는 이전에 저장된 상태에서 다시 생성됩니다.<br>
     */
    protected open fun afterOnCreateView(rootView: View, savedInstanceState: Bundle?) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        getBackgroundColor()?.let { setBackgroundColor(it) }
        getBackgroundResId()?.let { setBackgroundDrawable(it) }
    }

    /**
     * Override this method to set up ViewModel event collection.<br>
     * Typically used to collect Flow events from ViewModel using viewLifecycleOwner.lifecycleScope.<br><br>
     * ViewModel 이벤트 수집을 설정하려면 이 메서드를 오버라이드하세요.<br>
     * 일반적으로 viewLifecycleOwner.lifecycleScope를 사용하여 ViewModel의 Flow 이벤트를 수집하는 데 사용됩니다.<br>
     */
    protected open fun eventVmCollect() {}

    override fun onDestroyView() {
        super.onDestroyView()
        binding.lifecycleOwner = null
        binding.unbind()
        _binding = null
    }

    /**
     * Obtains a ViewModel of the specified type using ViewModelProvider.<br><br>
     * ViewModelProvider를 사용하여 지정된 타입의 ViewModel을 가져옵니다.<br>
     *
     * @return The ViewModel instance of type T.<br><br>
     *         타입 T의 ViewModel 인스턴스.<br>
     */
    protected inline fun <reified T : ViewModel> DialogFragment.getViewModel(): T = ViewModelProvider(this)[T::class.java]

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        _binding?.root?.setBackgroundColor(color)
    }

    override fun setBackgroundDrawable(resId: Int) {
        super.setBackgroundDrawable(resId)
        _binding?.root?.setBackgroundResource(resId)
    }
}
