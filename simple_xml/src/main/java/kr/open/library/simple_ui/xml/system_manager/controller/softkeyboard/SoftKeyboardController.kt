package kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard

import android.content.Context
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getInputMethodManager

/**
 * Controller for managing soft keyboard operations using InputMethodManager and WindowInsets.<br><br>
 * InputMethodManager와 WindowInsets를 사용하여 소프트 키보드 동작을 관리하는 컨트롤러입니다.<br>
 *
 * **Important contract / 핵심 계약:**<br>
 * - `show()`/`hide()` return request-level result only.<br>
 * - `showDelay()`/`hideDelay()` return queue registration result only.<br>
 * - `showAwait()`/`hideAwait()` return actual visibility result (`Success`, `Timeout`, `Failure`).<br><br>
 * - `showAwaitAsync()` returns deferred actual visibility result and never returns null.<br>
 * - `hideAwaitAsync()` returns deferred actual visibility result and may return null on off-main call or scheduling failure.<br><br>
 * - `show()`/`hide()`는 요청 전달 수준 결과만 반환합니다.<br>
 * - `showDelay()`/`hideDelay()`는 큐 등록 성공 여부만 반환합니다.<br>
 * - `showAwait()`/`hideAwait()`는 실제 가시성 결과(`Success`, `Timeout`, `Failure`)를 반환합니다.<br>
 * - `showAwaitAsync()`는 실제 가시성 결과를 담은 Deferred를 반환하며 null을 반환하지 않습니다.<br>
 * - `hideAwaitAsync()`는 실제 가시성 결과를 담은 Deferred를 반환하며 오프메인 호출 또는 스케줄링 실패 시 null을 반환할 수 있습니다.<br>
 */
public open class SoftKeyboardController(
    context: Context,
) : BaseSystemService(context, null) {
    public companion object {
        public const val DEFAULT_IME_VISIBILITY_TIMEOUT_MS: Long = 700L
        private const val IME_FALLBACK_POLL_INTERVAL_MS: Long = 50L
    }

    private val imm: InputMethodManager by lazy { context.getInputMethodManager() }

    private enum class HideRequestResult {
        REQUEST_ISSUED,
        WINDOW_CONTEXT_MISSING,
        REQUEST_REJECTED,
    }

    /**
     * Sets window soft input mode to adjust pan.<br><br>
     * 윈도우 소프트 입력 모드를 adjust pan으로 설정합니다.<br>
     */
    @MainThread
    public fun setAdjustPan(window: Window): Boolean = tryCatchSystemManager(false) {
        if (!ensureMainThread("setAdjustPan")) return@tryCatchSystemManager false
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        true
    }

    /**
     * Sets custom soft input mode for the window.<br><br>
     * 윈도우에 사용자 정의 소프트 입력 모드를 설정합니다.<br>
     */
    @MainThread
    public fun setSoftInputMode(window: Window, softInputTypes: Int): Boolean =
        tryCatchSystemManager(false) {
            if (!ensureMainThread("setSoftInputMode")) return@tryCatchSystemManager false
            window.setSoftInputMode(softInputTypes)
            true
        }

    /**
     * Shows the soft keyboard for input-capable views.<br><br>
     * 입력 가능한 뷰에 소프트 키보드 표시를 요청합니다. 실제 가시성 확인이 필요하면 `showAwait*`를 사용하세요.<br>
     */
    @MainThread
    public fun show(v: View, flag: Int = InputMethodManager.SHOW_IMPLICIT): Boolean =
        tryCatchSystemManager(false) {
            if (!ensureMainThread("show")) return@tryCatchSystemManager false
            if (!v.requestFocus()) {
                Logx.e("SoftKeyboardController: View requestFocus failed")
                return@tryCatchSystemManager false
            }
            requestShowInternal(v, flag)
        }

    /**
     * Schedules delayed keyboard show and returns queue registration status.<br><br>
     * 지연 키보드 표시를 예약하고 메시지 큐 등록 결과를 반환합니다. 실제 표시 결과는 포함하지 않습니다.<br>
     */
    @MainThread
    public fun showDelay(
        v: View,
        delay: Long,
        flag: Int = InputMethodManager.SHOW_IMPLICIT
    ): Boolean = tryCatchSystemManager(false) {
        if (!ensureMainThread("showDelay")) return@tryCatchSystemManager false
        if (delay < 0L) {
            Logx.w("SoftKeyboardController: showDelay delay must be >= 0")
            return@tryCatchSystemManager false
        }
        v.postDelayed(Runnable { show(v, flag) }, delay)
    }

    /**
     * Shows keyboard and waits for actual IME visibility result.<br><br>
     * 키보드 표시를 요청하고 실제 IME 가시성 결과까지 대기합니다. `Timeout`은 제한 시간 내 미관측을 의미하며 영구 실패를 뜻하지는 않습니다.<br>
     */
    @MainThread
    public suspend fun showAwait(
        v: View,
        delayMillis: Long = 0L,
        flag: Int = InputMethodManager.SHOW_IMPLICIT,
        timeoutMillis: Long = DEFAULT_IME_VISIBILITY_TIMEOUT_MS,
    ): SoftKeyboardActionResult {
        if (!ensureMainThread("showAwait")) {
            return SoftKeyboardActionResult.Failure(
                reason = SoftKeyboardFailureReason.OFF_MAIN_THREAD,
                message = "showAwait must be called on Main thread.",
            )
        }
        if (delayMillis < 0L || timeoutMillis <= 0L) {
            return SoftKeyboardActionResult.Failure(
                reason = SoftKeyboardFailureReason.INVALID_ARGUMENT,
                message = "delayMillis must be >= 0 and timeoutMillis must be > 0.",
            )
        }

        return try {
            if (delayMillis > 0L) delay(delayMillis)
            if (!v.requestFocus()) {
                return SoftKeyboardActionResult.Failure(
                    reason = SoftKeyboardFailureReason.FOCUS_REQUEST_FAILED,
                    message = "View requestFocus failed.",
                )
            }

            val requestIssued = requestShowInternal(v, flag)
            if (!requestIssued) {
                return SoftKeyboardActionResult.Failure(
                    reason = SoftKeyboardFailureReason.IME_REQUEST_REJECTED,
                    message = "IME show request was rejected.",
                )
            }

            if (awaitImeVisibility(v, expectedVisible = true, timeoutMillis = timeoutMillis)) {
                SoftKeyboardActionResult.Success
            } else {
                SoftKeyboardActionResult.Timeout
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logx.e("SoftKeyboardController: showAwait failed: ${e.message}")
            SoftKeyboardActionResult.Failure(
                reason = SoftKeyboardFailureReason.EXCEPTION_OCCURRED,
                message = e.message,
            )
        }
    }

    /**
     * Async wrapper of [showAwait]. Returns a non-null Deferred result that can be cancelled.<br><br>
     * [showAwait]의 비동기 래퍼입니다. 취소 가능한 non-null Deferred 결과를 반환합니다.<br>
     */
    @MainThread
    public fun showAwaitAsync(
        v: View,
        coroutineScope: CoroutineScope,
        delayMillis: Long = 0L,
        flag: Int = InputMethodManager.SHOW_IMPLICIT,
        timeoutMillis: Long = DEFAULT_IME_VISIBILITY_TIMEOUT_MS,
    ): Deferred<SoftKeyboardActionResult> =
        tryCatchSystemManager(
            defaultValue = createFailureDeferred(
                reason = SoftKeyboardFailureReason.EXCEPTION_OCCURRED,
                message = "showAwaitAsync failed before scheduling.",
            )
        ) {
            if (!ensureMainThread("showAwaitAsync")) {
                return@tryCatchSystemManager createFailureDeferred(
                    reason = SoftKeyboardFailureReason.OFF_MAIN_THREAD,
                    message = "showAwaitAsync must be called on Main thread.",
                )
            }
            coroutineScope.async(Dispatchers.Main.immediate) {
                showAwait(v, delayMillis, flag, timeoutMillis)
            }
        }

    /**
     * Hides the soft keyboard from input-capable views.<br><br>
     * 입력 가능한 뷰에서 소프트 키보드 숨김을 요청합니다. 실제 가시성 확인이 필요하면 `hideAwait*`를 사용하세요.<br>
     */
    @MainThread
    public fun hide(v: View, flag: Int = 0): Boolean = tryCatchSystemManager(false) {
        if (!ensureMainThread("hide")) return@tryCatchSystemManager false
        requestHideInternal(v, flag) == HideRequestResult.REQUEST_ISSUED
    }

    /**
     * Schedules delayed keyboard hide and returns queue registration status.<br><br>
     * 지연 키보드 숨김을 예약하고 메시지 큐 등록 결과를 반환합니다. 실제 숨김 결과는 포함하지 않습니다.<br>
     */
    @MainThread
    public fun hideDelay(v: View, delay: Long, flag: Int = 0): Boolean =
        tryCatchSystemManager(false) {
            if (!ensureMainThread("hideDelay")) return@tryCatchSystemManager false
            if (delay < 0L) {
                Logx.w("SoftKeyboardController: hideDelay delay must be >= 0")
                return@tryCatchSystemManager false
            }
            v.postDelayed(Runnable { hide(v, flag) }, delay)
        }

    /**
     * Hides keyboard and waits for actual IME visibility result.<br><br>
     * 키보드 숨김을 요청하고 실제 IME 가시성 결과까지 대기합니다. `Timeout`은 제한 시간 내 미관측을 의미하며 영구 실패를 뜻하지는 않습니다.<br>
     */
    @MainThread
    public suspend fun hideAwait(
        v: View,
        delayMillis: Long = 0L,
        flag: Int = 0,
        timeoutMillis: Long = DEFAULT_IME_VISIBILITY_TIMEOUT_MS,
    ): SoftKeyboardActionResult {
        if (!ensureMainThread("hideAwait")) {
            return SoftKeyboardActionResult.Failure(
                reason = SoftKeyboardFailureReason.OFF_MAIN_THREAD,
                message = "hideAwait must be called on Main thread.",
            )
        }
        if (delayMillis < 0L || timeoutMillis <= 0L) {
            return SoftKeyboardActionResult.Failure(
                reason = SoftKeyboardFailureReason.INVALID_ARGUMENT,
                message = "delayMillis must be >= 0 and timeoutMillis must be > 0.",
            )
        }

        return try {
            if (delayMillis > 0L) delay(delayMillis)
            when (requestHideInternal(v, flag)) {
                HideRequestResult.REQUEST_ISSUED -> Unit
                HideRequestResult.WINDOW_CONTEXT_MISSING -> {
                    return SoftKeyboardActionResult.Failure(
                        reason = SoftKeyboardFailureReason.WINDOW_TOKEN_MISSING,
                        message = "Window token and WindowInsetsController are unavailable.",
                    )
                }
                HideRequestResult.REQUEST_REJECTED -> {
                    return SoftKeyboardActionResult.Failure(
                        reason = SoftKeyboardFailureReason.IME_REQUEST_REJECTED,
                        message = "IME hide request was rejected.",
                    )
                }
            }

            if (awaitImeVisibility(v, expectedVisible = false, timeoutMillis = timeoutMillis)) {
                SoftKeyboardActionResult.Success
            } else {
                SoftKeyboardActionResult.Timeout
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logx.e("SoftKeyboardController: hideAwait failed: ${e.message}")
            SoftKeyboardActionResult.Failure(
                reason = SoftKeyboardFailureReason.EXCEPTION_OCCURRED,
                message = e.message,
            )
        }
    }

    /**
     * Async wrapper of [hideAwait]. Returns Deferred result that can be cancelled.<br><br>
     * [hideAwait]의 비동기 래퍼입니다. 취소 가능한 Deferred 결과를 반환합니다.<br>
     */
    @MainThread
    public fun hideAwaitAsync(
        v: View,
        coroutineScope: CoroutineScope,
        delayMillis: Long = 0L,
        flag: Int = 0,
        timeoutMillis: Long = DEFAULT_IME_VISIBILITY_TIMEOUT_MS,
    ): Deferred<SoftKeyboardActionResult>? = tryCatchSystemManager(null) {
        if (!ensureMainThread("hideAwaitAsync")) return@tryCatchSystemManager null
        coroutineScope.async(Dispatchers.Main.immediate) {
            hideAwait(v, delayMillis, flag, timeoutMillis)
        }
    }

    /**
     * Starts stylus handwriting mode for the given view.<br><br>
     * 지정된 뷰에 대해 스타일러스 필기 모드를 시작합니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @MainThread
    public fun startStylusHandwriting(v: View): Boolean = tryCatchSystemManager(false) {
        if (!ensureMainThread("startStylusHandwriting")) return@tryCatchSystemManager false
        if (v.requestFocus()) {
            imm.startStylusHandwriting(v)
            true
        } else {
            Logx.e("SoftKeyboardController: View requestFocus failed")
            false
        }
    }

    /**
     * Configures resize behavior with explicit policy.<br><br>
     * 명시적 정책으로 키보드 resize 동작을 설정합니다.<br>
     */
    @MainThread
    public fun configureImeResize(
        window: Window,
        policy: SoftKeyboardResizePolicy = SoftKeyboardResizePolicy.KEEP_CURRENT_WINDOW,
    ): Boolean = tryCatchSystemManager(false) {
        if (!ensureMainThread("configureImeResize")) return@tryCatchSystemManager false

        when (policy) {
            SoftKeyboardResizePolicy.KEEP_CURRENT_WINDOW -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Keep existing edge-to-edge / decorFits state on API 30+
                    true
                } else {
                    @Suppress("DEPRECATION")
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    true
                }
            }

            SoftKeyboardResizePolicy.LEGACY_ADJUST_RESIZE -> {
                @Suppress("DEPRECATION")
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                true
            }

            SoftKeyboardResizePolicy.FORCE_DECOR_FITS_TRUE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                    true
                } else {
                    @Suppress("DEPRECATION")
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    true
                }
            }
        }
    }

    /**
     * Configures keyboard resize behavior with safe default policy.<br><br>
     * 안전한 기본 정책으로 키보드 resize 동작을 설정합니다.<br>
     *
     * Default behavior:
     * - API 30+: keep current window policy (no forced decorFits change)
     * - API 29-: apply legacy adjust resize<br><br>
     * 기본 동작:
     * - API 30+: 현재 윈도우 정책 유지(강제 decorFits 변경 없음)
     * - API 29-: 레거시 adjust resize 적용<br>
     */
    @MainThread
    public fun setAdjustResize(window: Window): Boolean =
        configureImeResize(window, SoftKeyboardResizePolicy.KEEP_CURRENT_WINDOW)

    /**
     * Starts stylus handwriting mode after a delay.<br><br>
     * 지연 시간 후 스타일러스 필기 모드를 시작합니다.<br>
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @MainThread
    public fun startStylusHandwriting(v: View, delay: Long): Boolean =
        tryCatchSystemManager(false) {
            if (!ensureMainThread("startStylusHandwriting(delay)")) return@tryCatchSystemManager false
            if (delay < 0L) {
                Logx.w("SoftKeyboardController: startStylusHandwriting delay must be >= 0")
                return@tryCatchSystemManager false
            }
            v.postDelayed(Runnable { startStylusHandwriting(v) }, delay)
        }

    /**
     * Waits IME visibility using layout callback first and fallback polling second.<br><br>
     * 레이아웃 콜백을 우선 사용하고, 폴백 폴링을 보조로 사용해 IME 가시성 변화를 대기합니다.<br>
     */
    @MainThread
    private suspend fun awaitImeVisibility(
        v: View,
        expectedVisible: Boolean,
        timeoutMillis: Long
    ): Boolean {
        if (isImeVisible(v) == expectedVisible) return true

        val callbackObserved = CompletableDeferred<Unit>()
        val observer = v.viewTreeObserver
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            if (!callbackObserved.isCompleted && isImeVisible(v) == expectedVisible) {
                callbackObserved.complete(Unit)
            }
        }

        if (observer.isAlive) {
            observer.addOnGlobalLayoutListener(listener)
        }

        val start = SystemClock.elapsedRealtime()
        try {
            while (SystemClock.elapsedRealtime() - start <= timeoutMillis) {
                if (isImeVisible(v) == expectedVisible) return true

                val remaining = timeoutMillis - (SystemClock.elapsedRealtime() - start)
                if (remaining <= 0L) break

                val waitMillis = minOf(IME_FALLBACK_POLL_INTERVAL_MS, remaining)
                val callbackHit = withTimeoutOrNull(waitMillis) {
                    callbackObserved.await()
                    true
                } ?: false

                if (callbackHit) return true
            }
            return false
        } finally {
            if (observer.isAlive) {
                observer.removeOnGlobalLayoutListener(listener)
            }
        }
    }

    /**
     * Issues keyboard show request through WindowInsets controller and IMM fallback.<br><br>
     * WindowInsets 컨트롤러 경로와 IMM 폴백 경로를 통해 키보드 표시 요청을 전달합니다.<br>
     */
    @MainThread
    private fun requestShowInternal(v: View, flag: Int): Boolean {
        val controller = ViewCompat.getWindowInsetsController(v)
        controller?.show(WindowInsetsCompat.Type.ime())

        val immResult = imm.showSoftInput(v, flag)
        return controller != null || immResult
    }

    /**
     * Issues keyboard hide request through WindowInsets controller and IMM fallback.<br><br>
     * WindowInsets 컨트롤러 경로와 IMM 폴백 경로를 통해 키보드 숨김 요청을 전달합니다.<br>
     */
    @MainThread
    private fun requestHideInternal(v: View, flag: Int): HideRequestResult {
        val controller = ViewCompat.getWindowInsetsController(v)
        controller?.hide(WindowInsetsCompat.Type.ime())

        val windowToken = v.windowToken ?: v.applicationWindowToken
        if (windowToken == null && controller == null) {
            Logx.e("SoftKeyboardController: View windowToken and WindowInsetsController are null")
            return HideRequestResult.WINDOW_CONTEXT_MISSING
        }

        val immResult = if (windowToken != null) {
            imm.hideSoftInputFromWindow(windowToken, flag)
        } else {
            false
        }
        return if (controller != null || immResult) {
            HideRequestResult.REQUEST_ISSUED
        } else {
            HideRequestResult.REQUEST_REJECTED
        }
    }

    /**
     * Checks current IME visibility from root window insets.<br><br>
     * 루트 윈도우 insets를 기준으로 현재 IME 가시성을 확인합니다.<br>
     */
    @MainThread
    private fun isImeVisible(v: View): Boolean =
        ViewCompat.getRootWindowInsets(v)?.isVisible(WindowInsetsCompat.Type.ime()) == true

    /**
     * Validates that the current call is running on Main thread.<br><br>
     * 현재 호출이 메인 스레드에서 실행 중인지 검증합니다.<br>
     */
    private fun ensureMainThread(methodName: String): Boolean {
        if (Looper.myLooper() == Looper.getMainLooper()) return true
        Logx.w("SoftKeyboardController: $methodName must be called on Main thread")
        return false
    }

    private fun createFailureDeferred(
        reason: SoftKeyboardFailureReason,
        message: String? = null,
    ): Deferred<SoftKeyboardActionResult> =
        CompletableDeferred(
            SoftKeyboardActionResult.Failure(
                reason = reason,
                message = message,
            )
        )
}
