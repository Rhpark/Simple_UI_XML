package kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard

import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getInputMethodManager

/**
 * Controller for managing soft keyboard operations using InputMethodManager.<br><br>
 * InputMethodManager를 사용하여 소프트 키보드 작업을 관리하는 컨트롤러입니다.<br>
 *
 * Provides comprehensive keyboard show/hide functionality with error handling.<br><br>
 * 오류 처리와 함께 포괄적인 키보드 표시/숨김 기능을 제공합니다.<br>
 *
 * @see <a href="https://blog.naver.com/il7942li/222671675950">windowSoftInputMode reference</a>
 */
public open class SoftKeyboardController(
    context: Context,
) : BaseSystemService(context, null) {
    public val imm: InputMethodManager by lazy { context.getInputMethodManager() }

    /**
     * Sets window soft input mode to adjust pan.<br><br>
     * 윈도우 소프트 입력 모드를 adjust pan으로 설정합니다.<br>
     *
     * Can be configured in manifest: `android:windowSoftInputMode="adjustPan"`.<br><br>
     * 매니페스트에서도 `android:windowSoftInputMode="adjustPan"`으로 설정할 수 있습니다.<br>
     *
     * @param window Target window to configure.<br><br>
     *               설정할 대상 윈도우입니다.<br>
     * @return true if the mode was set without errors.<br><br>
     *         오류 없이 설정되면 true를 반환합니다.<br>
     */
    public fun setAdjustPan(window: Window): Boolean =
        tryCatchSystemManager(false) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            return true
        }

    /**
     * Sets custom soft input mode for the window.<br><br>
     * 윈도우에 사용자 정의 소프트 입력 모드를 설정합니다.<br>
     *
     * Can be configured in manifest: `android:windowSoftInputMode="..."`.<br><br>
     * 매니페스트에서도 `android:windowSoftInputMode="..."`로 설정할 수 있습니다.<br>
     *
     * @param window Target window.<br><br>
     *               대상 윈도우입니다.<br>
     * @param softInputTypes Input mode flags (e.g., `SOFT_INPUT_ADJUST_PAN`, `SOFT_INPUT_MASK_STATE`).<br><br>
     *                       설정할 입력 모드 플래그입니다.<br>
     * @return true if the mode was set without errors.<br><br>
     *         오류 없이 설정되면 true를 반환합니다.<br>
     */
    public fun setSoftInputMode(
        window: Window,
        softInputTypes: Int,
    ): Boolean =
        tryCatchSystemManager(false) {
            window.setSoftInputMode(softInputTypes)
            return true
        }

    /**
     * Shows the soft keyboard for input-capable views (EditText, SearchView, etc.).<br><br>
     * 입력 가능한 뷰(EditText, SearchView 등)에 소프트 키보드를 표시합니다.<br>
     *
     * Default flag options: `SHOW_IMPLICIT`, `SHOW_FORCED` (deprecated in API 33).<br><br>
     * 기본 플래그: `SHOW_IMPLICIT`, `SHOW_FORCED`(API 33에서 deprecated).<br>
     *
     * @param v Target view to receive focus and keyboard.<br><br>
     *          포커스와 키보드를 받을 대상 뷰입니다.<br>
     * @param flag Show flag for the input method manager.<br><br>
     *             입력기 표시 플래그입니다.<br>
     * @return true if focus was obtained and the request was issued.<br><br>
     *         포커스를 얻고 요청을 전달하면 true를 반환합니다.<br>
     */
    public fun show(
        v: View,
        flag: Int = InputMethodManager.SHOW_IMPLICIT,
    ): Boolean =
        tryCatchSystemManager(false) {
            return if (v.requestFocus()) {
                imm.showSoftInput(v, flag)
            } else {
                Logx.e("view requestFocus() is false!!")
                false
            }
        }

    /**
     * Shows the soft keyboard for input-capable views after a delay.<br><br>
     * 지연 시간 뒤 입력 가능한 뷰에 소프트 키보드를 표시합니다.<br>
     *
     * @param v Target view.<br><br>
     *          대상 뷰입니다.<br>
     * @param delay Delay in milliseconds before showing the keyboard.<br><br>
     *              키보드 표시까지 기다릴 지연(ms)입니다.<br>
     * @param flag Show flag (`SHOW_IMPLICIT`, `SHOW_FORCED` deprecated in API 33).<br><br>
     *             표시 플래그입니다(`SHOW_IMPLICIT`, `SHOW_FORCED`는 API 33에서 deprecated).<br>
     * @return true if the runnable was posted to the message queue.<br><br>
     *         Runnable이 메시지 큐에 등록되면 true를 반환합니다.<br>
     */
    public fun showDelay(
        v: View,
        delay: Long,
        flag: Int = InputMethodManager.SHOW_IMPLICIT,
    ): Boolean =
        tryCatchSystemManager(false) {
            return v.postDelayed(Runnable { show(v, flag) }, delay)
        }

    /**
     * Shows the soft keyboard using a coroutine-based delay.<br><br>
     * 코루틴 기반 지연으로 소프트 키보드를 표시합니다.<br>
     *
     * @param v Target view.<br><br>
     *          대상 뷰입니다.<br>
     * @param delay Delay in milliseconds before showing the keyboard.<br><br>
     *              키보드 표시까지 기다릴 지연(ms)입니다.<br>
     * @param flag Show flag for the input method manager.<br><br>
     *             입력기 표시 플래그입니다.<br>
     * @param coroutineScope Scope used to launch the delayed task.<br><br>
     *                       지연 작업을 실행할 코루틴 스코프입니다.<br>
     * @return true after scheduling the coroutine.<br><br>
     *         코루틴 예약을 완료하면 true를 반환합니다.<br>
     */
    public fun showDelay(
        v: View,
        delay: Long,
        flag: Int = InputMethodManager.SHOW_IMPLICIT,
        coroutineScope: CoroutineScope,
    ): Boolean =
        tryCatchSystemManager(false) {
            coroutineScope.launch {
                delay(delay)
                show(v, flag)
            }
            return true
        }

    /**
     * Hides the soft keyboard from input-capable views.<br><br>
     * 입력 가능한 뷰에서 소프트 키보드를 숨깁니다.<br>
     *
     * Default flag options: `HIDE_IMPLICIT_ONLY`, `HIDE_NOT_ALWAYS`. <br><br>
     * 기본 플래그: `HIDE_IMPLICIT_ONLY`, `HIDE_NOT_ALWAYS`.<br>
     *
     * @param v Target view whose window token will be used.<br><br>
     *          윈도우 토큰을 가져올 대상 뷰입니다.<br>
     * @param flag Hide flag for the input method manager.<br><br>
     *             입력기 숨김 플래그입니다.<br>
     * @return true if focus was obtained and the request was issued.<br><br>
     *         포커스를 얻고 요청을 전달하면 true를 반환합니다.<br>
     */
    public fun hide(
        v: View,
        flag: Int = 0,
    ): Boolean =
        tryCatchSystemManager(false) {
            return if (v.requestFocus()) {
                imm.hideSoftInputFromWindow(v.windowToken, flag)
            } else {
                Logx.e("view requestFocus() is false!!")
                false
            }
        }

    /**
     * Hides the soft keyboard from input-capable views after a delay.<br><br>
     * 지연 시간 뒤 입력 가능한 뷰에서 소프트 키보드를 숨깁니다.<br>
     *
     * @param v Target view.<br><br>
     *          대상 뷰입니다.<br>
     * @param delay Delay in milliseconds before hiding the keyboard.<br><br>
     *              키보드를 숨기기 전 기다릴 지연(ms)입니다.<br>
     * @param flag Hide flag (`HIDE_IMPLICIT_ONLY`, `HIDE_NOT_ALWAYS`).<br><br>
     *             숨김 플래그입니다(`HIDE_IMPLICIT_ONLY`, `HIDE_NOT_ALWAYS`).<br>
     * @return true if the runnable was posted to the message queue.<br><br>
     *         Runnable이 메시지 큐에 등록되면 true를 반환합니다.<br>
     */
    public fun hideDelay(
        v: View,
        delay: Long,
        flag: Int = 0,
    ): Boolean =
        tryCatchSystemManager(false) {
            return v.postDelayed(Runnable { hide(v, flag) }, delay)
        }

    /**
     * Hides the soft keyboard using a coroutine-based delay.<br><br>
     * 코루틴 기반 지연으로 소프트 키보드를 숨깁니다.<br>
     *
     * @param v Target view.<br><br>
     *          대상 뷰입니다.<br>
     * @param delay Delay in milliseconds before hiding the keyboard.<br><br>
     *              키보드를 숨기기 전 기다릴 지연(ms)입니다.<br>
     * @param flag Hide flag for the input method manager.<br><br>
     *             입력기 숨김 플래그입니다.<br>
     * @param coroutineScope Scope used to launch the delayed task.<br><br>
     *                       지연 작업을 실행할 코루틴 스코프입니다.<br>
     * @return true after scheduling the coroutine.<br><br>
     *         코루틴 예약을 완료하면 true를 반환합니다.<br>
     */
    public fun hideDelay(
        v: View,
        delay: Long,
        flag: Int = 0,
        coroutineScope: CoroutineScope,
    ): Boolean =
        tryCatchSystemManager(false) {
            coroutineScope.launch {
                delay(delay)
                hide(v, flag)
            }
            return true
        }

    /**
     * Starts stylus handwriting mode for the given view.<br><br>
     * 지정된 뷰에 대해 스타일러스 필기 모드를 시작합니다.<br>
     *
     * Requires Android 13 (API level 33) or higher.<br><br>
     * Android 13(API 레벨 33) 이상에서만 동작합니다.<br>
     *
     * @param v Target view that will enter handwriting mode.<br><br>
     *          필기 모드를 적용할 대상 뷰입니다.<br>
     * @return true if focus was obtained and the request was issued.<br><br>
     *         포커스를 얻고 요청을 전달하면 true를 반환합니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public fun startStylusHandwriting(v: View): Boolean =
        tryCatchSystemManager(false) {
            return if (v.requestFocus()) {
                imm.startStylusHandwriting(v)
                true
            } else {
                Logx.e("[ERROR]view requestFocus() is false!!")
                false
            }
        }

    /**
     * Configures the window to adjust resize, using WindowInsetsController on Android 11+ and legacy flags otherwise.<br><br>
     * Android 11 이상에서는 WindowInsetsController로 IME 동작을 제어하고, 그 미만에서는 기존 adjustResize 플래그를 적용합니다.<br>
     *
     * @param window Target window to configure.<br><br>
     *               설정할 대상 윈도우입니다.<br>
     */
    public fun setAdjustResize(window: Window) {
        checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = {
                // Android 11+: rely on WindowInsetsController
                val controller = window.insetsController
                if (controller != null) {
                    // Let system adjust layout when IME is shown
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    // Fallback to WindowCompat when controller is unavailable
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                }
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            },
        )
    }

    /**
     * Starts stylus handwriting mode after a delay.<br><br>
     * 지연 시간 후 스타일러스 필기 모드를 시작합니다.<br>
     *
     * @param v Target view that will enter handwriting mode.<br><br>
     *          필기 모드를 적용할 대상 뷰입니다.<br>
     * @param delay Delay in milliseconds before starting handwriting mode.<br><br>
     *              필기 모드를 시작하기 전 기다릴 지연(ms)입니다.<br>
     * @return true if the runnable was posted to the message queue.<br><br>
     *         Runnable이 메시지 큐에 등록되면 true를 반환합니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public fun startStylusHandwriting(
        v: View,
        delay: Long,
    ): Boolean =
        tryCatchSystemManager(false) {
            return v.postDelayed(Runnable { startStylusHandwriting(v) }, delay)
        }
}
