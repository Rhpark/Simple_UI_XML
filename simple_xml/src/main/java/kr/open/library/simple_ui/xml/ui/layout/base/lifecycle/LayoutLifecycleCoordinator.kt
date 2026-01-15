package kr.open.library.simple_ui.xml.ui.layout.base.lifecycle

import android.view.View
import androidx.lifecycle.LifecycleOwner
import kr.open.library.simple_ui.xml.ui.layout.base.bind.retry.LayoutLifecycleBindRetry
import kr.open.library.simple_ui.xml.ui.layout.base.bind.retry.LayoutLifecycleBindRetryCallbacks

/**
 * Coordinates lifecycle observer binding for views.<br><br>
 * 뷰의 라이프사이클 옵저버 바인딩을 조정합니다.<br>
 *
 * @param view The target view used for retry scheduling and attach checks.<br><br>
 *             재시도 스케줄링과 attach 확인에 사용하는 대상 View입니다.<br>
 * @param callbacks Callback interface that binds and unbinds lifecycle observers.<br><br>
 *                  라이프사이클 옵저버를 바인딩/해제하는 콜백 인터페이스입니다.<br>
 * @param maxRetry Maximum number of delayed retry attempts.<br><br>
 *                 지연 재시도 최대 횟수입니다.<br>
 * @param retryDelayMs Delay in milliseconds between retry attempts.<br><br>
 *                     재시도 간 지연 시간(ms)입니다.<br>
 */
internal class LayoutLifecycleCoordinator(
    private val view: View,
    private val callbacks: LayoutLifecycleCallbacks,
    maxRetry: Int = 3,
    retryDelayMs: Long = 50L,
) {
    private val retry = LayoutLifecycleBindRetry(
        view = view,
        callbacks = object : LayoutLifecycleBindRetryCallbacks {
            override fun bind(): LifecycleOwner? = callbacks.bindLifecycle()
        },
        maxRetry = maxRetry,
        retryDelayMs = retryDelayMs,
    )

    /**
     * Starts lifecycle binding with retry support on attach.<br><br>
     * attach 시 재시도 지원과 함께 라이프사이클 바인딩을 시작합니다.<br>
     */
    fun onAttach() {
        retry.start()
    }

    /**
     * Cancels retries and unbinds lifecycle observers on detach.<br><br>
     * detach 시 재시도를 취소하고 라이프사이클 옵저버를 해제합니다.<br>
     */
    fun onDetach() {
        retry.cancel()
        callbacks.unbindLifecycle()
    }
}
