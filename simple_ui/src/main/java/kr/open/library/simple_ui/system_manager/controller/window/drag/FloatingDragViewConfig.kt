package kr.open.library.simple_ui.system_manager.controller.window.drag

import android.graphics.Point
import android.graphics.PointF
import android.view.View
import kotlin.math.abs

/**
 * FloatingDragViewConfig - 플로팅 드래그 뷰 설정 클래스
 * Floating Drag View Configuration Class
 * 
 * 드래그 가능한 플로팅 뷰의 터치 이벤트 처리 및 드래그/클릭 감지를 담당합니다.
 * Handles touch events and drag/click detection for draggable floating views.
 * 
 * 주요 기능 / Main Features:
 * - 터치 이벤트 처리 / Touch event handling
 * - 드래그/클릭 구분 감지 / Drag/click distinction detection
 * - 위치 계산 및 업데이트 / Position calculation and updates
 */
internal class FloatingDragViewConfig(
    public val floatingView: FloatingDragView,

) {
    companion object {
        /**
         * 클릭과 드래그를 구분하는 임계값 (픽셀)
         * Threshold to distinguish between click and drag (pixels)
         */
        private const val CLICK_THRESHOLD_PIXELS = 15
        
        /**
         * 기본 좌표값
         * Default coordinate value
         */
        private const val DEFAULT_COORDINATE = 0
    }
    /**
     * 드래그 관련 값들
     * Drag-related values
     */
    private var initialTouchDownPosition: Point = Point(DEFAULT_COORDINATE, DEFAULT_COORDINATE)
    private var initialTouchDragPosition: PointF = PointF(DEFAULT_COORDINATE.toFloat(), DEFAULT_COORDINATE.toFloat())

    /**
     * 클릭 관련 값들
     * Click-related values
     */
    private var isDragging = false
    private var initialClickDownPosition: PointF = PointF(DEFAULT_COORDINATE.toFloat(), DEFAULT_COORDINATE.toFloat())

    /**
     * 터치 다운 이벤트 처리
     * Handles touch down events
     * 
     * @param rawX 터치 X 좌표 / Touch X coordinate
     * @param rawY 터치 Y 좌표 / Touch Y coordinate
     */
    public fun onTouchDown(rawX: Float, rawY: Float) {
        setLocation(rawX, rawY)
    }

    /**
     * 터치 이동 이벤트 처리
     * Handles touch move events
     * 
     * @param rawX 이동된 X 좌표 / Moved X coordinate
     * @param rawY 이동된 Y 좌표 / Moved Y coordinate
     */
    public fun onTouchMove(rawX: Float, rawY: Float) {
        changeLocation(rawX, rawY)
    }

    /**
     * 터치 업 이벤트 처리
     * Handles touch up events
     */
    public fun onTouchUp() {
        isDragging = false
    }

    /**
     * 현재 드래그 상태를 반환합니다.
     * Returns current drag status.
     * 
     * @return 드래그 중인지 여부 / Whether currently dragging
     */
    public fun getIsDragging(): Boolean = isDragging

    /**
     * 터치 위치를 설정합니다.
     * Sets touch location.
     * 
     * @param x X 좌표 / X coordinate
     * @param y Y 좌표 / Y coordinate
     */
    private fun setLocation(x: Float, y: Float) {
        initDrag(x, y)
        initClick(x, y)
    }

    /**
     * 드래그 관련 초기값을 설정합니다.
     * Initializes drag-related values.
     * 
     * @param x 초기 X 좌표 / Initial X coordinate
     * @param y 초기 Y 좌표 / Initial Y coordinate
     */
    private fun initDrag(x: Float, y: Float) {
        initialTouchDownPosition.set(floatingView.params.x, floatingView.params.y)
        initialTouchDragPosition.set(x, y)
    }

    /**
     * 클릭 관련 초기값을 설정합니다.
     * Initializes click-related values.
     * 
     * @param x 초기 X 좌표 / Initial X coordinate
     * @param y 초기 Y 좌표 / Initial Y coordinate
     */
    private fun initClick(x: Float, y: Float) {
        isDragging = false
        initialClickDownPosition.set(x, y)
    }

    /**
     * 위치 변경을 처리합니다.
     * Handles location changes.
     * 
     * @param x 새로운 X 좌표 / New X coordinate
     * @param y 새로운 Y 좌표 / New Y coordinate
     */
    private fun changeLocation(x: Float, y: Float) {
        touchDragChangeLocation(x, y)
        clickDragChanged(x, y)
    }

    /**
     * 터치 드래그에 따른 위치를 변경합니다.
     * Changes position based on touch drag.
     * 
     * @param x 현재 X 좌표 / Current X coordinate
     * @param y 현재 Y 좌표 / Current Y coordinate
     */
    private fun touchDragChangeLocation(x: Float, y: Float) {
        floatingView.params.x = initialTouchDownPosition.x + (x - initialTouchDragPosition.x).toInt()
        floatingView.params.y = initialTouchDownPosition.y + (y - initialTouchDragPosition.y).toInt()
    }

    /**
     * 클릭과 드래그를 구분하여 드래그 상태를 업데이트합니다.
     * Updates drag state by distinguishing between click and drag.
     * 
     * @param x 현재 X 좌표 / Current X coordinate
     * @param y 현재 Y 좌표 / Current Y coordinate
     */
    private fun clickDragChanged(x: Float, y: Float) {
        if (abs(x - initialClickDownPosition.x) > CLICK_THRESHOLD_PIXELS ||
            abs(y - initialClickDownPosition.y) > CLICK_THRESHOLD_PIXELS) {
            isDragging = true
        }
    }

    /**
     * 플로팅 뷰를 반환합니다.
     * Returns the floating view.
     * 
     * @return 플로팅 뷰 / The floating view
     */
    public fun getView(): View = floatingView.view
}