package kr.open.library.simple_ui.xml.ui.layout.frame.binding

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * FrameLayout that uses ViewBinding with a provided inflate function.<br><br>
 * 제공된 inflate 함수로 ViewBinding을 사용하는 FrameLayout입니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Provides lifecycle-aware ViewBinding for custom FrameLayout components.<br>
 * - Ideal for container views, overlay views, or single-child wrapper layouts.<br>
 * - Automatically initializes binding on attach and cleans up on detach.<br><br>
 * - 커스텀 FrameLayout 컴포넌트에 생명주기 인식 ViewBinding을 제공합니다.<br>
 * - 컨테이너 뷰, 오버레이 뷰, 또는 단일 자식 래퍼 레이아웃에 적합합니다.<br>
 * - attach 시 자동으로 바인딩을 초기화하고 detach 시 정리합니다.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Extend this class and pass the ViewBinding inflate function reference.<br>
 * 2. Override onInitBind() to perform initial view setup after binding is ready.<br>
 * 3. Override onEventVmCollect() to collect ViewModel events with repeatOnLifecycle.<br>
 * 4. Use the custom layout in XML or create programmatically.<br><br>
 * 1. 이 클래스를 상속받고 ViewBinding inflate 함수 참조를 전달하세요.<br>
 * 2. 바인딩이 준비된 후 초기 뷰 설정을 수행하려면 onInitBind()를 오버라이드하세요.<br>
 * 3. repeatOnLifecycle로 ViewModel 이벤트를 수집하려면 onEventVmCollect()를 오버라이드하세요.<br>
 * 4. XML에서 커스텀 레이아웃을 사용하거나 프로그래밍 방식으로 생성하세요.<br>
 *
 * **Usage example:**<br>
 * ```kotlin
 * // 1. Define custom layout class
 * class LoadingOverlayView @JvmOverloads constructor(
 *     context: Context,
 *     attrs: AttributeSet? = null,
 * ) : BaseViewBindingFrameLayout<LayoutLoadingOverlayBinding>(
 *     context, attrs,
 *     inflate = LayoutLoadingOverlayBinding::inflate,
 * ) {
 *     override fun onInitBind(binding: LayoutLoadingOverlayBinding) {
 *         binding.progressBar.isIndeterminate = true
 *         binding.tvMessage.text = "Loading..."
 *     }
 *
 *     fun setMessage(message: String) {
 *         getBinding().tvMessage.text = message
 *     }
 *
 *     fun show() { visibility = VISIBLE }
 *     fun hide() { visibility = GONE }
 * }
 *
 * // 2. Use in XML
 * // <com.example.LoadingOverlayView
 * //     android:id="@+id/loadingOverlay"
 * //     android:layout_width="match_parent"
 * //     android:layout_height="match_parent"
 * //     android:visibility="gone" />
 * ```
 * <br>
 *
 * @param BINDING The type of ViewBinding to be used.<br><br>
 *                사용할 ViewBinding 타입.<br>
 *
 * @see ParentsBindingFrameLayout For the parent class with binding lifecycle management.<br><br>
 *      바인딩 생명주기 관리가 있는 부모 클래스는 ParentsBindingFrameLayout을 참조하세요.<br>
 *
 * @see BaseDataBindingFrameLayout For DataBinding variant with LiveData support.<br><br>
 *      LiveData 지원이 있는 DataBinding 버전은 BaseDataBindingFrameLayout을 참조하세요.<br>
 */
abstract class BaseViewBindingFrameLayout<BINDING : ViewBinding> : ParentsBindingFrameLayout<BINDING> {
    /**
     * Inflate function used to create the binding instance.<br><br>
     * 바인딩 인스턴스를 생성하는 inflate 함수입니다.<br>
     */
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING

    /**
     * Whether the inflated binding should be attached to this parent.<br><br>
     * inflate된 바인딩을 이 부모에 attach할지 여부입니다.<br>
     */
    private val attachToParent: Boolean

    /**
     * Creates the layout with ViewBinding inflation.<br><br>
     * ViewBinding inflate를 사용하는 레이아웃을 생성합니다.<br>
     *
     * @param context The context for this view.<br><br>
     *                이 뷰의 컨텍스트.<br>
     * @param inflate The function to inflate the binding.<br><br>
     *                바인딩을 inflate하는 함수.<br>
     * @param attachToParent Whether to attach to the parent.<br><br>
     *                       부모에 attach할지 여부.<br>
     */
    constructor(
        context: Context,
        inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
        attachToParent: Boolean = true,
    ) : super(context) {
        this.inflate = inflate
        this.attachToParent = attachToParent
    }

    /**
     * Creates the layout with ViewBinding inflation and attributes.<br><br>
     * 속성을 포함하여 ViewBinding inflate를 사용하는 레이아웃을 생성합니다.<br>
     *
     * @param context The context for this view.<br><br>
     *                이 뷰의 컨텍스트.<br>
     * @param attrs AttributeSet from XML.<br><br>
     *              XML에서 전달된 AttributeSet.<br>
     * @param inflate The function to inflate the binding.<br><br>
     *                바인딩을 inflate하는 함수.<br>
     * @param attachToParent Whether to attach to the parent.<br><br>
     *                       부모에 attach할지 여부.<br>
     */
    constructor(
        context: Context,
        attrs: AttributeSet?,
        inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
        attachToParent: Boolean = true,
    ) : super(context, attrs) {
        this.inflate = inflate
        this.attachToParent = attachToParent
    }

    /**
     * Creates the layout with ViewBinding inflation and style attribute.<br><br>
     * 스타일 속성을 포함하여 ViewBinding inflate를 사용하는 레이아웃을 생성합니다.<br>
     *
     * @param context The context for this view.<br><br>
     *                이 뷰의 컨텍스트.<br>
     * @param attrs AttributeSet from XML.<br><br>
     *              XML에서 전달된 AttributeSet.<br>
     * @param defStyleAttr Default style attribute.<br><br>
     *                     기본 스타일 속성.<br>
     * @param inflate The function to inflate the binding.<br><br>
     *                바인딩을 inflate하는 함수.<br>
     * @param attachToParent Whether to attach to the parent.<br><br>
     *                       부모에 attach할지 여부.<br>
     */
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
        attachToParent: Boolean = true,
    ) : super(context, attrs, defStyleAttr) {
        this.inflate = inflate
        this.attachToParent = attachToParent
    }

    /**
     * Creates the layout with ViewBinding inflation and style attributes.<br><br>
     * 스타일 속성을 포함하여 ViewBinding inflate를 사용하는 레이아웃을 생성합니다.<br>
     *
     * @param context The context for this view.<br><br>
     *                이 뷰의 컨텍스트.<br>
     * @param attrs AttributeSet from XML.<br><br>
     *              XML에서 전달된 AttributeSet.<br>
     * @param defStyleAttr Default style attribute.<br><br>
     *                     기본 스타일 속성.<br>
     * @param defStyleRes Default style resource.<br><br>
     *                    기본 스타일 리소스.<br>
     * @param inflate The function to inflate the binding.<br><br>
     *                바인딩을 inflate하는 함수.<br>
     * @param attachToParent Whether to attach to the parent.<br><br>
     *                       부모에 attach할지 여부.<br>
     */
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
        inflate: (LayoutInflater, ViewGroup, Boolean) -> BINDING,
        attachToParent: Boolean = true,
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        this.inflate = inflate
        this.attachToParent = attachToParent
    }

    /**
     * Creates the ViewBinding instance using the provided inflate function.<br><br>
     * 제공된 inflate 함수로 ViewBinding 인스턴스를 생성합니다.<br>
     *
     * @return The initialized ViewBinding instance. No logging is performed.<br><br>
     *         초기화된 ViewBinding 인스턴스를 반환합니다. 로깅은 수행하지 않습니다.<br>
     */
    override fun createBinding(): BINDING =
        inflate(LayoutInflater.from(context), this, attachToParent)
}
