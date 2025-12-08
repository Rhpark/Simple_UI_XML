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
    private val isAttachToParent: Boolean = false,
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
     *
     * @return The root view of the fragment.<br><br>
     *         Fragment의 루트 뷰.<br>
     * @throws IllegalStateException if accessed after onDestroyView().<br><br>
     *                               onDestroyView() 이후에 접근하는 경우.
     */
    public val rootView: View
        get() =
            _rootView
                ?: throw IllegalStateException("View accessed after onDestroyView()")

    /**
     * Called to have the fragment instantiate its user interface view.<br><br>
     * Fragment가 사용자 인터페이스 뷰를 인스턴스화하기 위해 호출됩니다.<br>
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.<br><br>
     *                 Fragment의 뷰를 인플레이션하는 데 사용할 수 있는 LayoutInflater 객체.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.<br><br>
     *                  null이 아닌 경우, Fragment의 UI가 첫부될 부모 뷰.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.<br><br>
     *                           null이 아닌 경우, 이 Fragment는 이전에 저장된 상태에서 다시 구성되고 있습니다.
     * @return Return the View for the fragment's UI, or null.<br><br>
     *         Fragment UI의 View를 반환하거나 null을 반환.<br>
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _rootView = inflater.inflate(layoutRes, container, isAttachToParent)
        return rootView
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     * Cleans up the rootView reference to prevent memory leaks.<br><br>
     * onCreateView에서 생성된 뷰가 Fragment에서 분리될 때 호출됩니다.<br>
     * 메모리 누수를 방지하기 위해 rootView 참조를 정리합니다.<br>
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _rootView = null
    }
}
