package kr.open.library.simple_ui.xml.ui.layout.relative.binding

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.ui.layout.base.bind.bindLifecycleOwnerOnce

/**
 * RelativeLayout that uses DataBinding and sets a LifecycleOwner.<br>
 * Binds lifecycle on attach and releases on detach.<br><br>
 * DataBinding을 사용하는 RelativeLayout입니다.<br>
 * attach 시 LifecycleOwner를 연결하고 detach 시 해제합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Provides lifecycle-aware DataBinding for custom RelativeLayout components.<br>
 * - Ideal for legacy layouts with LiveData-bound relative positioned views.<br>
 * - Automatically sets lifecycleOwner for LiveData observation in XML.<br><br>
 * - 커스텀 RelativeLayout 컴포넌트에 생명주기 인식 DataBinding을 제공합니다.<br>
 * - LiveData 바인딩된 상대 위치 뷰가 있는 레거시 레이아웃에 적합합니다.<br>
 * - XML에서 LiveData 관찰을 위해 자동으로 lifecycleOwner를 설정합니다.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Extend this class and pass the layout resource ID.<br>
 * 2. Override onInitBind() to bind ViewModel and perform initial setup.<br>
 * 3. Override onEventVmCollect() to collect ViewModel events with repeatOnLifecycle.<br>
 * 4. Use XML data binding expressions to bind views to ViewModel properties.<br><br>
 * 1. 이 클래스를 상속받고 레이아웃 리소스 ID를 전달하세요.<br>
 * 2. ViewModel을 바인딩하고 초기 설정을 수행하려면 onInitBind()를 오버라이드하세요.<br>
 * 3. repeatOnLifecycle로 ViewModel 이벤트를 수집하려면 onEventVmCollect()를 오버라이드하세요.<br>
 * 4. XML 데이터 바인딩 표현식을 사용하여 뷰를 ViewModel 프로퍼티에 바인딩하세요.<br>
 *
 * **Usage example:**<br>
 * ```kotlin
 * // 1. Define custom layout class
 * class PlayerControlView @JvmOverloads constructor(
 *     context: Context,
 *     attrs: AttributeSet? = null,
 * ) : BaseDataBindingRelativeLayout<LayoutPlayerControlBinding>(
 *     context, attrs,
 *     layoutId = R.layout.layout_player_control,
 * ) {
 *     private val viewModel: PlayerControlViewModel by lazy { /* get ViewModel */ }
 *
 *     override fun onInitBind(binding: LayoutPlayerControlBinding) {
 *         binding.viewModel = viewModel
 *         // LiveData properties like isPlaying, currentPosition will auto-update
 *     }
 * }
 *
 * // 2. XML layout (layout_player_control.xml)
 * // <layout>
 * //     <data>
 * //         <variable name="viewModel" type="PlayerControlViewModel" />
 * //     </data>
 * //     <RelativeLayout ...>
 * //         <ImageButton android:src="@{viewModel.isPlaying ? @drawable/pause : @drawable/play}"
 * //                      android:layout_centerInParent="true" />
 * //         <SeekBar android:progress="@{viewModel.currentPosition}"
 * //                  android:layout_alignParentBottom="true" />
 * //     </RelativeLayout>
 * // </layout>
 * ```
 * <br>
 *
 * @param BINDING The type of ViewDataBinding to be used.<br><br>
 *                사용할 ViewDataBinding 타입.<br>
 *
 * @see ParentsBindingRelativeLayout For the parent class with binding lifecycle management.<br><br>
 *      바인딩 생명주기 관리가 있는 부모 클래스는 ParentsBindingRelativeLayout을 참조하세요.<br>
 *
 * @see BaseViewBindingRelativeLayout For ViewBinding variant without LiveData auto-binding.<br><br>
 *      LiveData 자동 바인딩이 없는 ViewBinding 버전은 BaseViewBindingRelativeLayout을 참조하세요.<br>
 */
abstract class BaseDataBindingRelativeLayout<BINDING : ViewDataBinding> : ParentsBindingRelativeLayout<BINDING> {
    /**
     * Layout resource ID used for DataBinding inflation.<br><br>
     * DataBinding inflate에 사용할 레이아웃 리소스 ID입니다.<br>
     */
    private val layoutId: Int

    /**
     * Whether to attach the inflated binding to this parent.<br><br>
     * inflate된 바인딩을 이 부모에 attach할지 여부입니다.<br>
     */
    private val attachToParent: Boolean

    constructor(context: Context, layoutId: Int, attachToParent: Boolean = true) :
        super(context) {
        this.layoutId = layoutId
        this.attachToParent = attachToParent
    }

    constructor(context: Context, attrs: AttributeSet?, layoutId: Int, attachToParent: Boolean = true) :
        super(context, attrs) {
        this.layoutId = layoutId
        this.attachToParent = attachToParent
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        layoutId: Int,
        attachToParent: Boolean = true,
    ) : super(context, attrs, defStyleAttr) {
        this.layoutId = layoutId
        this.attachToParent = attachToParent
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
        layoutId: Int,
        attachToParent: Boolean = true,
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        this.layoutId = layoutId
        this.attachToParent = attachToParent
    }

    /**
     * Creates the DataBinding instance for this layout.<br><br>
     * 이 레이아웃의 DataBinding 인스턴스를 생성합니다.<br>
     *
     * @return The initialized DataBinding instance. No logging is performed.<br><br>
     *         초기화된 DataBinding 인스턴스를 반환합니다. 로깅은 수행하지 않습니다.<br>
     */
    override fun createBinding(): BINDING =
        DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, this, attachToParent)

    /**
     * Sets the LifecycleOwner when attached to the window.<br><br>
     * 윈도우에 attach될 때 LifecycleOwner를 설정합니다.<br>
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bindLifecycleOwnerOnce(getBinding())
    }

    override fun onDetachedFromWindow() {
        // Accesses binding before parent clears it; keep super call at the end.<br><br>
        // 부모가 바인딩을 정리하기 전에 접근하므로 super 호출은 마지막에 유지해야 합니다.<br>
        if (clearBindingOnDetach) {
            getBinding().unbind()
        }
        getBinding().lifecycleOwner = null
        super.onDetachedFromWindow()
    }
}
