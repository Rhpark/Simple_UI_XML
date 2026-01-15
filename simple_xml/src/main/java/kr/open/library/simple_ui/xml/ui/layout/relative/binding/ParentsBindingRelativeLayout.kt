package kr.open.library.simple_ui.xml.ui.layout.relative.binding

import android.content.Context
import android.util.AttributeSet
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.base.lifecycle.ParentBindingInterfaceForLayout
import kr.open.library.simple_ui.xml.ui.layout.base.bind.coordinator.LayoutBindingCallbacks
import kr.open.library.simple_ui.xml.ui.layout.base.bind.coordinator.LayoutBindingCoordinator
import kr.open.library.simple_ui.xml.ui.layout.relative.root.RootRelativeLayout

/**
 * Abstract base class for RelativeLayouts using ViewBinding or DataBinding.<br>
 * Initializes binding on attach and starts event collection once, then clears on detach.<br><br>
 * ViewBinding 또는 DataBinding을 사용하는 RelativeLayout의 추상 기본 클래스입니다.<br>
 * attach 시 바인딩을 초기화하고 이벤트 수집을 1회 시작한 뒤, detach 시 정리합니다.<br>
 */
abstract class ParentsBindingRelativeLayout<BINDING : ViewBinding> :
    RootRelativeLayout,
    ParentBindingInterfaceForLayout<BINDING> {
    private val bindingCallbacks = object : LayoutBindingCallbacks<BINDING> {
        override fun createBinding(): BINDING = this@ParentsBindingRelativeLayout.createBinding()

        override fun onInitBind(binding: BINDING) = this@ParentsBindingRelativeLayout.onInitBind(binding)

        override fun onEventVmCollect(binding: BINDING) = this@ParentsBindingRelativeLayout.onEventVmCollect(binding)
    }

    /**
     * Coordinator that manages binding lifecycle and event collection.<br><br>
     * 바인딩 생명주기와 이벤트 수집을 관리하는 코디네이터입니다.<br>
     */
    private val bindingCoordinator = LayoutBindingCoordinator(
        view = this,
        callbacks = bindingCallbacks,
    )

    /**
     * Controls whether binding is cleared on detach; set to false only when reuse is safe.<br>
     * If true, binding is cleared on detach and onInitBind() runs again on next attach.<br>
     * If false, binding is retained and onInitBind() is not called again on reattach.<br><br>
     * detach 시 바인딩 정리를 제어하며, false는 재사용이 안전한 경우에만 사용하세요.<br>
     * true면 detach 시 바인딩을 정리하고 다음 attach 때 onInitBind()가 다시 호출됩니다.<br>
     * false면 바인딩을 유지하므로 재부착 시 onInitBind()가 다시 호출되지 않습니다.<br>
     */
    protected open val clearBindingOnDetach: Boolean = true

    /**
     * Returns the initialized binding instance.<br>
     * Call only after onAttachedToWindow() completes.<br><br>
     * 초기화된 바인딩 인스턴스를 반환합니다.<br>
     * onAttachedToWindow() 완료 이후에만 호출하세요.<br>
     *
     * @return The initialized ViewBinding instance. No logging is performed.<br><br>
     *         초기화된 ViewBinding 인스턴스를 반환합니다. 로깅은 수행하지 않습니다.<br>
     */
    protected fun getBinding(): BINDING = bindingCoordinator.getBinding()

    /**
     * Creates the ViewBinding instance for this layout.<br><br>
     * 이 레이아웃의 ViewBinding 인스턴스를 생성합니다.<br>
     *
     * @return The initialized ViewBinding instance. No logging is performed.<br><br>
     *         초기화된 ViewBinding 인스턴스를 반환합니다. 로깅은 수행하지 않습니다.<br>
     */
    protected abstract fun createBinding(): BINDING

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes)

    /**
     * Initializes binding and starts event collection when attached to window.<br><br>
     * 윈도우에 attach될 때 바인딩을 초기화하고 이벤트 수집을 시작합니다.<br>
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bindingCoordinator.clearBindingOnDetach = clearBindingOnDetach
        bindingCoordinator.onAttach()
    }

    /**
     * Clears binding and resets event collection state when detached from window.<br><br>
     * 윈도우에서 detach될 때 바인딩을 정리하고 이벤트 수집 상태를 초기화합니다.<br>
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bindingCoordinator.clearBindingOnDetach = clearBindingOnDetach
        bindingCoordinator.onDetach()
    }
}
