package kr.open.library.simple_ui.xml.ui.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * A basic DialogFragment that handles layout inflation automatically.<br>
 * Extends RootDialogFragment to inherit dialog functionality and permission management.<br><br>
 * 레이아웃 인플레이션을 자동으로 처리하는 기본 DialogFragment입니다.<br>
 * RootDialogFragment를 확장하여 다이얼로그 기능과 권한 관리를 상속받습니다.<br>
 *
 * Features:<br>
 * - Automatic layout inflation in onCreateView<br>
 * - Safe rootView access with null safety<br>
 * - Background color and drawable support<br>
 * - Proper view cleanup in onDestroyView<br>
 * - All RootDialogFragment features (animation, gravity, permissions)<br><br>
 * 기능:<br>
 * - onCreateView에서 자동 레이아웃 인플레이션<br>
 * - null 안전성을 갖춘 안전한 rootView 접근<br>
 * - 배경색 및 drawable 지원<br>
 * - onDestroyView에서 적절한 뷰 정리<br>
 * - 모든 RootDialogFragment 기능 (애니메이션, gravity, 권한)<br>
 *
 * Usage example:<br>
 * ```kotlin
 * class ConfirmDialog : BaseDialogFragment(R.layout.dialog_confirm) {
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *         rootView.findViewById<TextView>(R.id.tvTitle).text = "Confirm"
 *         rootView.findViewById<Button>(R.id.btnOk).setOnClickListener {
 *             safeDismiss()
 *         }
 *     }
 * }
 * ```
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
 * @see BaseBindingDialogFragment For DataBinding-enabled DialogFragment.<br><br>
 *      DataBinding을 사용하는 DialogFragment는 BaseBindingDialogFragment를 참조하세요.<br>
 */
public abstract class BaseDialogFragment(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false,
) : RootDialogFragment() {
    /**
     * Internal backing field for rootView.<br><br>
     * rootView의 내부 백킹 필드입니다.<br>
     */
    private var _rootView: View? = null

    /**
     * The root view of the dialog's layout.<br>
     * Throws IllegalStateException if accessed after onDestroyView().<br><br>
     * 다이얼로그 레이아웃의 루트 뷰입니다.<br>
     * onDestroyView() 이후에 접근하면 IllegalStateException이 발생합니다.<br>
     */
    public val rootView: View
        get() = _rootView
            ?: throw IllegalStateException("View accessed after onDestroyView()")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _rootView = inflater.inflate(layoutRes, container, isAttachToParent)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBackgroundColor()?.let { setBackgroundColor(it) }
        getBackgroundResId()?.let { setBackgroundDrawable(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _rootView = null
    }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        _rootView?.setBackgroundColor(color)
    }

    override fun setBackgroundDrawable(resId: Int) {
        super.setBackgroundDrawable(resId)
        _rootView?.setBackgroundResource(resId)
    }
}
