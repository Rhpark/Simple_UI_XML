package kr.open.library.simple_ui.system_manager.controller.window.vo

/**
 * FloatingViewCollisionsType - 플로팅 뷰 충돌 상태 열거형
 * Floating View Collision State Enum
 * 
 * 플로팅 뷰 간의 충돌 상태를 나타냅니다.
 * Represents collision states between floating views.
 */
public enum class FloatingViewCollisionsType {
    /**
     * 충돌 발생 중
     * Collision occurring
     */
    OCCURING,
    
    /**
     * 충돌 없음
     * No collision
     */
    UNCOLLISIONS,
}

/**
 * FloatingViewTouchType - 플로팅 뷰 터치 상태 열거형
 * Floating View Touch State Enum
 * 
 * 플로팅 뷰의 터치 이벤트 단계를 나타냅니다.
 * Represents touch event phases for floating views.
 */
public enum class FloatingViewTouchType {
    /**
     * 터치 시작 (손가락이 화면에 닿음)
     * Touch start (finger touches screen)
     */
    TOUCH_DOWN,
    
    /**
     * 터치 이동 (드래그 중)
     * Touch move (dragging)
     */
    TOUCH_MOVE,
    
    /**
     * 터치 종료 (손가락이 화면에서 떨어짐)
     * Touch end (finger leaves screen)
     */
    TOUCH_UP,
}