package kr.open.library.simple_ui.xml.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * A basic Fragment that handles layout inflation automatically.<br>
 * Extends RootFragment to inherit permission management.<br><br>
 * 레이아웃 인플레이션을 자동으로 처리하는 기본 Fragment입니다.<br>
 * RootFragment를 확장하여 권한 관리를 상속받습니다.<br>
 *
 * Features:<br>
 * - Automatic layout inflation in onCreateView<br>
 * - Safe rootView access with null safety<br>
 * - Proper view cleanup in onDestroyView<br>
 * - All RootFragment features (permissions)<br><br>
 * 기능:<br>
 * - onCreateView에서 자동 레이아웃 인플레이션<br>
 * - null 안전성을 갖춘 안전한 rootView 접근<br>
 * - onDestroyView에서 적절한 뷰 정리<br>
 * - 모든 RootFragment 기능 (권한)<br>
 *
 * Usage example:<br>
 * ```kotlin
 * class HomeFragment : BaseFragment(R.layout.fragment_home) {
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *         // Access rootView or use findViewById
 *         rootView.findViewById<TextView>(R.id.tvTitle).text = "Home"
 *     }
 * }
 * ```
 * <br>
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
 * @see BaseBindingFragment For DataBinding-enabled Fragment.<br><br>
 *      DataBinding을 사용하는 Fragment는 BaseBindingFragment를 참조하세요.<br>
 */
abstract class BaseFragment(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false
) : RootFragment() {

    /**
     * Internal backing field for rootView.<br><br>
     * rootView의 내부 백킹 필드입니다.<br>
     */
    private var _rootView: View? = null

    /**
     * The root view of the fragment's layout.<br>
     * Throws IllegalStateException if accessed after onDestroyView().<br><br>
     * Fragment 레이아웃의 루트 뷰입니다.<br>
     * onDestroyView() 이후에 접근하면 IllegalStateException이 발생합니다.<br>
     */
    protected val rootView: View
        get() = _rootView
            ?: throw IllegalStateException("View accessed after onDestroyView()")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _rootView = inflater.inflate(layoutRes, container, isAttachToParent)
        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _rootView = null
    }
}
