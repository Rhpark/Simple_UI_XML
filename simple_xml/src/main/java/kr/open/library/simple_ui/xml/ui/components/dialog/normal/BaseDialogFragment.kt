package kr.open.library.simple_ui.xml.ui.components.dialog.normal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.xml.ui.components.dialog.root.RootDialogFragment

/**
 * A basic DialogFragment that handles layout inflation automatically without ViewBinding or DataBinding.<br>
 * Extends RootDialogFragment to inherit dialog functionality and permission management.<br><br>
 * ViewBinding이나 DataBinding 없이 레이아웃 인플레이션을 자동으로 처리하는 기본 DialogFragment입니다.<br>
 * RootDialogFragment를 확장하여 다이얼로그 기능과 권한 관리를 상속받습니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android DialogFragments require manual layout inflation and view lifecycle management for every dialog.<br>
 * - This class eliminates boilerplate by accepting a layout resource ID and automatically handling inflation and cleanup.<br>
 * - Provides safe rootView access with null safety checks to prevent crashes after onDestroyView().<br>
 * - Ideal for simple dialogs that don't need ViewBinding/DataBinding overhead.<br><br>
 * - Android DialogFragment는 매번 수동으로 레이아웃 인플레이션과 뷰 생명주기 관리가 필요합니다.<br>
 * - 이 클래스는 레이아웃 리소스 ID를 받아 자동으로 인플레이션과 정리를 처리하여 보일러플레이트를 제거합니다.<br>
 * - onDestroyView() 이후 크래시를 방지하기 위해 null 안전성 검사가 포함된 안전한 rootView 접근을 제공합니다.<br>
 * - ViewBinding/DataBinding 오버헤드가 필요하지 않은 간단한 다이얼로그에 이상적입니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses constructor parameter for layout resource ID to enable simple single-line class declaration.<br>
 * - Uses nullable _rootView with public rootView accessor that throws exception after onDestroyView() for safe access.<br>
 * - Extends RootDialogFragment to inherit dialog functionality (animation, gravity, permissions).<br>
 * - Automatically sets rootView to null in onDestroyView() to prevent memory leaks.<br><br>
 * - 간단한 한 줄 클래스 선언을 위해 생성자 파라미터로 레이아웃 리소스 ID를 사용합니다.<br>
 * - onDestroyView() 이후 안전한 접근을 위해 nullable _rootView와 예외를 던지는 public rootView 접근자를 사용합니다.<br>
 * - RootDialogFragment를 상속하여 다이얼로그 기능(애니메이션, gravity, 권한)을 상속받습니다.<br>
 * - 메모리 누수를 방지하기 위해 onDestroyView()에서 자동으로 rootView를 null로 설정합니다.<br>
 *
 * **Important notes / 주의사항:**<br>
 * - Access rootView property only between onViewCreated() and onDestroyView() - accessing after onDestroyView() throws IllegalStateException.<br>
 * - Use findViewById() on rootView to access child views instead of ViewBinding.<br>
 * - For dialogs needing ViewBinding or DataBinding, use BaseViewBindingDialogFragment or BaseDataBindingDialogFragment instead.<br><br>
 * - rootView 프로퍼티는 onViewCreated()와 onDestroyView() 사이에서만 접근 - onDestroyView() 이후 접근 시 IllegalStateException 발생.<br>
 * - ViewBinding 대신 rootView에서 findViewById()를 사용하여 자식 뷰에 접근하세요.<br>
 * - ViewBinding이나 DataBinding이 필요한 다이얼로그는 BaseViewBindingDialogFragment 또는 BaseDataBindingDialogFragment를 사용하세요.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Extend this class with your DialogFragment and pass the layout resource ID.<br>
 * 2. Access views through findViewById() on rootView in onViewCreated() or later lifecycle methods.<br>
 * 3. No need to manually handle view inflation or cleanup - it's done automatically.<br><br>
 * 1. DialogFragment에서 이 클래스를 상속받고 레이아웃 리소스 ID를 전달하세요.<br>
 * 2. onViewCreated() 또는 이후 생명주기 메서드에서 rootView의 findViewById()를 통해 뷰에 접근하세요.<br>
 * 3. 뷰 인플레이션이나 정리를 수동으로 처리할 필요 없음 - 자동으로 수행됩니다.<br>
 *
 * **Usage example:**<br>
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
 * @see ParentBindingViewDialogFragment For the abstract parent class of all binding-enabled dialog fragments.<br><br>
 *      모든 바인딩 지원 DialogFragment의 추상 부모 클래스는 ParentBindingViewDialogFragment를 참조하세요.<br>
 *
 * @see BaseViewBindingDialogFragment For ViewBinding-enabled DialogFragment.<br><br>
 *      ViewBinding을 사용하는 DialogFragment는 BaseViewBindingDialogFragment를 참조하세요.<br>
 *
 * @see BaseDataBindingDialogFragment For DataBinding-enabled DialogFragment.<br><br>
 *      DataBinding을 사용하는 DialogFragment는 BaseDataBindingDialogFragment를 참조하세요.<br>
 */
public abstract class BaseDialogFragment(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false,
) : RootDialogFragment() {
    /**
     * Internal backing field for rootView.<br><br>
     * rootView의 내부 백킹 필드입니다.<br>
     */
    private var rootView: View? = null

    /**
     * The root view of the dialog's layout.<br>
     * Throws IllegalStateException if accessed after onDestroyView().<br><br>
     * 다이얼로그 레이아웃의 루트 뷰입니다.<br>
     * onDestroyView() 이후에 접근하면 IllegalStateException이 발생합니다.<br>
     *
     * **Usage / 사용법:**<br>
     * Access views using findViewById() in lifecycle methods between onViewCreated() and onDestroyView().<br><br>
     * onViewCreated()와 onDestroyView() 사이의 생명주기 메서드에서 findViewById()를 사용하여 뷰에 접근합니다.<br>
     *
     * **Example / 예시:**<br>
     * ```kotlin
     * override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
     *     super.onViewCreated(view, savedInstanceState)
     *     rootView.findViewById<TextView>(R.id.tvTitle).text = "Hello"
     * }
     * ```
     *
     * @return The root view of the dialog.<br><br>
     *         다이얼로그의 루트 뷰.<br>
     * @throws IllegalStateException if accessed after onDestroyView().<br><br>
     *                               onDestroyView() 이후에 접근하는 경우.<br>
     */
    protected fun getRootView(): View {
        if (rootView == null) {
            throw IllegalStateException("rootView is not initialized.")
        }
        return rootView!!
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(layoutRes, container, isAttachToParent)
        return getRootView()
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRootView().let { rootView ->
            config.updateBackgroundColor(rootView)
        }
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }
}
