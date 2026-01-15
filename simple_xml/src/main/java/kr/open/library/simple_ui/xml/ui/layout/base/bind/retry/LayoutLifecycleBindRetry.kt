package kr.open.library.simple_ui.xml.ui.layout.base.bind.retry

import android.view.View
import androidx.lifecycle.LifecycleOwner
import kr.open.library.simple_ui.xml.extensions.view.doOnLayout

/**
 * Retry helper that binds a LifecycleOwner with a layout-first strategy and delayed retries.<br><br>
 * 레이아웃 완료 후 우선 바인딩을 시도하고, 실패 시 지연 재시도를 수행하는 헬퍼입니다.<br>
 *
 * @param view The target view used to post callbacks and check attach state.<br><br>
 *             콜백 게시와 attach 상태 확인에 사용하는 대상 View입니다.<br>
 * @param callbacks Callback interface that performs the bind operation.<br><br>
 *                  바인딩 동작을 수행하는 콜백 인터페이스입니다.<br>
 * @param maxRetry Maximum number of delayed retry attempts.<br><br>
 *                 지연 재시도 최대 횟수입니다.<br>
 * @param retryDelayMs Delay in milliseconds between retry attempts.<br><br>
 *                     재시도 간 지연 시간(ms)입니다.<br>
 */
internal class LayoutLifecycleBindRetry(
    private val view: View,
    private val callbacks: LayoutLifecycleBindRetryCallbacks,
    private val maxRetry: Int = 3,
    private val retryDelayMs: Long = 50L,
) {
    /**
     * Tracks how many delayed retries have been attempted.<br><br>
     * 지연 재시도 횟수를 추적합니다.<br>
     */
    private var retryCount = 0

    /**
     * Prevents duplicate start calls for the same attach cycle.<br><br>
     * 동일한 attach 주기에서 중복 시작을 방지합니다.<br>
     */
    private var started = false

    /**
     * Runnable that performs delayed retry attempts after layout fallback fails.<br><br>
     * 레이아웃 우선 시도 실패 후 지연 재시도를 수행하는 Runnable입니다.<br>
     */
    private val retryRunnable = object : Runnable {
        override fun run() {
            if (!started) return
            if (!view.isAttachedToWindow) return
            val owner = callbacks.bind()
            if (owner != null) return

            if (retryCount < maxRetry) {
                retryCount++
                view.postDelayed(this, retryDelayMs)
            }
        }
    }

    /**
     * Starts binding with a layout-first attempt and then delayed retries.<br><br>
     * 레이아웃 우선 시도 후 지연 재시도로 바인딩을 시작합니다.<br>
     */
    fun start() {
        if (started) return
        started = true

        view.doOnLayout {

            if (!view.isAttachedToWindow) return@doOnLayout
            val retryOwner = callbacks.bind()
            if (retryOwner != null) return@doOnLayout

            retryCount = 0
            view.postDelayed(retryRunnable, retryDelayMs)
        }
    }

    /**
     * Cancels pending retries and resets internal state.<br><br>
     * 대기 중인 재시도를 취소하고 내부 상태를 초기화합니다.<br>
     */
    fun cancel() {
        view.removeCallbacks(retryRunnable)
        retryCount = 0
        started = false
    }
}

