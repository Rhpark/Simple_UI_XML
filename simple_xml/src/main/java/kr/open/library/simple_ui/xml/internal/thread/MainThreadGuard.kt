package kr.open.library.simple_ui.xml.internal.thread

import android.os.Looper
import kr.open.library.simple_ui.xml.BuildConfig

/**
 * Runtime main-thread assertion helper used together with `@MainThread` contracts.<br><br>
 * `@MainThread` 계약과 함께 사용하는 런타임 메인 스레드 검증 헬퍼입니다.<br>
 *
 * `@MainThread` is a static lint/IDE contract, while this helper enforces the rule at runtime in Debug builds.<br><br>
 * `@MainThread`는 정적(lint/IDE) 계약이고, 이 헬퍼는 Debug 빌드에서 런타임으로 규칙을 강제합니다.<br>
 *
 * **Why not `@MainThread` only / `@MainThread`만 단독 사용하지 않는 이유:**<br>
 * - Lint can be disabled or warnings can be missed at call sites.<br>
 * - Library consumers may call APIs from worker threads at runtime.<br><br>
 * - 호출부에서 lint가 비활성화되거나 경고를 놓칠 수 있습니다.<br>
 * - 라이브러리 사용자가 런타임에 워커 스레드에서 API를 호출할 수 있습니다.<br>
 *
 * **Debug-only enforcement policy / Debug 전용 강제 정책:**<br>
 * - `BuildConfig.DEBUG == true`: fail fast with `IllegalStateException` to catch misuse early.<br>
 * - `BuildConfig.DEBUG == false`(release): no crash from this guard to avoid production impact.<br><br>
 * - `BuildConfig.DEBUG == true`: 오용을 조기에 발견하기 위해 `IllegalStateException`으로 즉시 실패합니다.<br>
 * - `BuildConfig.DEBUG == false`(release): 운영 영향 최소화를 위해 이 가드로 인한 크래시는 발생시키지 않습니다.<br>
 *
 * @param apiName Human-readable API name used in failure message.<br><br>
 *                실패 메시지에 출력할 API 이름입니다.<br>
 */
@PublishedApi
internal fun assertMainThreadDebug(apiName: String) {
    if (!BuildConfig.DEBUG) return

    check(Looper.myLooper() == Looper.getMainLooper()) {
        "$apiName must be called on Main thread"
    }
}
