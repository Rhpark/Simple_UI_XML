package kr.open.library.simple_ui.xml.ui.adapter.common.thread

import android.os.Looper

/**
 * Runtime main-thread guard for adapter public APIs.<br><br>
 * 어댑터 공개 API용 런타임 메인 스레드 가드입니다.<br>
 *
 * This guard fails fast with [IllegalStateException] when called off the main thread.<br><br>
 * 메인 스레드가 아닌 곳에서 호출되면 [IllegalStateException]으로 즉시 실패합니다.<br>
 *
 * @param apiName Human-readable API name for error messages.<br><br>
 *                오류 메시지에 표시할 API 이름입니다.<br>
 */
internal fun assertAdapterMainThread(apiName: String) {
    check(Looper.myLooper() == Looper.getMainLooper()) {
        "$apiName must be called on Main thread"
    }
}
