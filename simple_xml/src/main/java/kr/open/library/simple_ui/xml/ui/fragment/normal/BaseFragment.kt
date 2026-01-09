package kr.open.library.simple_ui.xml.ui.fragment.normal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.xml.ui.fragment.root.RootFragment

/**
 * A basic Fragment that handles layout inflation automatically without ViewBinding or DataBinding.<br>
 * Extends RootFragment to inherit permission management.<br><br>
 * ViewBinding이나 DataBinding 없이 레이아웃 인플레이션을 자동으로 처리하는 기본 Fragment입니다.<br>
 * RootFragment를 확장하여 권한 관리를 상속받습니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android Fragments require manual layout inflation and view lifecycle management for every Fragment.<br>
 * - This class eliminates boilerplate by accepting a layout resource ID and automatically handling inflation and cleanup.<br>
 * - Provides safe rootView access with null safety checks to prevent crashes after onDestroyView().<br>
 * - Ideal for simple Fragments that don't need ViewBinding/DataBinding overhead.<br><br>
 * - Android Fragment는 매번 수동으로 레이아웃 인플레이션과 뷰 생명주기 관리가 필요합니다.<br>
 * - 이 클래스는 레이아웃 리소스 ID를 받아 자동으로 인플레이션과 정리를 처리하여 보일러플레이트를 제거합니다.<br>
 * - onDestroyView() 이후 크래시를 방지하기 위해 null 안전성 검사가 포함된 안전한 rootView 접근을 제공합니다.<br>
 * - ViewBinding/DataBinding 오버헤드가 필요하지 않은 간단한 Fragment에 이상적입니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses constructor parameter for layout resource ID to enable simple single-line class declaration.<br>
 * - Uses nullable _rootView with public rootView accessor that throws exception after onDestroyView() for safe access.<br>
 * - Extends RootFragment to inherit permission management features.<br>
 * - Automatically sets rootView to null in onDestroyView() to prevent memory leaks.<br><br>
 * - 간단한 한 줄 클래스 선언을 위해 생성자 파라미터로 레이아웃 리소스 ID를 사용합니다.<br>
 * - onDestroyView() 이후 안전한 접근을 위해 nullable _rootView와 예외를 던지는 public rootView 접근자를 사용합니다.<br>
 * - RootFragment를 상속하여 권한 관리 기능을 상속받습니다.<br>
 * - 메모리 누수를 방지하기 위해 onDestroyView()에서 자동으로 rootView를 null로 설정합니다.<br>
 *
 * **Important notes / 주의사항:**<br>
 * - Access rootView property only between onViewCreated() and onDestroyView() - accessing after onDestroyView() throws IllegalStateException.<br>
 * - Use findViewById() on rootView to access child views instead of ViewBinding.<br>
 * - For Fragments needing ViewBinding or DataBinding, use BaseViewBindingFragment or BaseDataBindingFragment instead.<br><br>
 * - rootView 프로퍼티는 onViewCreated()와 onDestroyView() 사이에서만 접근 - onDestroyView() 이후 접근 시 IllegalStateException 발생.<br>
 * - ViewBinding 대신 rootView에서 findViewById()를 사용하여 자식 뷰에 접근하세요.<br>
 * - ViewBinding이나 DataBinding이 필요한 Fragment는 BaseViewBindingFragment 또는 BaseDataBindingFragment를 사용하세요.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Extend this class with your Fragment and pass the layout resource ID.<br>
 * 2. Access views through findViewById() on rootView in onViewCreated() or later lifecycle methods.<br>
 * 3. No need to manually handle view inflation or cleanup - it's done automatically.<br><br>
 * 1. Fragment에서 이 클래스를 상속받고 레이아웃 리소스 ID를 전달하세요.<br>
 * 2. onViewCreated() 또는 이후 생명주기 메서드에서 rootView의 findViewById()를 통해 뷰에 접근하세요.<br>
 * 3. 뷰 인플레이션이나 정리를 수동으로 처리할 필요 없음 - 자동으로 수행됩니다.<br>
 *
 * **Usage example:**<br>
 * ```kotlin
 * class HomeFragment : BaseFragment(R.layout.fragment_home) {
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *         // Access views using findViewById on rootView
 *         rootView.findViewById<TextView>(R.id.tvTitle).text = "Home"
 *         rootView.findViewById<Button>(R.id.btnAction).setOnClickListener {
 *             // Handle click
 *         }
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
 * @see RootFragment For base class with permission features.<br><br>
 *      권한 기능이 있는 기본 클래스는 RootFragment를 참조하세요.<br>
 *
 * @see ParentsBindingFragment For the abstract parent class of all binding-enabled fragments.<br><br>
 *      모든 바인딩 지원 Fragment의 추상 부모 클래스는 ParentsBindingFragment를 참조하세요.<br>
 *
 * @see BaseViewBindingFragment For ViewBinding-enabled Fragment.<br><br>
 *      ViewBinding을 사용하는 Fragment는 BaseViewBindingFragment를 참조하세요.<br>
 *
 * @see BaseDataBindingFragment For DataBinding-enabled Fragment.<br><br>
 *      DataBinding을 사용하는 Fragment는 BaseDataBindingFragment를 참조하세요.<br>
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
     * @return The root view of the fragment.<br><br>
     *         Fragment의 루트 뷰.<br>
     * @throws IllegalStateException if accessed after onDestroyView().<br><br>
     *                               onDestroyView() 이후에 접근하는 경우.<br>
     */
    public val rootView: View
        get() = _rootView
            ?: throw IllegalStateException("View accessed after onDestroyView()")

    /**
     * Called to have the fragment instantiate its user interface view.<br>
     * Automatically inflates the layout using the provided layoutRes.<br><br>
     * Fragment가 사용자 인터페이스 뷰를 인스턴스화하기 위해 호출됩니다.<br>
     * 제공된 layoutRes를 사용하여 자동으로 레이아웃을 인플레이션합니다.<br>
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.<br><br>
     *                 Fragment의 뷰를 인플레이션하는 데 사용할 수 있는 LayoutInflater 객체.<br>
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.<br><br>
     *                  null이 아닌 경우, Fragment의 UI가 첨부될 부모 뷰.<br>
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.<br><br>
     *                           null이 아닌 경우, 이 Fragment는 이전에 저장된 상태에서 다시 구성되고 있습니다.<br>
     * @return Return the View for the fragment's UI, or null.<br><br>
     *         Fragment UI의 View를 반환하거나 null을 반환.<br>
     */
    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _rootView = inflater.inflate(layoutRes, container, isAttachToParent)
        return rootView
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.<br>
     * Automatically cleans up the rootView reference to prevent memory leaks.<br><br>
     * onCreateView에서 생성된 뷰가 Fragment에서 분리될 때 호출됩니다.<br>
     * 메모리 누수를 방지하기 위해 자동으로 rootView 참조를 정리합니다.<br>
     */
    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        _rootView = null
    }
}
