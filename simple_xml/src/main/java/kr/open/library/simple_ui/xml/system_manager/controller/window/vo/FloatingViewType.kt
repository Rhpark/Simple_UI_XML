package kr.open.library.simple_ui.xml.system_manager.controller.window.vo

/**
 * Collision state between floating views.<br><br>
 * 플로팅 뷰 간 충돌 상태를 나타냅니다.<br>
 */
public enum class FloatingViewCollisionsType {
    /**
     * Collision occurring.<br><br>
     * 충돌이 발생한 상태입니다.<br>
     */
    OCCURING,

    /**
     * No collision.<br><br>
     * 충돌이 없는 상태입니다.<br>
     */
    UNCOLLISIONS,
}

/**
 * Touch event phases for floating views.<br><br>
 * 플로팅 뷰의 터치 이벤트 단계를 나타냅니다.<br>
 */
public enum class FloatingViewTouchType {
    /**
     * Touch start (finger touches screen).<br><br>
     * 터치 시작 상태입니다.<br>
     */
    TOUCH_DOWN,

    /**
     * Touch move (dragging).<br><br>
     * 터치 이동(드래그) 상태입니다.<br>
     */
    TOUCH_MOVE,

    /**
     * Touch end (finger leaves screen).<br><br>
     * 터치 종료 상태입니다.<br>
     */
    TOUCH_UP,
}