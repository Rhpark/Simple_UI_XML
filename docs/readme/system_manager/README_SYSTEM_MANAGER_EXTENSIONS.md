# System Manager Extensions vs Plain Android - Complete Comparison Guide
> **System Manager Extensions vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_system_manager` (system manager 전용 모듈)
- **Package**: `kr.open.library.simple_ui.xml.system_manager.extensions`

<br></br>

## Overview (개요)
Summarizes extension entry points for system manager helpers owned by `simple_system_manager`.  
> `simple_system_manager`가 제공하는 system manager 확장 진입점을 정리합니다.

<br></br>

## At a Glance (한눈 비교)
| Function (함수) | Return (반환) | Description (설명) | Related Docs (관련 상세 문서) |
|---|---|---|---|
| `getSoftKeyboardController()` | `SoftKeyboardController` | Keyboard show/hide/delay control<br>키보드 표시/숨김/지연 제어 | [README_SOFTKEYBOARD_CONTROLLER.md](controller/xml/README_SOFTKEYBOARD_CONTROLLER.md) |
| `getFloatingViewController()` | `FloatingViewController` | Floating view control<br>플로팅 뷰 제어 | [README_FLOATING_VIEW_CONTROLLER.md](controller/xml/README_FLOATING_VIEW_CONTROLLER.md) |
| `getDisplayInfo()` | `DisplayInfo` | Display info access<br>디스플레이 정보 제공 | [README_DISPLAY_INFO.md](info/xml/README_DISPLAY_INFO.md) |
| `Window.getSystemBarController()` | `SystemBarController` | System bar control (cached)<br>시스템 바 제어(캐시) | [README_SYSTEMBAR_CONTROLLER.md](controller/xml/README_SYSTEMBAR_CONTROLLER.md) |
| `Window.destroySystemBarControllerCache()` | `Unit` | Clear system bar controller cache<br>시스템 바 컨트롤러 캐시 제거 | [README_SYSTEMBAR_CONTROLLER.md](controller/xml/README_SYSTEMBAR_CONTROLLER.md) |

<br></br>

## Why It Matters (중요한 이유)
Provides a compact map of Context and Window extension entry points for `simple_system_manager`.  
> `simple_system_manager`의 Context, Window 확장 함수 진입점과 역할을 한 번에 확인할 수 있습니다.

Extensions are the primary entry point to create XML-side system manager controllers owned by `simple_system_manager`.  
> 확장 함수는 `simple_system_manager`가 소유하는 XML 계열 system manager controller를 생성하는 기본 진입점입니다.

<br></br>

## Usage Example (사용 예시)
```kotlin
private fun showKeyboard(editText: EditText) {
    getSoftKeyboardController().show(editText)
}
```

```kotlin
private fun setupSystemBar(window: Window) {
    val controller = window.getSystemBarController()
    controller.setStatusBarDarkIcon(true)
    controller.setNavigationBarDarkIcon(false)
}

private fun clearSystemBar(window: Window) {
    window.destroySystemBarControllerCache()
}
```

<br></br>

## Notes (주의사항)
- Floating view 계열은 `SYSTEM_ALERT_WINDOW` 권한이 필요할 수 있습니다.
- Floating controller의 Boolean 반환은 실제 `WindowManager` 적용 성공 여부를 기준으로 합니다.
- `removeAllFloatingView()`는 `first-failure-stop` 전략이며 일반 호출에서는 부분 정리 상태가 남을 수 있습니다.
- System bar controller는 `Window` 단위로 캐시되므로 사용 종료 시 `destroySystemBarControllerCache()` 호출을 권장합니다.
- `window.getSystemBarController()`와 `window.destroySystemBarControllerCache()`는 `@MainThread` 계약이며 Debug 빌드에서는 오프 메인스레드 호출 시 `IllegalStateException`으로 즉시 실패합니다.
- `clearTint`, `applyWindowInsetsAsPadding`, `bindLifecycleObserver`/`unbindLifecycleObserver`는 View 확장 범위이므로 `docs/readme/README_EXTENSIONS.md`를 참조하십시오.

<br></br>

## Related Docs (관련 문서)
- Controller docs: `docs/readme/system_manager/controller/xml/`
- Info docs: `docs/readme/system_manager/info/xml/`
- Feature docs: `simple_system_manager/docs/feature/system_manager/`
