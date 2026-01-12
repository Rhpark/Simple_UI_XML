package kr.open.library.simple_ui.xml.ui.components.fragment.normal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.xml.ui.components.fragment.root.RootFragment

/**
 * A basic Fragment that automatically handles layout inflation without ViewBinding or DataBinding.<br>
 * Directly extends RootFragment to inherit only permission management features.<br><br>
 * ViewBinding이나 DataBinding 없이 레이아웃 인플레이션을 자동으로 처리하는 기본 Fragment입니다.<br>
 * 권한 관리 기능만 상속받기 위해 RootFragment를 직접 확장합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Directly extends RootFragment (not ParentsBindingFragment) to avoid unnecessary binding infrastructure.<br>
 * - Uses constructor parameter for layout resource ID to enable simple single-line class declaration.<br>
 * - Automatically sets rootView to null in onDestroyView() to prevent memory leaks.<br><br>
 * - 불필요한 바인딩 인프라를 피하기 위해 RootFragment를 직접 상속합니다 (ParentsBindingFragment 상속 안 함).<br>
 * - 간단한 한 줄 클래스 선언을 위해 생성자 파라미터로 레이아웃 리소스 ID를 사용합니다.<br>
 * - 메모리 누수를 방지하기 위해 onDestroyView()에서 자동으로 rootView를 null로 설정합니다.<br>
 *
 * **Usage example:**<br>
 * ```kotlin
 * class HomeFragment : BaseFragment(R.layout.fragment_home) {
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *         getRootView().findViewById<TextView>(R.id.tvTitle).text = "Home"
 *     }
 * }
 * ```
 *
 * @param layoutRes The layout resource ID for the fragment.<br><br>
 *                  Fragment의 레이아웃 리소스 ID.<br>
 *
 * @param isAttachToParent Whether to attach the inflated view to the parent container.<br><br>
 *                         인플레이션된 뷰를 부모 컨테이너에 첨부할지 여부.<br>
 *
 * @see RootFragment For the parent class with permission management.<br><br>
 *      권한 관리 기능이 있는 부모 클래스.<br>
 *
 * @see BaseViewBindingFragment For ViewBinding-enabled Fragment.<br><br>
 *      ViewBinding을 사용하는 Fragment.<br>
 *
 * @see BaseDataBindingFragment For DataBinding-enabled Fragment.<br><br>
 *      DataBinding을 사용하는 Fragment.<br>
 */
abstract class BaseFragment(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false,
) : RootFragment() {
    private var rootView: View? = null

    /**
     * Returns the root view of the fragment's layout.<br><br>
     * Fragment 레이아웃의 루트 뷰를 반환합니다.<br>
     *
     * @return The root view of the fragment.<br><br>
     *         Fragment의 루트 뷰.<br>
     * @throws IllegalStateException if accessed before onCreateView() or after onDestroyView().<br><br>
     *                               onCreateView() 이전이나 onDestroyView() 이후에 접근하는 경우.<br>
     */
    protected fun getRootView(): View {
        check(rootView != null) {
            "rootView can only be accessed between onCreateView() and onDestroyView()."
        }
        return rootView!!
    }

    /**
     * Inflates the layout and returns the root view.<br><br>
     * 레이아웃을 인플레이션하고 루트 뷰를 반환합니다.<br>
     */
    @CallSuper
    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(layoutRes, container, isAttachToParent)
        onCreateView(getRootView(), savedInstanceState)
        return getRootView()
    }

    protected abstract fun onCreateView(rootView: View, savedInstanceState: Bundle?)

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }
}
