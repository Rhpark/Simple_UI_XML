package kr.open.library.simple_ui.xml.ui.layout.base.bind.coordinator

import android.view.View
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.base.helper.ParentBindingHelperForLayout

/**
 * Coordinates binding initialization and event collection for layouts.<br><br>
 * 레이아웃의 바인딩 초기화와 이벤트 수집을 조정합니다.<br>
 *
 * @param view The target view used for binding access checks.<br><br>
 *             바인딩 접근 검사에 사용하는 대상 View입니다.<br>
 * @param callbacks Callback interface that provides binding lifecycle hooks.<br><br>
 *                  바인딩 생명주기 훅을 제공하는 콜백 인터페이스입니다.<br>
 * @param helper Helper that guards event collection and binding reuse.<br><br>
 *               이벤트 수집과 바인딩 재사용을 보조하는 헬퍼입니다.<br>
 */
internal class LayoutBindingCoordinator<BINDING : ViewBinding>(
    private val view: View,
    private val callbacks: LayoutBindingCallbacks<BINDING>,
    private val helper: ParentBindingHelperForLayout<BINDING> = ParentBindingHelperForLayout(),
) {
    /**
     * Controls whether binding is cleared on detach; set to false only when reuse is safe.<br><br>
     * detach 시 바인딩 정리를 제어하며, false는 재사용이 안전한 경우에만 사용하세요.<br>
     */
    var clearBindingOnDetach: Boolean = true

    /**
     * Returns the initialized binding instance.<br>
     * Call only after onAttach() completes.<br><br>
     * 초기화된 바인딩 인스턴스를 반환합니다.<br>
     * onAttach() 완료 이후에만 호출하세요.<br>
     *
     * @return The initialized ViewBinding instance. No logging is performed.<br><br>
     *         초기화된 ViewBinding 인스턴스를 반환합니다. 로깅은 수행하지 않습니다.<br>
     */
    fun getBinding(): BINDING = helper.getBinding(view)

    /**
     * Initializes binding and starts event collection on attach.<br><br>
     * attach 시 바인딩을 초기화하고 이벤트 수집을 시작합니다.<br>
     */
    fun onAttach() {
        helper.setBinding(provider = callbacks::createBinding) {
            callbacks.onInitBind(helper.getBinding(view))
        }

        if (helper.canStartEventCollect()) {
            val binding = helper.getBinding(view)
            helper.startEventVmCollect { callbacks.onEventVmCollect(binding) }
        }
    }

    /**
     * Clears binding and resets event collection state on detach.<br><br>
     * detach 시 바인딩을 정리하고 이벤트 수집 상태를 초기화합니다.<br>
     */
    fun onDetach() {
        helper.reset(clearBindingOnDetach)
    }
}
