package kr.open.library.simple_ui.xml.system_manager.controller.window.fixed

import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.WindowManager.LayoutParams
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch

/**
 * Fixed floating view that anchors an overlay at a given position.<br><br>
 * 지정한 위치에 오버레이를 고정하는 플로팅 뷰이며 드래그 뷰의 기반 클래스입니다.<br>
 *
 * Key features:<br><br>
 * 주요 기능:<br>
 * - API level-specific layout parameter configuration.<br>
 * - View bounds calculation.<br>
 * - Floating overlay permission handling.<br>
 */
public open class FloatingFixedView(
    public val view: View,
    public val startX: Int,
    public val startY: Int,
) {
    /**
     * Layout parameters for the floating view.<br><br>
     * 플로팅 뷰의 레이아웃 파라미터입니다.<br>
     */
    public val params: LayoutParams =
        getFloatingLayoutParam().apply {
            gravity = Gravity.TOP or Gravity.LEFT
            this.x = startX
            this.y = startY
        }

    /**
     * Creates floating layout parameters based on API level.<br><br>
     * API 레벨에 따라 플로팅 레이아웃 파라미터를 생성합니다.<br>
     *
     * SECURITY NOTE: For security, uses the most restrictive window type available.<br><br>
     * 보안상 가장 제한적인 윈도우 타입을 사용합니다.<br>
     *
     * @return Floating layout parameters.<br><br>
     *         플로팅 레이아웃 파라미터입니다.<br>
     */
    private fun getFloatingLayoutParam(): LayoutParams =
        safeCatch(defaultValue = getDefaultLayoutParam()) {
            return LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.TYPE_APPLICATION_OVERLAY,
                LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT,
            )
        }

    /**
     * Calculates the bounds of the floating view.<br><br>
     * 플로팅 뷰의 영역을 계산합니다.<br>
     *
     * @return View bounds information.<br><br>
     *         뷰의 영역 정보입니다.<br>
     */
    public fun getRect(): Rect =
        safeCatch(defaultValue = Rect()) {
            val width = if (view.width > 0) view.width else view.measuredWidth
            val height = if (view.height > 0) view.height else view.measuredHeight
            return Rect(params.x, params.y, params.x + width, params.y + height)
        }

    /**
     * Creates default layout parameters (fallback for errors).<br><br>
     * 에러 대비용 기본 레이아웃 파라미터를 생성합니다.<br>
     *
     * @return Default layout parameters.<br><br>
     *         기본 레이아웃 파라미터입니다.<br>
     */
    private fun getDefaultLayoutParam(): LayoutParams =
        LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.TYPE_APPLICATION_OVERLAY,
            LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        )
}
