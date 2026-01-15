package kr.open.library.simple_ui.xml.ui.base.helper

import android.view.View
import androidx.viewbinding.ViewBinding

/**
 * Helper class to manage the lifecycle of ViewModel event collection for Layouts.
 * Prevents duplicate event collection by tracking the start state.
 *
 * Layout의 ViewModel 이벤트 수집 생명주기를 관리하는 헬퍼 클래스입니다.
 * 시작 상태를 추적하여 중복 이벤트 수집을 방지합니다.
 */
internal class ParentBindingHelperForLayout<BINDING : ViewBinding> : ParentBindingHelper() {
    /**
     * Holds the ViewBinding instance for this layout.<br><br>
     * 이 레이아웃의 ViewBinding 인스턴스를 보관합니다.<br>
     */
    private var binding: BINDING? = null

    /**
     * Returns the initialized binding instance.<br>
     * Call only after onAttachedToWindow() completes.<br><br>
     * 초기화된 바인딩 인스턴스를 반환합니다.<br>
     * onAttachedToWindow() 완료 이후에만 호출하세요.<br>
     *
     * @return The initialized ViewBinding instance. No logging is performed.<br><br>
     *         초기화된 ViewBinding 인스턴스를 반환합니다. 로깅은 수행하지 않습니다.<br>
     */
    public fun getBinding(view: View): BINDING {
        check(binding != null) {
            if (view.isAttachedToWindow) {
                "Binding is null while view is attached. This should not happen - please report this bug."
            } else {
                "Binding accessed while view is not attached to window. " +
                    "Call getBinding() only between onAttachedToWindow() and onDetachedFromWindow()."
            }
        }
        return binding!!
    }

    /**
     * EditMode handling is delegated to callers or subclasses; preview may throw exceptions.<br><br>
     * EditMode 처리는 호출자 또는 하위 클래스 판단에 맡기며 프리뷰 환경에서는 예외가 발생할 수 있습니다.<br>
     */
    public fun setBinding(provider: () -> BINDING, doWork: () -> Unit) {
        if (binding == null) {
            binding = provider()
            doWork.invoke()
        }
    }

    /**
     * Resets the event collection state.<br>
     * Call this when the view is destroyed to allow re-collection when recreated.<br><br>
     * 이벤트 수집 상태를 초기화합니다.<br>
     * 뷰가 파괴될 때 호출하여 재생성 시 다시 수집할 수 있도록 합니다.<br>
     */
    public fun reset(clearBinding: Boolean) {
        if (clearBinding) {
            binding = null
        }
        eventCollectStarted = false
    }
}
