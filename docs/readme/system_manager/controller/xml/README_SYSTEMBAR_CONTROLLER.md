# SystemBarController vs Plain Android - Complete Comparison Guide
> **SystemBarController vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_xml` (UI-dependent module / UI 의존 모듈)
- **Package**: `kr.open.library.simple_ui.xml.system_manager.controller.systembar`
- **Primary Entry Path**: `window.getSystemBarController()` (Window 확장 함수 기반 진입)

<br></br>

## Overview (개요)
Provides unified status/navigation bar control for state queries, visibility, icon contrast, color, and edge-to-edge mode.
> 상태바/내비게이션바의 상태 조회, 가시성, 아이콘 대비, 색상, edge-to-edge 제어를 통합 제공합니다.

This guide reflects the current implementation contract aligned with PRD/SPEC.
> 이 문서는 PRD/SPEC과 정합성을 맞춘 현행 구현 계약 기준입니다.

<br></br>

## Recommended Entry Path (권장 진입 경로)
```kotlin
import android.graphics.Color
import kr.open.library.simple_ui.xml.system_manager.extensions.destroySystemBarControllerCache
import kr.open.library.simple_ui.xml.system_manager.extensions.getSystemBarController

val controller = window.getSystemBarController()

controller.setStatusBarColor(Color.TRANSPARENT, isDarkIcon = true)
controller.setNavigationBarColor(Color.BLACK, isDarkIcon = false)

controller.setStatusBarVisible()
controller.setNavigationBarVisible()

window.destroySystemBarControllerCache() // 종료 시 캐시 정리
```

- `SystemBarController(window)` 직접 생성보다 `window.getSystemBarController()`를 우선 사용하세요.
- 동일 Window는 컨트롤러 인스턴스를 캐시 재사용합니다.

<br></br>

## Cache Lifecycle (캐시 생명주기)
- `window.getSystemBarController()`
  - `decorView` tag(`R.id.tag_system_bar_controller`) 기반으로 Window당 1개 컨트롤러를 보장합니다.
- `window.destroySystemBarControllerCache()`
  - 캐시된 컨트롤러의 `onDestroy()`를 호출한 뒤 tag를 제거합니다.

<br></br>

## At a Glance (한눈 비교)
| Item (항목) | Plain Android (기본 방식) | Simple UI (Simple UI) | Notes (비고) |
|---|---|---|---|
| Color control | `window.statusBarColor` / `window.navigationBarColor` + SDK 분기 | `setStatusBarColor` / `setNavigationBarColor` | API 35+ overlay 경로 포함 |
| Visibility control | `WindowInsetsControllerCompat` 직접 조합 | `setStatusBarVisible/Gone`, `setNavigationBarVisible/Gone` | 호출 의도 명확 |
| Insets state query | `WindowInsets` 직접 계산 | `getStatusBar*State`, `getNavigationBar*State` | 상태 의미 통일 |
| Legacy Rect query | 좌표/널 해석을 호출부가 직접 처리 | `get*Rect()` 하위 호환 API | state 매핑 기반 |
| Edge-to-edge | `WindowCompat.setDecorFitsSystemWindows` 직접 호출 | `setEdgeToEdgeMode` | 단일 진입 API |
| Cleanup | 호출부가 정리 책임 | `destroySystemBarControllerCache()` | 정리 경로 일원화 |

<br></br>

## Public API Summary (공개 API 요약)
```kotlin
// State
getStatusBarVisibleState(), getStatusBarStableState()
getNavigationBarVisibleState(), getNavigationBarStableState()

// Legacy Rect
getStatusBarVisibleRect(), getStatusBarStableRect()
getNavigationBarVisibleRect(), getNavigationBarStableRect()

// Color / Icon
setStatusBarColor(), setNavigationBarColor()
setStatusBarDarkIcon(), setNavigationBarDarkIcon()
resetStatusBarColor(), resetNavigationBarColor()

// Visibility
setStatusBarVisible(), setStatusBarGone()
setNavigationBarVisible(), setNavigationBarGone()

// Common
setEdgeToEdgeMode(), isEdgeToEdgeEnabled(), onDestroy()
```

<br></br>

## State Model (상태 모델)
- `SystemBarVisibleState`
  - `NotReady`, `NotPresent`, `Hidden`, `Visible(rect)`
- `SystemBarStableState`
  - `NotReady`, `NotPresent`, `Stable(rect)`

### Hidden Criteria (Hidden 판정 기준)
- **StatusBar**
  - `stableTop == 0 && visibleTop == 0` -> `NotPresent`
  - `stableTop > 0 && visibleTop == 0` -> `Hidden`
- **NavigationBar**
  - `stableInsets.isAllZero() && visibleInsets.isAllZero()` -> `NotPresent`
  - `!stableInsets.isAllZero() && visibleInsets.isAllZero()` -> `Hidden`

즉, `stable`과 `visible`이 모두 0이면 `Hidden`이 아니라 `NotPresent`입니다.

<br></br>

## Legacy Rect Mapping (Legacy Rect 매핑)
- `NotReady` -> `null`
- `NotPresent` -> `Rect()`
- `Hidden` -> `Rect()`
- `Visible(rect)` / `Stable(rect)` -> 실제 `Rect`

기존 `Rect?` 호출부의 하위 호환을 유지하면서, 신규 코드는 state API 사용을 권장합니다.

<br></br>

## SDK Behavior (SDK 동작)
### API 35+
- 상태바/내비게이션바 색상은 overlay 기반으로 반영합니다.
- root insets 미준비 시 `WindowInsetsCompat.CONSUMED`를 폴백으로 사용합니다.
- overlay는 재사용하며, insets listener로 크기/위치를 동기화합니다.

### API 28~34
- `window.statusBarColor`, `window.navigationBarColor` 직접 설정 경로를 사용합니다.

### Visibility (가시성)
- API 30+: `WindowInsetsControllerCompat.show/hide`
- API 28~29: legacy window/systemUiVisibility 플래그 경로

<br></br>

## Thread Policy (스레드 정책)
- `window.getSystemBarController()` / `window.destroySystemBarControllerCache()`는 `@MainThread` 계약입니다.
- Debug 빌드에서는 오프 메인스레드 호출 시 `IllegalStateException`으로 즉시 실패합니다.

<br></br>

## Notes (주의사항)
- `setStatusBarVisible/Gone`, `setNavigationBarVisible/Gone` 호출 경로에서만 `systemBarsBehavior`가 `BEHAVIOR_DEFAULT`로 재설정됩니다.
- `setStatusBarDarkIcon`, `setNavigationBarDarkIcon`, `setStatusBarColor`, `setNavigationBarColor`는 `systemBarsBehavior`를 변경하지 않습니다.
- `isEdgeToEdgeEnabled()`는 컨트롤러 내부 플래그 기준이며, 외부 직접 변경과 완전 동기화되지는 않습니다.

<br></br>

## Test Command & Limitations (테스트 명령/한계)
```bash
./gradlew :simple_xml:testRobolectric --tests "*SystemBarHelperStateRobolectricTest"
```

- 검증 축: `NotReady`, `NotPresent`, `Visible`, `Stable`, navigation 위치(bottom/left/right)
- Robolectric 제약으로 Hidden/일부 Stable 시나리오는 완전 재현이 어려울 수 있습니다.

<br></br>

## Related Documents (관련 문서)
- AGENTS: `simple_xml/docs/feature/system_manager/controller/systembar/AGENTS.md`
- PRD: `simple_xml/docs/feature/system_manager/controller/systembar/PRD.md`
- SPEC: `simple_xml/docs/feature/system_manager/controller/systembar/SPEC.md`
- IMPLEMENTATION_PLAN: `simple_xml/docs/feature/system_manager/controller/systembar/IMPLEMENTATION_PLAN.md`
- Extensions index: `docs/readme/system_manager/README_SYSTEM_MANAGER_EXTENSIONS.md`

<br></br>
