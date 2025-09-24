package kr.open.library.simple_ui.system_manager.controller.softkeyboard

import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.system_manager.extensions.getInputMethodManager

/**
 * Controller for managing soft keyboard operations using InputMethodManager.
 * Provides comprehensive keyboard show/hide functionality with error handling.
 * 
 * InputMethodManager를 사용하여 소프트 키보드 작업을 관리하는 컨트롤러입니다.
 * 오류 처리와 함께 포괄적인 키보드 표시/숨김 기능을 제공합니다.
 * 
 * @see <a href="https://blog.naver.com/il7942li/222671675950">windowSoftInputMode reference</a>
 */
public open class SoftKeyboardController(context: Context) : BaseSystemService(context,null) {

    public val imm: InputMethodManager by lazy { context.getInputMethodManager() }

    /**
     * Sets window soft input mode to adjust pan.
     * Can be configured in manifest: android:windowSoftInputMode="adjustPan"
     *
     * 윈도우 소프트 입력 모드를 adjust pan으로 설정합니다.
     * 매니페스트에서 설정 가능: android:windowSoftInputMode="adjustPan"
     */
    public fun setAdjustPan(window: Window): Boolean = tryCatchSystemManager(false) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        true
    }

    /**
     * Sets custom soft input mode for the window.
     * Can be configured in manifest: android:windowSoftInputMode="..."
     *
     * 윈도우에 사용자 정의 소프트 입력 모드를 설정합니다.
     * 매니페스트에서 설정 가능: android:windowSoftInputMode="..."
     *
     * @param window Target window
     * @param softInputTypes Input mode types (e.g., SOFT_INPUT_ADJUST_PAN, SOFT_INPUT_MASK_STATE)
     */
    public fun setSoftInputMode(window: Window, softInputTypes: Int): Boolean = tryCatchSystemManager(false) {
        window.setSoftInputMode(softInputTypes)
        true
    }

    /**
     * Shows soft keyboard for input-capable views (EditText, SearchView, etc.).
     * Default flag options: SHOW_IMPLICIT, SHOW_FORCED (API 33 Deprecated)
     *
     * 입력 가능한 뷰(EditText, SearchView 등)에 소프트 키보드를 표시합니다.
     * 기본 플래그 옵션: SHOW_IMPLICIT, SHOW_FORCED (API 33에서 deprecated)
     */
    public fun show(v: View, flag: Int = InputMethodManager.SHOW_IMPLICIT): Boolean = tryCatchSystemManager(false) {
        if (v.requestFocus()) {
            imm.showSoftInput(v, flag)
        } else {
            Logx.e("view requestFocus() is false!!")
            false
        }
    }


    /**
     * Shows soft keyboard for input-capable views with delay.
     * 지연 시간 후 입력 가능한 뷰에 소프트 키보드를 표시합니다.
     *
     * @param v Target view
     * @param delay Delay in milliseconds
     * @param flag Show flag (SHOW_IMPLICIT, SHOW_FORCED deprecated in API 33)
     * @return Boolean true if Runnable was placed in message queue
     */
    public fun showDelay(v: View, delay: Long, flag: Int = InputMethodManager.SHOW_IMPLICIT): Boolean = tryCatchSystemManager(false) {
        v.postDelayed(Runnable { show(v, flag) }, delay)
    }

    /**
     * Shows soft keyboard with coroutine-based delay.
     * 코루틴 기반 지연을 사용하여 소프트 키보드를 표시합니다.
     */
    public fun showDelay(
        v: View,
        delay: Long,
        flag: Int = InputMethodManager.SHOW_IMPLICIT,
        coroutineScope: CoroutineScope
    ): Boolean = tryCatchSystemManager(false) {
        coroutineScope.launch {
            delay(delay)
            show(v, flag)
        }
        true
    }

    /**
     * Hides soft keyboard from input-capable views.
     * Default flag options: HIDE_IMPLICIT_ONLY, HIDE_NOT_ALWAYS
     *
     * 입력 가능한 뷰에서 소프트 키보드를 숨깁니다.
     * 기본 플래그 옵션: HIDE_IMPLICIT_ONLY, HIDE_NOT_ALWAYS
     */
    public fun hide(v: View, flag: Int = 0): Boolean = tryCatchSystemManager(false) {
        if (v.requestFocus()) {
            imm.hideSoftInputFromWindow(v.windowToken, flag)
        } else {
            Logx.e("view requestFocus() is false!!")
            false
        }
    }


    /**
     * Hides soft keyboard from input-capable views with delay.
     * 지연 시간 후 입력 가능한 뷰에서 소프트 키보드를 숨깁니다.
     *
     * @param v Target view
     * @param delay Delay in milliseconds
     * @param flag Hide flag (HIDE_IMPLICIT_ONLY, HIDE_NOT_ALWAYS)
     * @return Boolean true if Runnable was placed in message queue
     */
    public fun hideDelay(v: View, delay: Long, flag: Int = 0): Boolean = tryCatchSystemManager(false) {
        v.postDelayed(Runnable { hide(v, flag) }, delay)
    }

    /**
     * Hides soft keyboard with coroutine-based delay.
     * 코루틴 기반 지연을 사용하여 소프트 키보드를 숨깁니다.
     */
    public fun hideDelay(v: View, delay: Long, flag: Int = 0, coroutineScope: CoroutineScope): Boolean = tryCatchSystemManager(false) {
        coroutineScope.launch {
            delay(delay)
            hide(v, flag)
        }
        true
    }

    /**
     * Starts stylus handwriting mode for the given view.
     * Requires Android 13 (API level 33) or higher.
     *
     * 지정된 뷰에 대해 스타일러스 필기 모드를 시작합니다.
     * Android 13 (API 레벨 33) 이상이 필요합니다.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public fun startStylusHandwriting(v: View): Boolean = tryCatchSystemManager(false) {
        checkSdkVersion(Build.VERSION_CODES.TIRAMISU,
            positiveWork = {
                if (v.requestFocus()) {
                    imm.startStylusHandwriting(v)
                    true
                } else {
                    Logx.e("[ERROR]view requestFocus() is false!!")
                    false
                }
            },
            negativeWork = {
                Logx.e("startStylusHandwriting requires API 33 or higher")
                false
            }
        )
    }

    /**
     * Starts stylus handwriting mode with delay.
     * 지연 시간 후 스타일러스 필기 모드를 시작합니다.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public fun startStylusHandwriting(v: View, delay: Long): Boolean = tryCatchSystemManager(false) {
        checkSdkVersion(Build.VERSION_CODES.TIRAMISU,
            positiveWork = {
                v.postDelayed(Runnable { startStylusHandwriting(v) }, delay)
            },
            negativeWork = {
                Logx.e("startStylusHandwriting requires API 33 or higher")
                false
            }
        )
    }

}