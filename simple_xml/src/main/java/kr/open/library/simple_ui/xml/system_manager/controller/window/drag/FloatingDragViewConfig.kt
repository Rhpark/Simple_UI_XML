package kr.open.library.simple_ui.xml.system_manager.controller.window.drag

import android.graphics.Point
import android.graphics.PointF
import android.view.View
import kotlin.math.abs

/**
 * Configuration helper for draggable floating views.<br><br>
 * 드래그 가능한 플로팅 뷰의 터치 이벤트와 이동/클릭 판정을 처리하는 구성 도우미입니다.<br>
 *
 * Key features:<br><br>
 * 주요 기능:<br>
 * - Touch event handling.<br>
 * - Drag/click distinction detection.<br>
 * - Position calculation and updates.<br>
 */
internal class FloatingDragViewConfig(
    public val floatingView: FloatingDragView,
) {
    companion object {
        /**
         * Threshold to distinguish between click and drag (pixels).<br><br>
         * 클릭과 드래그를 구분하는 임계값(픽셀)입니다.<br>
         */
        private const val CLICK_THRESHOLD_PIXELS = 15

        /**
         * Default coordinate value.<br><br>
         * 기본 좌표값입니다.<br>
         */
        private const val DEFAULT_COORDINATE = 0
    }

    /**
     * Drag-related values.<br><br>
     * 드래그 관련 값입니다.<br>
     */
    private var initialTouchDownPosition: Point = Point(DEFAULT_COORDINATE, DEFAULT_COORDINATE)
    private var initialTouchDragPosition: PointF = PointF(DEFAULT_COORDINATE.toFloat(), DEFAULT_COORDINATE.toFloat())

    /**
     * Click-related values.<br><br>
     * 클릭 관련 값입니다.<br>
     */
    private var isDragging = false
    private var initialClickDownPosition: PointF = PointF(DEFAULT_COORDINATE.toFloat(), DEFAULT_COORDINATE.toFloat())

    /**
     * Handles touch down events.<br><br>
     * 터치 다운 이벤트를 처리합니다.<br>
     *
     * @param rawX Touch X coordinate.<br><br>
     *             터치 X 좌표입니다.<br>
     * @param rawY Touch Y coordinate.<br><br>
     *             터치 Y 좌표입니다.<br>
     */
    public fun onTouchDown(rawX: Float, rawY: Float) {
        setLocation(rawX, rawY)
    }

    /**
     * Handles touch move events.<br><br>
     * 터치 이동 이벤트를 처리합니다.<br>
     *
     * @param rawX Moved X coordinate.<br><br>
     *             이동한 X 좌표입니다.<br>
     * @param rawY Moved Y coordinate.<br><br>
     *             이동한 Y 좌표입니다.<br>
     */
    public fun onTouchMove(rawX: Float, rawY: Float) {
        changeLocation(rawX, rawY)
    }

    /**
     * Handles touch up events.<br><br>
     * 터치 업 이벤트를 처리합니다.<br>
     */
    public fun onTouchUp() {
        isDragging = false
    }

    /**
     * Returns whether the view is currently dragging.<br><br>
     * 현재 드래그 중인지 여부를 반환합니다.<br>
     *
     * @return true if dragging is in progress.<br><br>
     *         드래그 중이면 true를 반환합니다.<br>
     */
    public fun getIsDragging(): Boolean = isDragging

    /**
     * Sets touch location for drag and click tracking.<br><br>
     * 드래그/클릭 판별을 위해 터치 위치를 기록합니다.<br>
     *
     * @param x X coordinate.<br><br>
     *          X 좌표입니다.<br>
     * @param y Y coordinate.<br><br>
     *          Y 좌표입니다.<br>
     */
    private fun setLocation(x: Float, y: Float) {
        initDrag(x, y)
        initClick(x, y)
    }

    /**
     * Initializes drag-related values.<br><br>
     * 드래그 관련 값을 초기화합니다.<br>
     *
     * @param x Initial X coordinate.<br><br>
     *          초기 X 좌표입니다.<br>
     * @param y Initial Y coordinate.<br><br>
     *          초기 Y 좌표입니다.<br>
     */
    private fun initDrag(x: Float, y: Float) {
        initialTouchDownPosition.set(floatingView.params.x, floatingView.params.y)
        initialTouchDragPosition.set(x, y)
    }

    /**
     * Initializes click-related values.<br><br>
     * 클릭 관련 값을 초기화합니다.<br>
     *
     * @param x Initial X coordinate.<br><br>
     *          초기 X 좌표입니다.<br>
     * @param y Initial Y coordinate.<br><br>
     *          초기 Y 좌표입니다.<br>
     */
    private fun initClick(x: Float, y: Float) {
        isDragging = false
        initialClickDownPosition.set(x, y)
    }

    /**
     * Handles location changes from touch move events.<br><br>
     * 터치 이동에 따른 위치 변화를 처리합니다.<br>
     *
     * @param x New X coordinate.<br><br>
     *          변경된 X 좌표입니다.<br>
     * @param y New Y coordinate.<br><br>
     *          변경된 Y 좌표입니다.<br>
     */
    private fun changeLocation(x: Float, y: Float) {
        touchDragChangeLocation(x, y)
        clickDragChanged(x, y)
    }

    /**
     * Updates position based on drag distance.<br><br>
     * 드래그 이동 거리에 따라 위치를 갱신합니다.<br>
     *
     * @param x Current X coordinate.<br><br>
     *          현재 X 좌표입니다.<br>
     * @param y Current Y coordinate.<br><br>
     *          현재 Y 좌표입니다.<br>
     */
    private fun touchDragChangeLocation(x: Float, y: Float) {
        floatingView.params.x = initialTouchDownPosition.x + (x - initialTouchDragPosition.x).toInt()
        floatingView.params.y = initialTouchDownPosition.y + (y - initialTouchDragPosition.y).toInt()
    }

    /**
     * Distinguishes between click and drag and updates the drag state.<br><br>
     * 클릭과 드래그를 구분해 드래그 상태를 갱신합니다.<br>
     *
     * @param x Current X coordinate.<br><br>
     *          현재 X 좌표입니다.<br>
     * @param y Current Y coordinate.<br><br>
     *          현재 Y 좌표입니다.<br>
     */
    private fun clickDragChanged(x: Float, y: Float) {
        if (abs(x - initialClickDownPosition.x) > CLICK_THRESHOLD_PIXELS ||
            abs(y - initialClickDownPosition.y) > CLICK_THRESHOLD_PIXELS) {
            isDragging = true
        }
    }

    /**
     * Returns the underlying floating view.<br><br>
     * 내부 플로팅 뷰를 반환합니다.<br>
     *
     * @return Floating view instance.<br><br>
     *         플로팅 뷰 인스턴스입니다.<br>
     */
    public fun getView(): View = floatingView.view
}