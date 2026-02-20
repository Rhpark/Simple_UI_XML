# SoftKeyboardController vs Plain Android - Complete Comparison Guide
> **SoftKeyboardController vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_xml` (UI-dependent module / UI 의존 모듈)
- **Package**: `kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard`
- **Entry API**: `Context.getSoftKeyboardController()`

<br></br>

## Overview (개요)
Provides unified soft keyboard control APIs for request-level actions, delayed scheduling, and actual IME visibility observation.
> 요청 수준 동작, 지연 스케줄링, 실제 IME 가시성 관찰까지 포함하는 통합 소프트 키보드 제어 API를 제공합니다.

This guide reflects the current implementation contract.
> 이 문서는 현재 구현 계약 기준으로 작성되었습니다.

<br></br>

## Quick Start (빠른 시작)
```kotlin
import kr.open.library.simple_ui.xml.system_manager.extensions.getSoftKeyboardController

val controller = context.getSoftKeyboardController()

// Request-level result
val showRequested = controller.show(editText)
val hideRequested = controller.hide(editText)

// Delayed queue registration result
val showScheduled = controller.showDelay(editText, delay = 300L)
val hideScheduled = controller.hideDelay(editText, delay = 300L)
```

<br></br>

## Contract Summary (반환 계약 요약)
| API | Contract | Meaning |
|---|---|---|
| `show()` / `hide()` | Request-level result | IME 요청 발행 성공 여부를 반환 (`Boolean`) |
| `showDelay()` / `hideDelay()` | Queue registration result | `postDelayed` 등록 성공 여부를 반환 (`Boolean`) |
| `showAwait()` / `hideAwait()` | Actual visibility result | 실제 IME 가시성 결과를 반환 (`Success` / `Timeout` / `Failure`) |
| `showAwaitAsync()` | Deferred actual result | non-null `Deferred<SoftKeyboardActionResult>` 반환 |
| `hideAwaitAsync()` | Deferred actual result | nullable `Deferred<SoftKeyboardActionResult>?` 반환 |

<br></br>

## Actual Visibility APIs (실제 가시성 API)
```kotlin
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardActionResult

lifecycleScope.launch {
    when (val result = controller.showAwait(editText, timeoutMillis = 700L)) {
        SoftKeyboardActionResult.Success -> {
            // expected visibility observed
        }
        SoftKeyboardActionResult.Timeout -> {
            // not observed within timeout window
        }
        is SoftKeyboardActionResult.Failure -> {
            // reason-based handling
        }
    }
}
```

<br></br>

## Async APIs (비동기 API)
```kotlin
val showDeferred = controller.showAwaitAsync(
    v = editText,
    coroutineScope = lifecycleScope,
    delayMillis = 200L,
)

val hideDeferred = controller.hideAwaitAsync(
    v = editText,
    coroutineScope = lifecycleScope,
    delayMillis = 200L,
)
```

- `showAwaitAsync()`는 non-null Deferred를 반환합니다.
- `hideAwaitAsync()`는 오프 메인 호출 또는 스케줄링 실패 시 `null`을 반환할 수 있습니다.

<br></br>

## Resize Policy (Resize 정책)
```kotlin
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardResizePolicy

// Safe default
controller.setAdjustResize(window)

// Explicit policies
controller.configureImeResize(window, SoftKeyboardResizePolicy.KEEP_CURRENT_WINDOW)
controller.configureImeResize(window, SoftKeyboardResizePolicy.LEGACY_ADJUST_RESIZE)
controller.configureImeResize(window, SoftKeyboardResizePolicy.FORCE_DECOR_FITS_TRUE)

// Additional mode helpers
controller.setAdjustPan(window)
controller.setSoftInputMode(window, WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
```

정책 요약:
- `KEEP_CURRENT_WINDOW`: API 30+는 현재 window 정책 유지, API 29-는 legacy resize 적용
- `LEGACY_ADJUST_RESIZE`: 모든 API에서 legacy resize 경로
- `FORCE_DECOR_FITS_TRUE`: API 30+에서 `setDecorFitsSystemWindows(true)` 강제

<br></br>

## Stylus Handwriting (API 33+) (스타일러스 필기)
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    val startedNow = controller.startStylusHandwriting(editText)
    val startedDelayed = controller.startStylusHandwriting(editText, delay = 300L)
}
```

<br></br>

## Notes (주의사항)
- 모든 공개 API는 `@MainThread` 계약입니다.
- 오프 메인스레드 호출 시 경고 로그 후 `false`, `null`, 또는 `Failure(OFF_MAIN_THREAD)`로 실패를 반환합니다.
- `show()`/`hide()`는 요청 발행 성공 여부이며, 즉시 가시성 변화를 보장하지 않습니다.
- `showDelay()`/`hideDelay()`는 지연 등록 결과만 반환합니다.
- 실제 UI 반영 결과가 필요하면 `showAwait*`/`hideAwait*`를 사용하세요.
- `Timeout`은 제한 시간 내 관찰 실패를 의미하며, 이후 시점에 상태가 변경될 가능성은 남아 있습니다.

<br></br>

## Test Command & Limitations (테스트 명령/한계)
```bash
./gradlew :simple_xml:testRobolectric --tests "*SoftKeyboardControllerRobolectricTest"
```

- Robolectric은 IME 동작을 100% 재현하지 못하므로, 실제 단말 확인이 필요한 시나리오가 존재합니다.

<br></br>

## Related Documents (관련 문서)
- PRD: `simple_xml/docs/feature/system_manager/controller/softkeyboard/PRD.md`
- SPEC: `simple_xml/docs/feature/system_manager/controller/softkeyboard/SPEC.md`
- IMPLEMENTATION_PLAN: `simple_xml/docs/feature/system_manager/controller/softkeyboard/IMPLEMENTATION_PLAN.md`
- Extensions index: `docs/readme/system_manager/README_SYSTEM_MANAGER_EXTENSIONS.md`

<br></br>
