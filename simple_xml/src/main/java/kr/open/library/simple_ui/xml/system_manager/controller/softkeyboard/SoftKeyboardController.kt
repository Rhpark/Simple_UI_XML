package kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard

import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getInputMethodManager

/**
 * Controller for managing soft keyboard operations using InputMethodManager.<br><br>
 * InputMethodManager를 사용하여 소프트 키보드 작업을 관리하는 컨트롤러입니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android's InputMethodManager API requires boilerplate code for focus handling, window token checks, and null safety.<br>
 * - Different Android versions require different approaches for keyboard control (WindowInsetsController on API 30+, legacy flags below).<br>
 * - Developers need both immediate and delayed keyboard operations with proper cancellation support.<br>
 * - Window soft input mode configuration is scattered across manifest and runtime code.<br><br>
 * - Android의 InputMethodManager API는 포커스 처리, 윈도우 토큰 체크, null 안전성을 위한 보일러플레이트 코드가 필요합니다.<br>
 * - 안드로이드 버전마다 키보드 제어 방식이 다릅니다 (API 30+ WindowInsetsController, 이하 레거시 플래그).<br>
 * - 개발자는 즉시 실행과 지연 실행, 그리고 취소 지원이 모두 필요합니다.<br>
 * - 윈도우 소프트 입력 모드 설정이 매니페스트와 런타임 코드에 분산되어 있습니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - **No permission required**: InputMethodManager does not require runtime permissions, so requiredPermissions is null.<br>
 * - **Dual delay mechanisms**: Provides both Runnable-based (postDelayed) and Coroutine-based delay methods for flexibility.<br>
 * - **Job return for cancellation**: Coroutine-based delay methods return Job? to enable cancellation when needed (e.g., view detachment, rapid user actions).<br>
 * - **Method overloading for hideDelay/showDelay**: Same method name with different signatures - Runnable version returns Boolean, Coroutine version returns Job?.<br>
 * - **API-specific branching**: setAdjustResize uses checkSdkVersion to handle API 30+ (WindowCompat) vs legacy (SOFT_INPUT_ADJUST_RESIZE) approaches.<br>
 * - **Safe fallback for window token**: hide() checks both windowToken and applicationWindowToken with clear error logging.<br><br>
 * - **권한 불필요**: InputMethodManager는 런타임 권한이 필요 없으므로 requiredPermissions는 null입니다.<br>
 * - **이중 지연 메커니즘**: 유연성을 위해 Runnable 기반(postDelayed)과 Coroutine 기반 지연 메서드를 모두 제공합니다.<br>
 * - **취소를 위한 Job 반환**: Coroutine 기반 지연 메서드는 필요 시 취소할 수 있도록 Job?을 반환합니다 (예: 뷰 분리, 빠른 사용자 액션).<br>
 * - **hideDelay/showDelay 메서드 오버로딩**: 동일한 메서드명에 다른 시그니처 - Runnable 버전은 Boolean, Coroutine 버전은 Job? 반환.<br>
 * - **API별 분기 처리**: setAdjustResize는 checkSdkVersion을 사용하여 API 30+ (WindowCompat)와 레거시 (SOFT_INPUT_ADJUST_RESIZE) 방식을 처리합니다.<br>
 * - **윈도우 토큰 안전 Fallback**: hide()는 windowToken과 applicationWindowToken을 모두 확인하고 명확한 에러 로깅을 합니다.<br>
 *
 * **Usage / 사용법:**<br>
 * ```kotlin
 * // Get controller instance
 * val controller = context.getSoftKeyboardController()
 *
 * // Basic show/hide
 * controller.show(editText)
 * controller.hide(editText)
 *
 * // Delayed operations - Runnable version (returns Boolean)
 * controller.showDelay(editText, 300)
 * controller.hideDelay(editText, 300)
 *
 * // Delayed operations - Coroutine version with cancellation (returns Job?)
 * val showJob = controller.showDelay(editText, 300, coroutineScope = lifecycleScope)
 * val hideJob = controller.hideDelay(editText, 300, coroutineScope = lifecycleScope)
 * hideJob?.cancel() // Cancel if needed
 *
 * // Window configuration
 * controller.setAdjustPan(window)
 * controller.setAdjustResize(window)
 *
 * // Stylus handwriting (API 33+)
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
 *     controller.startStylusHandwriting(view)
 * }
 * ```<br><br>
 * ```kotlin
 * // 컨트롤러 인스턴스 가져오기
 * val controller = context.getSoftKeyboardController()
 *
 * // 기본 표시/숨김
 * controller.show(editText)
 * controller.hide(editText)
 *
 * // 지연 작업 - Runnable 버전 (Boolean 반환)
 * controller.showDelay(editText, 300)
 * controller.hideDelay(editText, 300)
 *
 * // 지연 작업 - 취소 가능한 Coroutine 버전 (Job? 반환)
 * val showJob = controller.showDelay(editText, 300, coroutineScope = lifecycleScope)
 * val hideJob = controller.hideDelay(editText, 300, coroutineScope = lifecycleScope)
 * hideJob?.cancel() // 필요시 취소
 *
 * // 윈도우 설정
 * controller.setAdjustPan(window)
 * controller.setAdjustResize(window)
 *
 * // 스타일러스 필기 (API 33+)
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
 *     controller.startStylusHandwriting(view)
 * }
 * ```<br>
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
    public fun setAdjustPan(window: Window): Boolean = tryCatchSystemManager(false) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        true
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
    public fun setSoftInputMode(window: Window, softInputTypes: Int): Boolean = tryCatchSystemManager(false) {
        window.setSoftInputMode(softInputTypes)
        true
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
    public fun show(v: View, flag: Int = InputMethodManager.SHOW_IMPLICIT): Boolean = tryCatchSystemManager(false) {
        return if (v.requestFocus()) {
            imm.showSoftInput(v, flag)
        } else {
            Logx.e("SoftKeyboardController: View requestFocus failed")
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
    public fun showDelay(v: View, delay: Long, flag: Int = InputMethodManager.SHOW_IMPLICIT): Boolean = tryCatchSystemManager(false) {
        return v.postDelayed(Runnable { show(v, flag) }, delay)
    }

    /**
     * Shows the soft keyboard using a coroutine-based delay with cancellation support.<br><br>
     * 취소 가능한 코루틴 기반 지연으로 소프트 키보드를 표시합니다.<br>
     *
     * @param v Target view.<br><br>
     *          대상 뷰입니다.<br>
     * @param delay Delay in milliseconds before showing the keyboard.<br><br>
     *              키보드 표시까지 기다릴 지연(ms)입니다.<br>
     * @param flag Show flag for the input method manager.<br><br>
     *             입력기 표시 플래그입니다.<br>
     * @param coroutineScope Scope used to launch the delayed task.<br><br>
     *                       지연 작업을 실행할 코루틴 스코프입니다.<br>
     * @return Job instance that can be used to cancel the delayed operation.<br><br>
     *         지연 작업을 취소할 수 있는 Job 인스턴스를 반환합니다.<br>
     */
    public fun showDelay(v: View, delay: Long, flag: Int = InputMethodManager.SHOW_IMPLICIT, coroutineScope: CoroutineScope): Job? =
        tryCatchSystemManager(null) {
            coroutineScope.launch {
                delay(delay)
                withContext(Dispatchers.Main.immediate) {
                    show(v, flag)
                }
            }
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
     * @return true if the window token was obtained and the hide request was issued.<br><br>
     *         윈도우 토큰을 얻고 숨김 요청을 전달하면 true를 반환합니다.<br>
     */
    public fun hide(v: View, flag: Int = 0): Boolean = tryCatchSystemManager(false) {
        val windowToken = v.windowToken ?: v.applicationWindowToken ?: run {
            Logx.e("SoftKeyboardController: View windowToken is null")
            return false
        }

        imm.hideSoftInputFromWindow(windowToken, flag)
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
    public fun hideDelay(v: View, delay: Long, flag: Int = 0): Boolean = tryCatchSystemManager(false) {
        return v.postDelayed(Runnable { hide(v, flag) }, delay)
    }

    /**
     * Hides the soft keyboard using a coroutine-based delay with cancellation support.<br><br>
     * 취소 가능한 코루틴 기반 지연으로 소프트 키보드를 숨깁니다.<br>
     *
     * @param v Target view.<br><br>
     *          대상 뷰입니다.<br>
     * @param delay Delay in milliseconds before hiding the keyboard.<br><br>
     *              키보드를 숨기기 전 기다릴 지연(ms)입니다.<br>
     * @param flag Hide flag for the input method manager.<br><br>
     *             입력기 숨김 플래그입니다.<br>
     * @param coroutineScope Scope used to launch the delayed task.<br><br>
     *                       지연 작업을 실행할 코루틴 스코프입니다.<br>
     * @return Job instance that can be used to cancel the delayed operation, or null if an error occurs.<br><br>
     *         지연 작업을 취소할 수 있는 Job 인스턴스를 반환하며, 오류 발생 시 null을 반환합니다.<br>
     */
    public fun hideDelay(v: View, delay: Long, flag: Int = 0, coroutineScope: CoroutineScope): Job? = tryCatchSystemManager(null) {
        coroutineScope.launch {
            delay(delay)
            withContext(Dispatchers.Main.immediate) {
                hide(v, flag)
            }
        }
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
    public fun startStylusHandwriting(v: View): Boolean = tryCatchSystemManager(false) {
        return if (v.requestFocus()) {
            imm.startStylusHandwriting(v)
            true
        } else {
            Logx.e("SoftKeyboardController: View requestFocus failed")
            false
        }
    }

    /**
     * Configures the window to adjust resize when the keyboard is shown.<br><br>
     * 키보드가 표시될 때 화면이 resize되도록 윈도우를 설정합니다.<br>
     *
     * On Android 11+ (API 30+), uses WindowCompat for proper edge-to-edge and IME handling.<br>
     * On older versions, uses the legacy SOFT_INPUT_ADJUST_RESIZE flag.<br><br>
     * Android 11 이상에서는 WindowCompat을 사용하여 올바른 edge-to-edge 및 IME 처리를 수행하며,<br>
     * 이전 버전에서는 레거시 SOFT_INPUT_ADJUST_RESIZE 플래그를 사용합니다.<br>
     *
     * @param window Target window to configure.<br><br>
     *               설정할 대상 윈도우입니다.<br>
     * @return true if the mode was set without errors.<br><br>
     *         오류 없이 설정되면 true를 반환합니다.<br>
     */
    public fun setAdjustResize(window: Window): Boolean = tryCatchSystemManager(false) {
        checkSdkVersion(
            Build.VERSION_CODES.R,
            positiveWork = {
                // Android 11+: Use WindowCompat for proper IME resize handling
                // setDecorFitsSystemWindows(true) ensures system handles IME insets automatically
                try {
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                } catch (e: Exception) {
                    Logx.e("SoftKeyboardController: setDecorFitsSystemWindows failed: ${e.message}")
                    return@tryCatchSystemManager false
                }
            },
            negativeWork = {
                // Android 10 and below: Use legacy SOFT_INPUT_ADJUST_RESIZE flag
                @Suppress("DEPRECATION")
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            },
        )
        true
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
    public fun startStylusHandwriting(v: View, delay: Long): Boolean = tryCatchSystemManager(false) {
        return v.postDelayed(Runnable { startStylusHandwriting(v) }, delay)
    }
}
