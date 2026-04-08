package kr.open.library.simple_ui.system_manager.xml.controller.window

import android.Manifest.permission.SYSTEM_ALERT_WINDOW
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.system_manager.core.base.BaseSystemService
import kr.open.library.simple_ui.system_manager.core.base.SystemResult
import kr.open.library.simple_ui.system_manager.core.extensions.getWindowManager
import kr.open.library.simple_ui.system_manager.xml.controller.window.drag.FloatingDragView
import kr.open.library.simple_ui.system_manager.xml.controller.window.drag.FloatingDragViewConfig
import kr.open.library.simple_ui.system_manager.xml.controller.window.fixed.FloatingFixedView
import kr.open.library.simple_ui.system_manager.xml.controller.window.vo.FloatingViewCollisionsType
import kr.open.library.simple_ui.system_manager.xml.controller.window.vo.FloatingViewTouchType

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
     * @return [SystemResult.Success] when WindowManager apply/remove operation succeeds and internal reference is updated accordingly,
     *         [SystemResult.PermissionDenied] if permission is missing, [SystemResult.Failure] on error.<br><br>
     *         WindowManager 적용/제거가 성공하고 내부 참조가 갱신되면 [SystemResult.Success],
     *         권한 없음 시 [SystemResult.PermissionDenied], 오류 시 [SystemResult.Failure]입니다.<br>
     */
    @RequiresPermission(SYSTEM_ALERT_WINDOW)
    public fun setFloatingFixedView(floatingView: FloatingFixedView?): SystemResult<Unit> =
        tryCatchSystemManagerResult {
            if (floatingView == null) {
                return@tryCatchSystemManagerResult removeFloatingFixedView()
            }

            val current = this.floatingFixedView

            if (current === floatingView) return@tryCatchSystemManagerResult SystemResult.Success(Unit)

            if (current == null) {
                val addResult = addView(floatingView.view, floatingView.params)
                if (addResult !is SystemResult.Success) return@tryCatchSystemManagerResult addResult
                this.floatingFixedView = floatingView
                return@tryCatchSystemManagerResult SystemResult.Success(Unit)
            }

            val addResult = addView(floatingView.view, floatingView.params)
            if (addResult !is SystemResult.Success) return@tryCatchSystemManagerResult addResult

            val removeResult = removeView(current.view)
            if (removeResult !is SystemResult.Success) {
                Logx.w("Failed to remove previous fixed view. Start rollback for new fixed view.")
                if (removeView(floatingView.view) !is SystemResult.Success) {
                    Logx.e("Rollback failed while replacing fixed view.")
                }
                return@tryCatchSystemManagerResult SystemResult.Failure(null)
            }

            this.floatingFixedView = floatingView
            SystemResult.Success(Unit)
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
     * @return [SystemResult.Success] if WindowManager add succeeds and the config/listener state is committed,
     *         [SystemResult.PermissionDenied] if permission is missing, [SystemResult.Failure] on error.<br><br>
     *         WindowManager 추가 성공 시 [SystemResult.Success],
     *         권한 없음 시 [SystemResult.PermissionDenied], 오류 시 [SystemResult.Failure]입니다.<br>
     */
    @RequiresPermission(SYSTEM_ALERT_WINDOW)
    public fun addFloatingDragView(floatingView: FloatingDragView): SystemResult<Unit> = tryCatchSystemManagerResult {
        val config = FloatingDragViewConfig(floatingView)

        setupTouchListener(config)

        val addResult = addView(config.getView(), floatingView.params)
        if (addResult !is SystemResult.Success) {
            config.getView().setOnTouchListener(null)
            return@tryCatchSystemManagerResult addResult
        }
        floatingDragViewInfoList.add(config)
        SystemResult.Success(Unit)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener(config: FloatingDragViewConfig) {
        config.floatingView.view.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> onTouchDownDragView(config, event)

                MotionEvent.ACTION_MOVE -> onTouchMoveDragView(config, view, event)

                MotionEvent.ACTION_UP -> onTouchUpDragView(config)

                else -> false
            }
        }
    }

    private fun onTouchDownDragView(config: FloatingDragViewConfig, event: MotionEvent): Boolean {
        config.onTouchDown(event.rawX, event.rawY)
        config.floatingView.updateCollisionState(FloatingViewTouchType.TOUCH_DOWN, getCollisionTypeWithFixedView(config.floatingView))
        return true
    }

    private fun onTouchMoveDragView(config: FloatingDragViewConfig, view: View, event: MotionEvent): Boolean {
        config.onTouchMove(event.rawX, event.rawY)
        updateView(view, config.floatingView.params)
        config.floatingView.updateCollisionState(FloatingViewTouchType.TOUCH_MOVE, getCollisionTypeWithFixedView(config.floatingView))
        return true
    }

    private fun onTouchUpDragView(config: FloatingDragViewConfig): Boolean {
        config.floatingView.updateCollisionState(
            FloatingViewTouchType.TOUCH_UP,
            getCollisionTypeWithFixedView(config.floatingView),
        )
        if (!config.getIsDragging()) {
            config.floatingView.view.performClick()
        }
        config.onTouchUp()
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
     * @return [SystemResult.Success] if the layout update succeeded,
     *         [SystemResult.PermissionDenied] if permission is missing, [SystemResult.Failure] on error.<br><br>
     *         레이아웃 업데이트 성공 시 [SystemResult.Success],
     *         권한 없음 시 [SystemResult.PermissionDenied], 오류 시 [SystemResult.Failure]입니다.<br>
     */
    public fun updateView(view: View, params: LayoutParams): SystemResult<Unit> = tryCatchSystemManagerResult {
        params.x = params.x.coerceAtLeast(0)
        params.y = params.y.coerceAtLeast(0)
        windowManager.updateViewLayout(view, params)
        SystemResult.Success(Unit)
    }

    /**
     * Adds a view to the window manager.<br><br>
     * 윈도우 매니저에 뷰를 추가합니다.<br>
     *
     * @param view View to add.<br><br>
     *             추가할 뷰입니다.<br>
     * @param params Layout parameters for the view.<br><br>
     *               적용할 레이아웃 파라미터입니다.<br>
     * @return [SystemResult.Success] if the view was added without errors,
     *         [SystemResult.PermissionDenied] if permission is missing, [SystemResult.Failure] on error.<br><br>
     *         오류 없이 추가 성공 시 [SystemResult.Success],
     *         권한 없음 시 [SystemResult.PermissionDenied], 오류 시 [SystemResult.Failure]입니다.<br>
     */
    public fun addView(view: View, params: LayoutParams): SystemResult<Unit> = tryCatchSystemManagerResult {
        windowManager.addView(view, params)
        SystemResult.Success(Unit)
    }

    /**
     * Removes a draggable floating view and detaches its listeners.<br><br>
     * 드래그 플로팅 뷰를 제거하고 리스너를 해제합니다.<br>
     *
     * @param floatingView Draggable floating view to remove.<br><br>
     *                     제거할 드래그 플로팅 뷰입니다.<br>
     * @return [SystemResult.Success] if target is found and WindowManager remove succeeds,
     *         [SystemResult.PermissionDenied] if permission is missing, [SystemResult.Failure] on error or not found.<br><br>
     *         대상을 찾아 제거 성공 시 [SystemResult.Success],
     *         권한 없음 시 [SystemResult.PermissionDenied], 오류 또는 대상 없음 시 [SystemResult.Failure]입니다.<br>
     */
    public fun removeFloatingDragView(floatingView: FloatingDragView): SystemResult<Unit> = tryCatchSystemManagerResult {
        val config = floatingDragViewInfoList.find { it.floatingView == floatingView }
            ?: return@tryCatchSystemManagerResult SystemResult.Failure(null)
        config.getView().setOnTouchListener(null)
        val removeResult = removeView(config.getView())
        if (removeResult !is SystemResult.Success) return@tryCatchSystemManagerResult removeResult
        floatingDragViewInfoList.remove(config)
        SystemResult.Success(Unit)
    }

    /**
     * Removes a view from the window manager.<br><br>
     * 윈도우 매니저에서 뷰를 제거합니다.<br>
     *
     * @param view View to remove.<br><br>
     *             제거할 뷰입니다.<br>
     * @return [SystemResult.Success] if removal succeeded without errors,
     *         [SystemResult.PermissionDenied] if permission is missing, [SystemResult.Failure] on error.<br><br>
     *         오류 없이 제거 성공 시 [SystemResult.Success],
     *         권한 없음 시 [SystemResult.PermissionDenied], 오류 시 [SystemResult.Failure]입니다.<br>
     */
    public fun removeView(view: View): SystemResult<Unit> = tryCatchSystemManagerResult {
        windowManager.removeView(view)
        SystemResult.Success(Unit)
    }

    /**
     * Removes the fixed floating view if present.<br><br>
     * 고정 플로팅 뷰가 있으면 제거합니다.<br>
     *
     * @return [SystemResult.Success] when WindowManager remove succeeds (or no fixed view exists) and the reference is cleared,
     *         [SystemResult.PermissionDenied] if permission is missing, [SystemResult.Failure] on error.<br><br>
     *         WindowManager 제거 성공 또는 제거 대상 없음 시 [SystemResult.Success],
     *         권한 없음 시 [SystemResult.PermissionDenied], 오류 시 [SystemResult.Failure]입니다.<br>
     */
    public fun removeFloatingFixedView(): SystemResult<Unit> = tryCatchSystemManagerResult {
        floatingFixedView?.let {
            val removeResult = removeView(it.view)
            if (removeResult !is SystemResult.Success) return@tryCatchSystemManagerResult removeResult
        }
        floatingFixedView = null
        SystemResult.Success(Unit)
    }

    /**
     * Removes all floating views (drag and fixed).<br>
     * Uses first-failure-stop strategy: returns false immediately on the first failure.<br>
     * In that case, already-removed items stay removed and remaining items are left as-is (partial cleanup).<br><br>
     * 모든 드래그/고정 플로팅 뷰를 제거합니다.<br>
     * first-failure-stop 전략을 사용하며, 첫 실패 지점에서 즉시 false를 반환합니다.<br>
     * 이 경우 이미 제거된 항목은 제거된 상태로 유지되고, 남은 항목은 그대로 남는 부분 정리 상태가 될 수 있습니다.<br>
     *
     * @return [SystemResult.Success] when all remove operations succeed,
     *         [SystemResult.PermissionDenied] if permission is missing, [SystemResult.Failure] when any single step fails.<br><br>
     *         모든 제거 작업 성공 시 [SystemResult.Success],
     *         권한 없음 시 [SystemResult.PermissionDenied], 하나라도 실패 시 [SystemResult.Failure]입니다.<br>
     */
    public fun removeAllFloatingView(): SystemResult<Unit> = tryCatchSystemManagerResult {
        val configs = floatingDragViewInfoList.toList()
        for (config in configs) {
            val result = removeFloatingDragView(config.floatingView)
            if (result !is SystemResult.Success) return@tryCatchSystemManagerResult result
        }
        val fixedResult = removeFloatingFixedView()
        if (fixedResult !is SystemResult.Success) return@tryCatchSystemManagerResult fixedResult
        SystemResult.Success(Unit)
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
        } catch (e: RuntimeException) {
            Logx.e("Error during FloatingViewController cleanup: ${e.message}")
        } finally {
            super.onDestroy()
        }
    }
}
