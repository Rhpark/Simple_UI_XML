package kr.open.library.simple_ui.xml.ui.layout.linear.root

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kr.open.library.simple_ui.xml.extensions.view.bindLifecycleObserver
import kr.open.library.simple_ui.xml.extensions.view.unbindLifecycleObserver
import kr.open.library.simple_ui.xml.ui.layout.base.lifecycle.LayoutLifecycleCallbacks
import kr.open.library.simple_ui.xml.ui.layout.base.lifecycle.LayoutLifecycleCoordinator

/**
 * LinearLayout that observes the host Activity or Fragment lifecycle.<br>
 * Automatically binds on attach and unbinds on detach.<br>
 * EditMode guard is not applied; preview handling is the caller's responsibility.<br><br>
 * 호스트 Activity 또는 Fragment의 생명주기를 관찰하는 LinearLayout입니다.<br>
 * attach 시 자동으로 바인딩하고 detach 시 자동으로 해제합니다.<br>
 * EditMode 가드는 적용하지 않으며, 프리뷰 처리는 호출자 책임입니다.<br>
 */
abstract class RootLinearLayout :
    LinearLayout,
    DefaultLifecycleObserver {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes)

    private val lifecycleCoordinator = LayoutLifecycleCoordinator(
        view = this,
        callbacks = object : LayoutLifecycleCallbacks {
            override fun bindLifecycle(): LifecycleOwner? = bindLifecycleObserver(this@RootLinearLayout)

            override fun unbindLifecycle() = unbindLifecycleObserver(this@RootLinearLayout)
        },
    )

    /**
     * Binds this view as a lifecycle observer when attached to window.<br><br>
     * 윈도우에 attach될 때 이 뷰를 라이프사이클 옵저버로 바인딩합니다.<br>
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleCoordinator.onAttach()
    }

    /**
     * Unbinds this view from lifecycle observation when detached from window.<br><br>
     * 윈도우에서 detach될 때 라이프사이클 옵저버 바인딩을 해제합니다.<br>
     */
    override fun onDetachedFromWindow() {
        lifecycleCoordinator.onDetach()
        super.onDetachedFromWindow()
    }

    /**
     * Lifecycle callback placeholder for subclasses.<br><br>
     * 하위 클래스에서 필요한 경우 오버라이드하는 생명주기 콜백입니다.<br>
     */
    override fun onCreate(owner: LifecycleOwner) {}

    /**
     * Lifecycle callback placeholder for subclasses.<br><br>
     * 하위 클래스에서 필요한 경우 오버라이드하는 생명주기 콜백입니다.<br>
     */
    override fun onStart(owner: LifecycleOwner) {}

    /**
     * Lifecycle callback placeholder for subclasses.<br><br>
     * 하위 클래스에서 필요한 경우 오버라이드하는 생명주기 콜백입니다.<br>
     */
    override fun onResume(owner: LifecycleOwner) {}

    /**
     * Lifecycle callback placeholder for subclasses.<br><br>
     * 하위 클래스에서 필요한 경우 오버라이드하는 생명주기 콜백입니다.<br>
     */
    override fun onPause(owner: LifecycleOwner) {}

    /**
     * Lifecycle callback placeholder for subclasses.<br><br>
     * 하위 클래스에서 필요한 경우 오버라이드하는 생명주기 콜백입니다.<br>
     */
    override fun onStop(owner: LifecycleOwner) {}

    /**
     * Lifecycle callback placeholder for subclasses.<br><br>
     * 하위 클래스에서 필요한 경우 오버라이드하는 생명주기 콜백입니다.<br>
     */
    override fun onDestroy(owner: LifecycleOwner) {}
}
