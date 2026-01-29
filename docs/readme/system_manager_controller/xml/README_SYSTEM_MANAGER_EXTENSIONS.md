# System Manager Extensions vs Plain Android - Complete Comparison Guide
> **System Manager Extensions vs 순수 Android - 완벽 비교 가이드**

## 📦 Module Information (모듈 정보)
- **Module**: `simple_xml` (UI-dependent module / UI 의존 모듈)
- **Package**: `kr.open.library.simple_ui.xml.system_manager.extensions`

<br></br>

## 🔎 At a Glance (한눈 비교)
| 함수 | 반환 | 설명 | 관련 상세 문서 |
|---|---|---|---|
| getSoftKeyboardController() | SoftKeyboardController | 키보드 표시/숨김/지연 제어 | [README_SOFTKEYBOARD_CONTROLLER.md](README_SOFTKEYBOARD_CONTROLLER.md) |
| getFloatingViewController() | FloatingViewController | 플로팅 뷰 제어 | [README_FLOATING_VIEW_CONTROLLER.md](README_FLOATING_VIEW_CONTROLLER.md) |
| getDisplayInfo() | DisplayInfo | 디스플레이 정보 제공 | - |
| Window.getSystemBarController() | SystemBarController | 시스템 바 제어(캐시) | - |
| Window.destroySystemBarControllerCache() | - | 시스템 바 컨트롤러 캐시 제거 | - |

<br></br>

## 💡 Why It Matters (왜 중요한가)
`simple_xml` 모듈에서 제공하는 Context 확장 함수 목록과 역할을 정리합니다.
확장 함수는 `simple_core`의 컨트롤러를 **간단히 생성**하기 위한 진입점입니다.

<br></br>

## 사용 예시
```kotlin
// 키보드 컨트롤러 예시
private fun showKeyboard(editText: EditText) {
    getSoftKeyboardController().show(editText)
}
```

<br></br>

## 주의사항
- 플로팅 뷰는 `SYSTEM_ALERT_WINDOW` 권한이 필요할 수 있습니다.
- 시스템 바 컨트롤러는 Window 단위 캐싱이며 사용 종료 시 정리를 권장합니다.
