package kr.open.library.simple_ui.xml.system_manager.controller.window.drag

import android.view.View
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.system_manager.controller.window.fixed.FloatingFixedView
import kr.open.library.simple_ui.xml.system_manager.controller.window.vo.FloatingViewCollisionsType
import kr.open.library.simple_ui.xml.system_manager.controller.window.vo.FloatingViewTouchType

/**
 * Draggable floating view that tracks collision states from touch events.<br><br>
 * 터치 이벤트에 따라 충돌 상태를 관리하는 드래그 플로팅 뷰입니다.<br>
 *
 * Key features:<br><br>
 * 주요 기능:<br>
 * - Reactive state management via `StateFlow`.<br>
 * - Touch phase-based collision detection.<br>
 * - Callback-based event handling.<br>
 * - Real-time collision state updates.<br>
 */
public open class FloatingDragView(
    view: View,
    startX: Int,
    startY: Int,
    /**
     * Collision callback invoked on touch down.<br><br>
     * 터치 다운 시 호출되는 충돌 콜백입니다.<br>
     */
    public var collisionsWhileTouchDown: ((FloatingDragView, FloatingViewCollisionsType) -> Unit)? = null,
    /**
     * Collision callback invoked while dragging.<br><br>
     * 드래그 중 호출되는 충돌 콜백입니다.<br>
     */
    public var collisionsWhileDrag: ((FloatingDragView, FloatingViewCollisionsType) -> Unit)? = null,
    /**
     * Collision callback invoked on touch up.<br><br>
     * 터치 업 시 호출되는 충돌 콜백입니다.<br>
     */
    public var collisionsWhileTouchUp: ((FloatingDragView, FloatingViewCollisionsType) -> Unit)? = null,
) : FloatingFixedView(view, startX, startY) {
    /**
     * Internal collision state flow (mutable).<br><br>
     * 내부 충돌 상태 플로우입니다(변경 가능).<br>
     */
    private val msfCollisionStateFlow =
        MutableStateFlow<Pair<FloatingViewTouchType, FloatingViewCollisionsType>>(
            FloatingViewTouchType.TOUCH_UP to FloatingViewCollisionsType.UNCOLLISIONS,
        )

    /**
     * External collision state flow (read-only).<br><br>
     * 외부에 노출되는 읽기 전용 충돌 상태 플로우입니다.<br>
     *
     * @return StateFlow containing the touch type and collision type pair.<br><br>
     *         터치 타입과 충돌 타입 쌍을 담은 `StateFlow`입니다.<br>
     */
    public val sfCollisionStateFlow: StateFlow<Pair<FloatingViewTouchType, FloatingViewCollisionsType>>
        get() = msfCollisionStateFlow

    /**
     * Updates collision state and invokes related callbacks.<br><br>
     * 충돌 상태를 갱신하고 관련 콜백을 호출합니다.<br>
     *
     * @param phase Touch phase.<br><br>
     *              터치 단계입니다.<br>
     * @param type Collision type.<br><br>
     *             충돌 타입입니다.<br>
     * @return true if the state was updated successfully.<br><br>
     *         상태 업데이트에 성공하면 true를 반환합니다.<br>
     */
    public fun updateCollisionState(
        phase: FloatingViewTouchType,
        type: FloatingViewCollisionsType,
    ): Boolean =
        safeCatch(false) {
            Logx.d("Collision State Updated: $phase -> $type")
            msfCollisionStateFlow.value = phase to type

            // 터치 단계별 콜백 호출 / Invoke callbacks based on touch phase
            when (phase) {
                FloatingViewTouchType.TOUCH_DOWN -> collisionsWhileTouchDown?.invoke(this, type)
                FloatingViewTouchType.TOUCH_MOVE -> collisionsWhileDrag?.invoke(this, type)
                FloatingViewTouchType.TOUCH_UP -> collisionsWhileTouchUp?.invoke(this, type)
            }
            return true
        }
}
