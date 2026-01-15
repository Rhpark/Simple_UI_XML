package kr.open.library.simple_ui.xml.ui.components.activity.binding

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

/**
 * A base Activity class for Activities that use ViewBinding.<br>
 * Extends ParentsBindingActivity to provide common binding functionality.<br><br>
 * ViewBinding을 사용하는 Activity를 위한 기본 Activity 클래스입니다.<br>
 * ParentsBindingActivity를 상속받아 공통 바인딩 기능을 제공합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android's ViewBinding requires manual inflate() calls and setContentView() setup for each Activity.<br>
 * - This class eliminates boilerplate by accepting an inflate function reference and automatically setting up the binding.<br>
 * - Provides type-safe view access without findViewById() or synthetic imports.<br><br>
 * - Android의 ViewBinding은 각 Activity마다 수동으로 inflate() 호출 및 setContentView() 설정이 필요합니다.<br>
 * - 이 클래스는 inflate 함수 참조를 받아 자동으로 바인딩을 설정하여 보일러플레이트를 제거합니다.<br>
 * - findViewById()나 synthetic import 없이 타입 안전한 뷰 접근을 제공합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses constructor parameter for inflate function reference to enable compile-time type safety.<br>
 * - Implements final createBinding() to prevent subclasses from breaking the binding initialization contract.<br>
 * - Automatically calls setContentView() in createBinding() to ensure views are ready in onCreate().<br><br>
 * - 컴파일 타임 타입 안전성을 위해 생성자 파라미터로 inflate 함수 참조를 사용합니다.<br>
 * - final createBinding()을 구현하여 하위 클래스가 바인딩 초기화 계약을 깨는 것을 방지합니다.<br>
 * - createBinding()에서 자동으로 setContentView()를 호출하여 onCreate()에서 뷰가 준비되도록 보장합니다.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Extend this class with your Activity and pass the ViewBinding inflate function reference.<br>
 * 2. Access views through the `getBinding()` method in onCreate() or later lifecycle methods.<br>
 * 3. Override onCreate(binding, savedInstanceState) to perform initial view setup after binding is ready.<br><br>
 * 1. Activity에서 이 클래스를 상속받고 ViewBinding inflate 함수 참조를 전달하세요.<br>
 * 2. onCreate() 또는 이후 생명주기 메서드에서 `getBinding()` 메서드를 통해 뷰에 접근하세요.<br>
 * 3. 바인딩이 준비된 후 초기 뷰 설정을 수행하려면 onCreate(binding, savedInstanceState)를 오버라이드하세요.<br>
 *
 * **Usage example:**<br>
 * ```kotlin
 * class MainActivity : BaseViewBindingActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         // Binding is already initialized and set as content view
 *         getBinding().textView.text = "Hello"
 *     }
 * }
 * ```
 *
 * @param BINDING The type of ViewBinding class.<br><br>
 *                ViewBinding 클래스의 타입.<br>
 * @param inflate The inflate function reference for the ViewBinding class (e.g., ActivityMainBinding::inflate).<br><br>
 *                ViewBinding 클래스의 inflate 함수 참조 (예: ActivityMainBinding::inflate).<br>
 *
 * @see ParentsBindingActivity For the parent class providing binding lifecycle.<br><br>
 *      바인딩 생명주기를 제공하는 부모 클래스는 ParentsBindingActivity를 참조하세요.<br>
 *
 * @see BaseDataBindingActivity For DataBinding-enabled Activity.<br><br>
 *      DataBinding을 사용하는 Activity는 BaseDataBindingActivity를 참조하세요.<br>
 */
public abstract class BaseViewBindingActivity<BINDING : ViewBinding>(
    private val inflate: (LayoutInflater) -> BINDING
) : ParentsBindingActivity<BINDING>() {
    /**
     * Creates the ViewBinding instance using the provided inflate function.<br>
     * Also sets the content view to the root of the binding.<br><br>
     *
     * 제공된 inflate 함수를 사용하여 ViewBinding 인스턴스를 생성합니다.<br>
     * 또한 바인딩의 루트를 콘텐츠 뷰로 설정합니다.<br>
     *
     * @return The initialized ViewBinding instance.<br><br>
     *         초기화된 ViewBinding 인스턴스.<br>
     */
    final override fun createBinding(): BINDING = inflate(layoutInflater).apply {
        setContentView(this.root)
    }
}
