package kr.open.library.simple_ui.core.thread

import android.os.Looper
import kr.open.library.simple_ui.core.BuildConfig

/**
 * Runtime main-thread assertion helper used together with `@MainThread` contracts.<br><br>
 * `@MainThread` 계약과 함께 사용하는 런타임 메인 스레드 검증 헬퍼입니다.<br>
 *
 * `@MainThread` is the primary static lint/IDE contract, while this helper additionally enforces the rule
 * at runtime when the `simple_core` library variant is built with `BuildConfig.DEBUG == true`.<br><br>
 * `@MainThread`는 기본 정적(lint/IDE) 계약이고, 이 헬퍼는 `simple_core` 라이브러리 변형이
 * `BuildConfig.DEBUG == true`로 빌드된 경우에만 런타임으로 규칙을 추가 검증합니다.<br>
 *
 * **Why not `@MainThread` only / `@MainThread`만 단독 사용하지 않는 이유:**<br>
 * - Lint can be disabled or warnings can be missed at call sites.<br>
 * - Library consumers may call APIs from worker threads at runtime.<br><br>
 * - 호출부에서 lint가 비활성화되거나 경고를 놓칠 수 있습니다.<br>
 * - 라이브러리 사용자가 런타임에 워커 스레드에서 API를 호출할 수 있습니다.<br>
 *
 * **Debug-only enforcement policy / Debug 전용 강제 정책:**<br>
 * - `BuildConfig.DEBUG == true`: fail fast with `IllegalStateException` to catch misuse early.<br>
 * - `BuildConfig.DEBUG == false` (release): no crash from this guard to avoid production impact.<br>
 * - A published release AAR keeps this guard disabled even when the consumer application is a Debug build.<br><br>
 * - `BuildConfig.DEBUG == true`: 오용을 조기에 발견하기 위해 `IllegalStateException`으로 즉시 실패합니다.<br>
 * - `BuildConfig.DEBUG == false`(release): 운영 영향 최소화를 위해 이 가드로 인한 크래시는 발생시키지 않습니다.<br>
 * - 배포된 release AAR에서는 소비자 앱이 Debug 빌드여도 이 가드가 비활성화된 상태를 유지합니다.<br>
 *
 * @param apiName Human-readable API name used in failure message.<br><br>
 *                실패 메시지에 출력할 API 이름입니다.<br>
 */
public inline fun assertMainThreadDebug(apiName: String) {
    if (!BuildConfig.DEBUG) return

    check(Looper.myLooper() == Looper.getMainLooper()) {
        "$apiName must be called on Main thread"
    }
}
