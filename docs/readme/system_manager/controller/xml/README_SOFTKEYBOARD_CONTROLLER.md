# SoftKeyboardController vs Plain Android - Complete Comparison Guide
> **SoftKeyboardController vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_xml` (UI-dependent module / UI 의존 모듈)
- **Package**: `kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard`

<br></br>

## Overview (개요)
Provides keyboard show/hide helpers with explicit result contract, Main-thread safety checks, and resize policy control.  
> 메인 스레드 안전성 검사, 명시적 결과 계약, resize 정책 제어를 포함한 키보드 표시/숨김 헬퍼를 제공합니다.

<br></br>

## Contract Summary (결과 계약 요약)
| API | Meaning | 설명 |
|---|---|---|
| `show()` / `hide()` | **Request-level result** | IME 요청 경로 호출 성공 여부(실제 가시성 보장 아님) |
| `showDelay()` / `hideDelay()` | **Queue registration result** | 지연 Runnable 큐 등록 성공 여부 |
| `showAwait()` / `hideAwait()` | **Actual visibility result** | 실제 IME 가시성 결과(`Success`, `Timeout`, `Failure`) |
| `showAwaitAsync()` | **Deferred actual result** | 취소 가능한 non-null `Deferred<SoftKeyboardActionResult>` |
| `hideAwaitAsync()` | **Deferred actual result** | 취소 가능한 nullable `Deferred<SoftKeyboardActionResult>?` |

<br></br>

## At a Glance (한눈 비교)
| Item (항목) | Plain Android (기본 방식) | Simple UI (Simple UI) | Notes (비고) |
|---|---|---|---|
| Service acquisition | Manual `getSystemService()` | `getSoftKeyboardController()` | Less boilerplate<br>코드 간소화 |
| Thread safety | Caller-managed | Main-thread annotation + runtime guard | Safer usage<br>오용 방지 |
| Delayed operation | Manual `postDelayed`/coroutine | `showDelay/hideDelay` + `showAwaitAsync/hideAwaitAsync` | Result contract clarified<br>결과 의미 명확화 |
| IME control path | Mostly IMM direct call | Insets controller path + IMM fallback | Better compatibility<br>호환성 개선 |
| Resize behavior | Per-screen manual branching | `configureImeResize(policy)` | Explicit policy choice<br>정책 기반 선택 |

<br></br>

## Plain Android Example (순수 Android 예시)
```kotlin
private fun showKeyboard(editText: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    if (imm != null && editText.requestFocus()) {
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}

private fun hideKeyboard(editText: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    val token = editText.windowToken
    if (imm != null && token != null) {
        imm.hideSoftInputFromWindow(token, 0)
    }
}
```

<br></br>

## Simple UI Example (Simple UI 방식)
```kotlin
// 1) Request-level show/hide (요청 전달 결과)
val requestShowOk = getSoftKeyboardController().show(editText)
val requestHideOk = getSoftKeyboardController().hide(editText)

// 2) Queue-level delayed scheduling (큐 등록 결과)
val scheduled = getSoftKeyboardController().showDelay(editText, delay = 300L)

// 3) Actual visibility result (실제 가시성 결과)
val deferred = getSoftKeyboardController().showAwaitAsync(
    v = editText,
    coroutineScope = lifecycleScope,
    delayMillis = 300L,
)

lifecycleScope.launch {
    when (val result = deferred.await()) {
        SoftKeyboardActionResult.Success -> { /* visible */ }
        SoftKeyboardActionResult.Timeout -> { /* not visible within timeout */ }
        is SoftKeyboardActionResult.Failure -> { /* reason based handling */ }
    }
}
```

<br></br>

## Stylus API (Stylus API)
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    val startNow = controller.startStylusHandwriting(editText)
    val scheduled = controller.startStylusHandwriting(editText, delay = 300L)
}
```
- API 33+(TIRAMISU)에서만 동작합니다.
- 내부적으로 `requestFocus()`가 실패하면 `false`를 반환합니다.
- 지연 오버로드는 큐 등록 결과를 반환합니다.

<br></br>

## Resize Policy (Resize 정책)
```kotlin
val controller = getSoftKeyboardController()

// Safe default: API 30+ keeps current window policy, API 29- uses legacy resize
controller.setAdjustResize(window)

// Explicit policy control
controller.configureImeResize(window, SoftKeyboardResizePolicy.KEEP_CURRENT_WINDOW)
controller.configureImeResize(window, SoftKeyboardResizePolicy.LEGACY_ADJUST_RESIZE)
controller.configureImeResize(window, SoftKeyboardResizePolicy.FORCE_DECOR_FITS_TRUE)
```

<br></br>

## Window Mode API (윈도우 모드 API)
```kotlin
val controller = getSoftKeyboardController()

// Adjust Pan mode
controller.setAdjustPan(window)

// Direct soft input mode control (직접 모드 제어)
controller.setSoftInputMode(window, WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
```

- `setAdjustPan()`은 `SOFT_INPUT_ADJUST_PAN` 모드를 적용합니다.
- `setSoftInputMode()`는 임의의 `softInputTypes` 플래그 조합을 직접 지정할 수 있습니다.

<br></br>

## Notes (주의사항)
- `show()` / `hide()`의 `true`는 요청 경로 호출 성공 의미이며, 즉시 가시성 변경을 보장하지 않습니다.
- `showDelay()` / `hideDelay()`의 `true`는 큐 등록 성공 의미이며, 실제 실행/가시성 결과를 의미하지 않습니다.
- 실제 UI 결과가 필요하면 `showAwait*` / `hideAwait*`를 사용하십시오.
- `showAwaitAsync()`는 항상 non-null `Deferred`를 반환합니다. 오프-메인 호출 시에도 `Failure(OFF_MAIN_THREAD)` 결과를 담아 반환합니다.
- `hideAwaitAsync()`는 nullable `Deferred`를 반환하며, 오프-메인 호출 또는 내부 스케줄링 실패 시 `null`을 반환할 수 있습니다.
- `Timeout`은 제한 시간 내 미관측을 의미하며, 이후 시점의 상태 변경 가능성은 남아 있습니다.
- UI API는 메인 스레드에서 호출해야 하며, 오프-메인 호출은 경고 로그와 함께 즉시 실패 처리됩니다.

<br></br>

## Feature Documents (기능 문서)
- PRD: `simple_xml/docs/feature/system_manager/controller/softkeyboard/PRD.md`
- SPEC: `simple_xml/docs/feature/system_manager/controller/softkeyboard/SPEC.md`
- IMPLEMENTATION_PLAN: `simple_xml/docs/feature/system_manager/controller/softkeyboard/IMPLEMENTATION_PLAN.md`

<br></br>

## Related Extensions (관련 확장 함수)
- `getSoftKeyboardController()`  
  See full list / 전체 목록: [README_SYSTEM_MANAGER_EXTENSIONS.md](../../README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>
