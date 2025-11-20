package kr.open.library.simple_ui.presenter.ui.layout

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.lifecycle.DefaultLifecycleObserver
import kr.open.library.simple_ui.presenter.extensions.view.bindLifecycleObserver
import kr.open.library.simple_ui.presenter.extensions.view.unbindLifecycleObserver

/**
 * A RelativeLayout that automatically observes the lifecycle of its hosting Activity or Fragment.<br>
 * Implements DefaultLifecycleObserver to receive lifecycle callbacks.<br><br>
 * 호스팅 Activity 또는 Fragment의 생명주기를 자동으로 관찰하는 RelativeLayout입니다.<br>
 * DefaultLifecycleObserver를 구현하여 생명주기 콜백을 수신합니다.<br>
 *
 * Features:<br>
 * - Automatic lifecycle binding when attached to window<br>
 * - Automatic lifecycle unbinding when detached from window<br>
 * - Access to lifecycle events (onCreate, onStart, onResume, onPause, onStop, onDestroy)<br><br>
 * 기능:<br>
 * - 윈도우에 연결될 때 자동 생명주기 바인딩<br>
 * - 윈도우에서 분리될 때 자동 생명주기 언바인딩<br>
 * - 생명주기 이벤트에 대한 접근 (onCreate, onStart, onResume, onPause, onStop, onDestroy)<br>
 *
 * Usage example:<br>
 * ```kotlin
 * class CustomRelativeLayout(context: Context) : BaseLifeCycleRelativeLayout(context) {
 *     override fun onResume(owner: LifecycleOwner) {
 *         // Called when the hosting Activity/Fragment is resumed
 *     }
 *
 *     override fun onPause(owner: LifecycleOwner) {
 *         // Called when the hosting Activity/Fragment is paused
 *     }
 * }
 * ```
 * <br>
 *
 * @see BaseLifeCycleFrameLayout For FrameLayout with lifecycle support.<br><br>
 *      생명주기 지원이 있는 FrameLayout은 BaseLifeCycleFrameLayout을 참조하세요.<br>
 *
 * @see BaseLifeCycleLinearLayout For LinearLayout with lifecycle support.<br><br>
 *      생명주기 지원이 있는 LinearLayout은 BaseLifeCycleLinearLayout을 참조하세요.<br>
 *
 * @see BaseLifeCycleConstraintLayout For ConstraintLayout with lifecycle support.<br><br>
 *      생명주기 지원이 있는 ConstraintLayout은 BaseLifeCycleConstraintLayout을 참조하세요.<br>
 */
class BaseLifeCycleRelativeLayout : RelativeLayout, DefaultLifecycleObserver {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    /**
     * Called when the view is attached to a window.<br>
     * Binds this view as a lifecycle observer to the hosting LifecycleOwner.<br><br>
     * 뷰가 윈도우에 연결될 때 호출됩니다.<br>
     * 이 뷰를 호스팅 LifecycleOwner의 생명주기 관찰자로 바인딩합니다.<br>
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bindLifecycleObserver(this)
    }

    /**
     * Called when the view is detached from a window.<br>
     * Unbinds this view from the lifecycle observer.<br><br>
     * 뷰가 윈도우에서 분리될 때 호출됩니다.<br>
     * 이 뷰를 생명주기 관찰자에서 언바인딩합니다.<br>
     */
    override fun onDetachedFromWindow() {
        unbindLifecycleObserver(this)
        super.onDetachedFromWindow()
    }
}