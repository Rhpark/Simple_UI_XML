package kr.open.library.simple_ui.xml.ui.layout.base.bind.retry

import androidx.lifecycle.LifecycleOwner

/**
 * Callback interface for LifecycleBindRetry binding attempts.<br><br>
 * LifecycleBindRetry 바인딩 시도를 위한 콜백 인터페이스입니다.<br>
 */
internal interface LayoutLifecycleBindRetryCallbacks {
    /**
     * Attempts to bind and returns a LifecycleOwner when successful.<br><br>
     * 바인딩을 시도하고 성공 시 LifecycleOwner를 반환합니다.<br>
     *
     * @return The bound LifecycleOwner, or null if binding failed.<br><br>
     *         바인딩 성공 시 LifecycleOwner, 실패 시 null을 반환합니다.<br>
     */
    fun bind(): LifecycleOwner?
}
