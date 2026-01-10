package kr.open.library.simple_ui.xml.ui.components.fragment.binding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * A base Fragment class for Fragments that use ViewBinding.<br>
 * Extends ParentsBindingFragment to provide common binding functionality.<br><br>
 * ViewBinding을 사용하는 Fragment를 위한 기본 Fragment 클래스입니다.<br>
 * ParentsBindingFragment를 상속받아 공통 바인딩 기능을 제공합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android's ViewBinding requires manual inflate() calls for each Fragment.<br>
 * - This class eliminates boilerplate by accepting an inflate function reference and automatically setting up the binding.<br>
 * - Provides type-safe view access without findViewById() or synthetic imports.<br><br>
 * - Android의 ViewBinding은 각 Fragment마다 수동으로 inflate() 호출이 필요합니다.<br>
 * - 이 클래스는 inflate 함수 참조를 받아 자동으로 바인딩을 설정하여 보일러플레이트를 제거합니다.<br>
 * - findViewById()나 synthetic import 없이 타입 안전한 뷰 접근을 제공합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses constructor parameter for inflate function reference to enable compile-time type safety.<br>
 * - Implements final createBinding() to prevent subclasses from breaking the binding initialization contract.<br>
 * - Properly handles isAttachToParent parameter for flexible view inflation.<br><br>
 * - 컴파일 타임 타입 안전성을 위해 생성자 파라미터로 inflate 함수 참조를 사용합니다.<br>
 * - final createBinding()을 구현하여 하위 클래스가 바인딩 초기화 계약을 깨는 것을 방지합니다.<br>
 * - 유연한 뷰 인플레이션을 위해 isAttachToParent 파라미터를 적절히 처리합니다.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Extend this class with your Fragment and pass the ViewBinding inflate function reference.<br>
 * 2. Access views through the `binding` property in onViewCreated() or later lifecycle methods.<br>
 * 3. Override onInitBind() to perform initial view setup after binding is ready.<br><br>
 * 1. Fragment에서 이 클래스를 상속받고 ViewBinding inflate 함수 참조를 전달하세요.<br>
 * 2. onViewCreated() 또는 이후 생명주기 메서드에서 `binding` 프로퍼티를 통해 뷰에 접근하세요.<br>
 * 3. 바인딩이 준비된 후 초기 뷰 설정을 수행하려면 onInitBind()를 오버라이드하세요.<br>
 *
 * **Usage example:**<br>
 * ```kotlin
 * class HomeFragment : BaseViewBindingFragment<FragmentHomeBinding>(
 *     FragmentHomeBinding::inflate
 * ) {
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *         // Binding is already initialized and views are ready
 *         binding.textView.text = "Hello"
 *     }
 * }
 * ```
 *
 * @param BINDING The type of ViewBinding class.<br><br>
 *                ViewBinding 클래스의 타입.<br>
 * @param inflate The inflate function reference for the ViewBinding class (e.g., FragmentHomeBinding::inflate).<br><br>
 *                ViewBinding 클래스의 inflate 함수 참조 (예: FragmentHomeBinding::inflate).<br>
 * @param isAttachToParent Whether to attach the inflated view to the parent container.<br><br>
 *                         인플레이션된 뷰를 부모 컨테이너에 첨부할지 여부.<br>
 *
 * @see ParentsBindingFragment For the parent class providing binding lifecycle.<br><br>
 *      바인딩 생명주기를 제공하는 부모 클래스는 ParentsBindingFragment를 참조하세요.<br>
 *
 * @see BaseDataBindingFragment For DataBinding-enabled Fragment.<br><br>
 *      DataBinding을 사용하는 Fragment는 BaseDataBindingFragment를 참조하세요.<br>
 */
public abstract class BaseViewBindingFragment<BINDING : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> BINDING,
    isAttachToParent: Boolean = false
) : ParentsBindingFragment<BINDING>(isAttachToParent) {
    /**
     * Creates the ViewBinding instance using the provided inflate function.<br><br>
     * 제공된 inflate 함수를 사용하여 ViewBinding 인스턴스를 생성합니다.<br>
     *
     * @param inflater The LayoutInflater object to inflate views.<br><br>
     *                 뷰를 인플레이션할 LayoutInflater 객체.<br>
     * @param container The parent view container.<br><br>
     *                  부모 뷰 컨테이너.<br>
     * @param isAttachToParent Whether to attach to parent.<br><br>
     *                         부모에 첨부할지 여부.<br>
     * @return The initialized ViewBinding instance.<br><br>
     *         초기화된 ViewBinding 인스턴스.<br>
     */
    final override fun createBinding(inflater: LayoutInflater, container: ViewGroup?, isAttachToParent: Boolean): BINDING =
        inflate.invoke(inflater, container, isAttachToParent)
}
