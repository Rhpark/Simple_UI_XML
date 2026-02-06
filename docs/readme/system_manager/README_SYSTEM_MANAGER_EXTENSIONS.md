# System Manager Extensions vs Plain Android - Complete Comparison Guide
> **System Manager Extensions vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_xml` (UI-dependent module / UI 의존 모듈)
- **Package**: `kr.open.library.simple_ui.xml.system_manager.extensions` (패키지)

<br></br>

## Overview (개요)
Summarizes extension entry points for system manager helpers in `simple_xml`.  
> `simple_xml`에서 제공하는 시스템 매니저 확장 진입점을 정리합니다.

<br></br>

## At a Glance (한눈 비교)
| Function (함수)                              | Return (반환)              | Description (설명)                   | Related Docs (관련 상세 문서) |
|--------------------------------------------|--------------------------|------------------------------------|---|
| `getSoftKeyboardController()`              | `SoftKeyboardController` | Keyboard show/hide/delay control<br>키보드 표시/숨김/지연 제어 | [README_SOFTKEYBOARD_CONTROLLER.md](controller/xml/README_SOFTKEYBOARD_CONTROLLER.md) |
| `getFloatingViewController()`              | `FloatingViewController` | Floating view control<br>플로팅 뷰 제어 | [README_FLOATING_VIEW_CONTROLLER.md](controller/xml/README_FLOATING_VIEW_CONTROLLER.md) |
| `getDisplayInfo()`                         | `DisplayInfo`            | Display info access<br>디스플레이 정보 제공 | [README_DISPLAY_INFO.md](info/xml/README_DISPLAY_INFO.md) |
| `Window.getSystemBarController()`          | `SystemBarController`    | System bar control (cached)<br>시스템 바 제어(캐시) | - |
| `Window.destroySystemBarControllerCache()` | `-`                      | Clear system bar controller cache<br>시스템 바 컨트롤러 캐시 제거 | - |

<br></br>

## Why It Matters (중요한 이유)
Provides a compact map of `simple_xml` Context extension functions and their roles.  
> `simple_xml` 모듈에서 제공하는 Context 확장 함수 목록과 역할을 정리합니다.

Extensions are the entry point to create `simple_core` controllers easily.  
> 확장 함수는 `simple_core`의 컨트롤러를 간단히 생성하기 위한 진입점입니다.

<br></br>

## Usage Example (사용 예시)
```kotlin
// Soft keyboard controller example (키보드 컨트롤러 예시)
private fun showKeyboard(editText: EditText) {
    getSoftKeyboardController().show(editText) // Show keyboard (키보드 표시)
}
```

<br></br>

## Notes (주의사항)
- Floating views may require the `SYSTEM_ALERT_WINDOW` permission.
- System bar controller is cached per Window; call destroy when done.
> 플로팅 뷰는 `SYSTEM_ALERT_WINDOW` 권한이 필요할 수 있습니다.
> 시스템 바 컨트롤러는 Window 단위 캐싱이며 사용 종료 시 정리를 권장합니다.

<br></br>

