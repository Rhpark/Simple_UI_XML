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
 * FloatingDragView - 드래그 가능한 플로팅 뷰 클래스
 * Draggable Floating View Class
 * 
 * 드래그 가능한 플로팅 뷰로, 터치 이벤트에 따른 충돌 상태를 실시간으로 관리합니다.
 * A draggable floating view that manages collision states in real-time based on touch events.
 * 
 * 주요 기능 / Main Features:
 * - StateFlow를 통한 반응형 상태 관리 / Reactive state management via StateFlow
 * - 터치 단계별 충돌 감지 / Touch phase-based collision detection
 * - 콜백 기반 이벤트 처리 / Callback-based event handling
 * - 실시간 충돌 상태 업데이트 / Real-time collision state updates
 */
public open class FloatingDragView(
    view: View,
    startX: Int,
    startY: Int,
    /**
     * 터치 다운 시 충돌 콜백
     * Collision callback on touch down
     */
    public var collisionsWhileTouchDown: ((FloatingDragView, FloatingViewCollisionsType) -> Unit)? = null,
    /**
     * 드래그 중 충돌 콜백
     * Collision callback during drag
     */
    public var collisionsWhileDrag: ((FloatingDragView, FloatingViewCollisionsType) -> Unit)? = null,
    /**
     * 터치 업 시 충돌 콜백
     * Collision callback on touch up
     */
    public var collisionsWhileTouchUp: ((FloatingDragView, FloatingViewCollisionsType) -> Unit)? = null
) : FloatingFixedView(view, startX, startY) {

    /**
     * 내부 충돌 상태 플로우 (변경 가능)
     * Internal collision state flow (mutable)
     */
    private val msfCollisionStateFlow = MutableStateFlow<Pair<FloatingViewTouchType, FloatingViewCollisionsType>>(
        FloatingViewTouchType.TOUCH_UP to FloatingViewCollisionsType.UNCOLLISIONS
    )
    
    /**
     * 외부 노출용 충돌 상태 플로우 (읽기 전용)
     * External collision state flow (read-only)
     * 
     * @return 터치 타입과 충돌 타입의 쌍을 담은 StateFlow / StateFlow containing touch type and collision type pair
     */
    public val sfCollisionStateFlow: StateFlow<Pair<FloatingViewTouchType, FloatingViewCollisionsType>>
        get() = msfCollisionStateFlow

    /**
     * 충돌 상태를 업데이트하고 관련 콜백을 호출합니다.
     * Updates collision state and invokes related callbacks.
     * 
     * @param phase 터치 단계 / Touch phase
     * @param type 충돌 타입 / Collision type
     * @return 업데이트 성공 여부 / Update success status
     */
    public fun updateCollisionState(phase: FloatingViewTouchType, type: FloatingViewCollisionsType): Boolean =
        safeCatch(defaultValue = false) {
            Logx.d("Collision State Updated: $phase -> $type")
            msfCollisionStateFlow.value = phase to type

            // 터치 단계별 콜백 호출 / Invoke callbacks based on touch phase
            when (phase) {
                FloatingViewTouchType.TOUCH_DOWN -> collisionsWhileTouchDown?.invoke(this, type)
                FloatingViewTouchType.TOUCH_MOVE -> collisionsWhileDrag?.invoke(this, type)
                FloatingViewTouchType.TOUCH_UP -> collisionsWhileTouchUp?.invoke(this, type)
            }
            true
        }

}