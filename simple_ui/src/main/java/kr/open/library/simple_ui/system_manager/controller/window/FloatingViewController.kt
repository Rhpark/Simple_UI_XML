package kr.open.library.simple_ui.system_manager.controller.window

import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import kr.open.library.simple_ui.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.system_manager.controller.window.drag.FloatingDragView
import kr.open.library.simple_ui.system_manager.controller.window.drag.FloatingDragViewConfig
import kr.open.library.simple_ui.system_manager.controller.window.fixed.FloatingFixedView
import kr.open.library.simple_ui.system_manager.controller.window.vo.FloatingViewCollisionsType
import kr.open.library.simple_ui.system_manager.controller.window.vo.FloatingViewTouchType
import kr.open.library.simple_ui.system_manager.extensions.getWindowManager


/**
 * FloatingViewController - 플로팅 뷰 관리 컨트롤러
 * Floating View Management Controller
 * 
 * 드래그 가능한 뷰와 고정 뷰를 관리하며, 충돌 감지 및 뷰 위치 업데이트 기능을 제공합니다.
 * Manages draggable and fixed floating views, providing collision detection and view position update functionality.
 * 
 * 주요 기능 / Main Features:
 * - 플로팅 뷰 추가/제거 / Add/Remove floating views
 * - 드래그 뷰와 고정 뷰 간 충돌 감지 / Collision detection between drag and fixed views
 * - 윈도우 매니저를 통한 뷰 관리 / View management through WindowManager
 * 
 * 필수 권한 / Required Permission:
 * - Android.Manifest.permission.SYSTEM_ALERT_WINDOW
 */
public open class FloatingViewController(context: Context) :
    BaseSystemService(context, listOf(android.Manifest.permission.SYSTEM_ALERT_WINDOW)) {

    public val windowManager: WindowManager by lazy { context.getWindowManager() }

    private var floatingDragViewInfoList: MutableList<FloatingDragViewConfig> = mutableListOf()
    private var floatingFixedView: FloatingFixedView? = null


    /**
     * 고정 플로팅 뷰를 설정합니다.
     * Sets the fixed floating view.
     * 
     * @param floatingView 설정할 고정 플로팅 뷰 (null이면 제거) / Fixed floating view to set (remove if null)
     * @return 성공 여부 / Success status
     */
    public fun setFloatingFixedView(floatingView: FloatingFixedView?): Boolean = tryCatchSystemManager(false) {
        if(floatingView == null) {
            removeFloatingFixedView()
        } else {
            addView(floatingView.view, floatingView.params)
        }
        this.floatingFixedView = floatingView
        true
    }


    /**
     * 현재 설정된 고정 플로팅 뷰를 반환합니다.
     * Returns the currently set fixed floating view.
     * 
     * @return 고정 플로팅 뷰 (없으면 null) / Fixed floating view (null if none)
     */
    public fun getFloatingFixedView(): FloatingFixedView? = floatingFixedView

    /**
     * 드래그 가능한 플로팅 뷰를 추가합니다.
     * Adds a draggable floating view.
     * 
     * @param floatingView 추가할 드래그 플로팅 뷰 / Draggable floating view to add
     * @return 추가 성공 여부 / Addition success status
     */
    public fun addFloatingDragView(floatingView: FloatingDragView): Boolean = tryCatchSystemManager(false) {
        val config = FloatingDragViewConfig(floatingView)

        floatingView.view.setOnTouchListener{ view, event->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    config.onTouchDown(event.rawX, event.rawY)
                    floatingView.updateCollisionState(
                        FloatingViewTouchType.TOUCH_DOWN,
                        getCollisionTypeWithFixedView(floatingView)
                    )
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    config.onTouchMove(event.rawX, event.rawY)
                    updateView(view, floatingView.params)
                    floatingView.updateCollisionState(
                        FloatingViewTouchType.TOUCH_MOVE,
                        getCollisionTypeWithFixedView(floatingView)
                    )
                    // 중복 호출 제거됨 (기존 버그 수정)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    floatingView.updateCollisionState(
                        FloatingViewTouchType.TOUCH_UP,
                        getCollisionTypeWithFixedView(floatingView)
                    )
                    if (!config.getIsDragging()) { floatingView.view.performClick() }
                    config.onTouchUp()
                    true
                }

                else -> false
            }
        }

        floatingDragViewInfoList.add(config)
        addView(config.getView(), floatingView.params)
        true
    }


    private fun getCollisionTypeWithFixedView(floatingDragView: FloatingDragView): FloatingViewCollisionsType =
        if(isCollisionFixedView(floatingDragView)) FloatingViewCollisionsType.OCCURING
        else FloatingViewCollisionsType.UNCOLLISIONS

    private fun isCollisionFixedView(floatingDragView: FloatingDragView): Boolean =
        floatingFixedView?.let { Rect.intersects(floatingDragView.getRect(), it.getRect()) } ?: false

    /**
     * 뷰의 레이아웃을 업데이트합니다.
     * Updates the view's layout.
     * 
     * @param view 업데이트할 뷰 / View to update
     * @param params 새로운 레이아웃 파라미터 / New layout parameters
     * @return 업데이트 성공 여부 / Update success status
     */
    public fun updateView(view: View, params: LayoutParams): Boolean = tryCatchSystemManager(false) {
        params.x = params.x.coerceAtLeast(0)
        params.y = params.y.coerceAtLeast(0)
        windowManager.updateViewLayout(view, params)
        true
    }


    /**
     * 윈도우 매니저에 뷰를 추가합니다.
     * Adds a view to the window manager.
     * 
     * @param view 추가할 뷰 / View to add
     * @param params 레이아웃 파라미터 / Layout parameters
     * @return 추가 성공 여부 / Addition success status
     */
    public fun addView(view: View, params: LayoutParams): Boolean = tryCatchSystemManager(false) {
        windowManager.addView(view, params)
        true
    }


    /**
     * 드래그 플로팅 뷰를 제거합니다.
     * Removes a draggable floating view.
     * 
     * @param floatingView 제거할 드래그 플로팅 뷰 / Draggable floating view to remove
     * @return 제거 성공 여부 / Removal success status
     */
    public fun removeFloatingDragView(floatingView: FloatingDragView): Boolean = tryCatchSystemManager(false) {
        floatingDragViewInfoList.find { it.floatingView == floatingView }?.let {
            it.getView().setOnTouchListener(null)
            removeView(it.getView())
            floatingDragViewInfoList.remove(it)
            true
        } ?: false
    }

    /**
     * 윈도우 매니저에서 뷰를 제거합니다.
     * Removes a view from the window manager.
     * 
     * @param view 제거할 뷰 / View to remove
     * @return 제거 성공 여부 / Removal success status
     */
    public fun removeView(view: View): Boolean = tryCatchSystemManager(false) {
        windowManager.removeView(view)
        true
    }


    /**
     * 고정 플로팅 뷰를 제거합니다.
     * Removes the fixed floating view.
     * 
     * @return 제거 성공 여부 / Removal success status
     */
    public fun removeFloatingFixedView(): Boolean = tryCatchSystemManager(false) {
        floatingFixedView?.let { removeView(it.view) }
        floatingFixedView = null
        true
    }

    /**
     * 모든 플로팅 뷰를 제거합니다.
     * Removes all floating views.
     * 
     * @return 제거 성공 여부 / Removal success status
     */
    public fun removeAllFloatingView(): Boolean = tryCatchSystemManager(false) {
        val configs = floatingDragViewInfoList.toList()
        configs.forEach { removeFloatingDragView(it.floatingView) }
        floatingDragViewInfoList.clear()
        removeFloatingFixedView()
        true
    }



    /**
     * Enhanced destroy method with proper cleanup and error handling.
     * 적절한 정리 작업과 오류 처리가 포함된 향상된 소멸 메서드입니다.
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