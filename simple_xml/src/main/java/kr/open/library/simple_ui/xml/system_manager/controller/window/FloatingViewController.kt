package kr.open.library.simple_ui.xml.system_manager.controller.window

import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getWindowManager
import kr.open.library.simple_ui.xml.system_manager.controller.window.drag.FloatingDragView
import kr.open.library.simple_ui.xml.system_manager.controller.window.drag.FloatingDragViewConfig
import kr.open.library.simple_ui.xml.system_manager.controller.window.fixed.FloatingFixedView
import kr.open.library.simple_ui.xml.system_manager.controller.window.vo.FloatingViewCollisionsType
import kr.open.library.simple_ui.xml.system_manager.controller.window.vo.FloatingViewTouchType

/**
 * Floating view management controller that handles draggable and fixed overlays.<br><br>
 * 드래그형과 고정형 오버레이를 관리하는 플로팅 뷰 컨트롤러입니다.<br>
 *
 * Provides collision detection, position updates, and add/remove operations via `WindowManager`.<br><br>
 * `WindowManager`를 통해 충돌 감지, 위치 업데이트, 추가/제거를 처리합니다.<br>
 *
 * **Required permission**: `android.Manifest.permission.SYSTEM_ALERT_WINDOW`.<br><br>
 * **필수 권한**: `android.Manifest.permission.SYSTEM_ALERT_WINDOW`.<br>
 */
public open class FloatingViewController(
    context: Context,
) : BaseSystemService(context, listOf(android.Manifest.permission.SYSTEM_ALERT_WINDOW)) {
    public val windowManager: WindowManager by lazy { context.getWindowManager() }

    private var floatingDragViewInfoList: MutableList<FloatingDragViewConfig> = mutableListOf()
    private var floatingFixedView: FloatingFixedView? = null

    /**
     * Sets the fixed floating view (removes it when null).<br><br>
     * 고정 플로팅 뷰를 설정하며, null이면 제거합니다.<br>
     *
     * @param floatingView Fixed floating view to set, or null to remove.<br><br>
     *                     설정할 고정 플로팅 뷰이며 null이면 제거합니다.<br>
     * @return true when the view was applied without errors.<br><br>
     *         오류 없이 적용되면 true를 반환합니다.<br>
     */
    public fun setFloatingFixedView(floatingView: FloatingFixedView?): Boolean =
        tryCatchSystemManager(false) {
            if (floatingView == null) {
                removeFloatingFixedView()
            } else {
                addView(floatingView.view, floatingView.params)
            }
            this.floatingFixedView = floatingView
            return true
        }

    /**
     * Returns the currently registered fixed floating view.<br><br>
     * 현재 등록된 고정 플로팅 뷰를 반환합니다.<br>
     *
     * @return Fixed floating view, or null when none is set.<br><br>
     *         설정된 고정 뷰가 없으면 null을 반환합니다.<br>
     */
    public fun getFloatingFixedView(): FloatingFixedView? = floatingFixedView

    /**
     * Adds a draggable floating view and wires collision callbacks.<br><br>
     * 드래그 가능한 플로팅 뷰를 추가하고 충돌 콜백을 연결합니다.<br>
     *
     * @param floatingView Draggable floating view to add.<br><br>
     *                     추가할 드래그 플로팅 뷰입니다.<br>
     * @return true if the view was added and listeners registered.<br><br>
     *         뷰 추가와 리스너 등록이 완료되면 true를 반환합니다.<br>
     */
    public fun addFloatingDragView(floatingView: FloatingDragView): Boolean =
        tryCatchSystemManager(false) {
            val config = FloatingDragViewConfig(floatingView)

            floatingView.view.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        config.onTouchDown(event.rawX, event.rawY)
                        floatingView.updateCollisionState(
                            FloatingViewTouchType.TOUCH_DOWN,
                            getCollisionTypeWithFixedView(floatingView),
                        )
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        config.onTouchMove(event.rawX, event.rawY)
                        updateView(view, floatingView.params)
                        floatingView.updateCollisionState(
                            FloatingViewTouchType.TOUCH_MOVE,
                            getCollisionTypeWithFixedView(floatingView),
                        )
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        floatingView.updateCollisionState(
                            FloatingViewTouchType.TOUCH_UP,
                            getCollisionTypeWithFixedView(floatingView),
                        )
                        if (!config.getIsDragging()) {
                            floatingView.view.performClick()
                        }
                        config.onTouchUp()
                        true
                    }

                    else -> false
                }
            }

            floatingDragViewInfoList.add(config)
            addView(config.getView(), floatingView.params)
            return true
        }

    private fun getCollisionTypeWithFixedView(floatingDragView: FloatingDragView): FloatingViewCollisionsType =
        if (isCollisionFixedView(floatingDragView)) {
            FloatingViewCollisionsType.OCCURING
        } else {
            FloatingViewCollisionsType.UNCOLLISIONS
        }

    private fun isCollisionFixedView(floatingDragView: FloatingDragView): Boolean =
        floatingFixedView?.let { Rect.intersects(floatingDragView.getRect(), it.getRect()) } ?: false

    /**
     * Updates the view layout while clamping coordinates to non-negative values.<br><br>
     * 좌표를 0 이상으로 보정하며 뷰 레이아웃을 업데이트합니다.<br>
     *
     * @param view View to update.<br><br>
     *             업데이트할 뷰입니다.<br>
     * @param params New layout parameters to apply.<br><br>
     *               적용할 새 레이아웃 파라미터입니다.<br>
     * @return true if the layout update succeeded.<br><br>
     *         레이아웃 업데이트에 성공하면 true를 반환합니다.<br>
     */
    public fun updateView(
        view: View,
        params: LayoutParams,
    ): Boolean =
        tryCatchSystemManager(false) {
            params.x = params.x.coerceAtLeast(0)
            params.y = params.y.coerceAtLeast(0)
            windowManager.updateViewLayout(view, params)
            return true
        }

    /**
     * Adds a view to the window manager.<br><br>
     * 윈도우 매니저에 뷰를 추가합니다.<br>
     *
     * @param view View to add.<br><br>
     *             추가할 뷰입니다.<br>
     * @param params Layout parameters for the view.<br><br>
     *               적용할 레이아웃 파라미터입니다.<br>
     * @return true if the view was added without errors.<br><br>
     *         오류 없이 추가되면 true를 반환합니다.<br>
     */
    public fun addView(
        view: View,
        params: LayoutParams,
    ): Boolean =
        tryCatchSystemManager(false) {
            windowManager.addView(view, params)
            return true
        }

    /**
     * Removes a draggable floating view and detaches its listeners.<br><br>
     * 드래그 플로팅 뷰를 제거하고 리스너를 해제합니다.<br>
     *
     * @param floatingView Draggable floating view to remove.<br><br>
     *                     제거할 드래그 플로팅 뷰입니다.<br>
     * @return true if the view was found and removed.<br><br>
     *         뷰를 찾아 제거하면 true를 반환합니다.<br>
     */
    public fun removeFloatingDragView(floatingView: FloatingDragView): Boolean =
        tryCatchSystemManager(false) {
            floatingDragViewInfoList.find { it.floatingView == floatingView }?.let {
                it.getView().setOnTouchListener(null)
                removeView(it.getView())
                floatingDragViewInfoList.remove(it)
                return true
            } ?: return false
        }

    /**
     * Removes a view from the window manager.<br><br>
     * 윈도우 매니저에서 뷰를 제거합니다.<br>
     *
     * @param view View to remove.<br><br>
     *             제거할 뷰입니다.<br>
     * @return true if removal succeeded without errors.<br><br>
     *         오류 없이 제거되면 true를 반환합니다.<br>
     */
    public fun removeView(view: View): Boolean =
        tryCatchSystemManager(false) {
            windowManager.removeView(view)
            return true
        }

    /**
     * Removes the fixed floating view if present.<br><br>
     * 고정 플로팅 뷰가 있으면 제거합니다.<br>
     *
     * @return true after clearing the fixed view reference.<br><br>
     *         고정 뷰 참조를 해제하면 true를 반환합니다.<br>
     */
    public fun removeFloatingFixedView(): Boolean =
        tryCatchSystemManager(false) {
            floatingFixedView?.let { removeView(it.view) }
            floatingFixedView = null
            return true
        }

    /**
     * Removes all floating views (drag and fixed) and clears stored configs.<br><br>
     * 모든 드래그/고정 플로팅 뷰를 제거하고 저장된 구성을 비웁니다.<br>
     *
     * @return true after cleanup completes.<br><br>
     *         정리 완료 후 true를 반환합니다.<br>
     */
    public fun removeAllFloatingView(): Boolean =
        tryCatchSystemManager(false) {
            val configs = floatingDragViewInfoList.toList()
            configs.forEach { removeFloatingDragView(it.floatingView) }
            floatingDragViewInfoList.clear()
            removeFloatingFixedView()
            return true
        }

    /**
     * Enhanced destroy method with cleanup and error handling.<br><br>
     * 정리 작업과 오류 처리를 포함한 개선된 소멸 메서드입니다.<br>
     */
    override fun onDestroy() {
        try {
            // Clear all touch listeners to prevent memory leaks
            floatingDragViewInfoList.forEach { config ->
                safeCatch { config.getView().setOnTouchListener(null) }
            }

            removeAllFloatingView()
            floatingDragViewInfoList.clear()
        } catch (e: Exception) {
            Logx.e("Error during FloatingViewController cleanup: ${e.message}")
        } finally {
            super.onDestroy()
        }
    }
}
