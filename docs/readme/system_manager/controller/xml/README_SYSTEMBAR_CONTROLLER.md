# SystemBarController vs Plain Android - Complete Comparison Guide
> **SystemBarController vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_xml` (UI-dependent module / UI 의존 모듈)
- **Package**: `kr.open.library.simple_ui.xml.system_manager.controller.systembar`
- **Primary Entry Path**: `Window.getSystemBarController()` (Window 확장 함수 기반 진입)

<br></br>

## Overview (개요)
Provides unified status/navigation bar control for color, visibility, edge-to-edge, and insets state queries.  
> 상태/내비게이션 바의 색상, 가시성, edge-to-edge, insets 상태 조회를 통합해서 제공합니다.

<br></br>

## Recommended Entry Path (권장 진입 경로)
```kotlin
val controller = window.getSystemBarController()

controller.setStatusBarColor(Color.BLACK, isDarkIcon = false)
controller.setNavigationBarColor(Color.WHITE, isDarkIcon = true)
```

- `SystemBarController(window)` 직접 생성보다 `window.getSystemBarController()` 사용을 권장합니다.
- Window 단위 캐시를 사용하므로 같은 Window에서 일관된 상태를 유지할 수 있습니다.

<br></br>

## Cache Lifecycle (캐시 수명주기)
```kotlin
// 정리 및 캐시 제거
window.destroySystemBarControllerCache()
```

- `destroySystemBarControllerCache()`는 내부에서 `onDestroy()`를 호출하고 캐시를 정리합니다.
- 강제 재생성 또는 화면 종료 정리 시 이 경로를 권장합니다.

<br></br>

## At a Glance (한눈 비교)
| Item (항목) | Plain Android (기본 방식) | Simple UI (Simple UI) | Notes (비고) |
|---|---|---|---|
| Status/Navigation color | `window.statusBarColor` / `window.navigationBarColor` + API 분기 | `setStatusBarColor` / `setNavigationBarColor` | API 분기 캡슐화 |
| Bar visibility | `WindowInsetsControllerCompat` 직접 조합 | `setStatusBarVisible/Gone`, `setNavigationBarVisible/Gone` | 호출 의도 명확 |
| Insets rect query | `WindowInsets` 직접 계산 | `getStatusBar*`, `getNavigationBar*` | 계산 로직 표준화 |
| Edge-to-edge | `WindowCompat.setDecorFitsSystemWindows` 직접 제어 | `setEdgeToEdgeMode` | 단일 API 제공 |
| Lifecycle cleanup | 호출부별 수동 정리 | `destroySystemBarControllerCache()` | Window 캐시 정리 일원화 |

<br></br>

## State API (상태 API)
```kotlin
val statusVisible = controller.getStatusBarVisibleState()
val statusStable = controller.getStatusBarStableState()
val navVisible = controller.getNavigationBarVisibleState()
val navStable = controller.getNavigationBarStableState()
```

- `VisibleState`: `NotReady`, `NotPresent`, `Hidden`, `Visible(rect)`
- `StableState`: `NotReady`, `NotPresent`, `Stable(rect)`

### Hidden 판단 기준 (Hidden Criteria)
- StatusBar: `stableTop > 0 && visibleTop == 0`일 때 `Hidden`
- NavigationBar: `stableInsets` 존재 + `visibleInsets`가 모두 0일 때 `Hidden`
- `stable`과 `visible`이 모두 0이면 `Hidden`이 아니라 `NotPresent`입니다.

<br></br>

## Legacy Rect API (호환 Rect API)
```kotlin
val legacyStatusRect = controller.getStatusBarVisibleRect()
val legacyNavRect = controller.getNavigationBarStableRect()
```

- 기존 `Rect?` 기반 API는 하위 호환용으로 유지됩니다.
- 의미 매핑:
  - `NotReady` -> `null`
  - `NotPresent/Hidden` -> `Rect()`
  - `Visible/Stable` -> 실제 `Rect`

<br></br>

## API 35+ Insets Fallback (CONSUMED 폴백)
- API 35+ 색상 적용 시 root insets가 아직 준비되지 않으면 내부적으로 `WindowInsetsCompat.CONSUMED`를 폴백으로 사용합니다.
- 이 경우 오버레이가 초기에는 0 크기로 붙을 수 있으며, 이후 `requestApplyInsets`와 insets listener로 보정됩니다.

<br></br>

## Notes (주의사항)
- 실제 사용 경로는 `Window` 확장 함수 기반을 권장합니다.
- 동일 Window에서 직접 생성과 캐시 진입을 혼용하지 않는 것을 권장합니다.
- API 35+에서는 내부 오버레이 뷰 기반으로 시스템 바 배경 처리를 수행합니다.
- `setStatusBarVisible/Gone`, `setNavigationBarVisible/Gone` 호출 시 내부 `WindowInsetsController`의 `systemBarsBehavior`가 `BEHAVIOR_DEFAULT`로 재설정됩니다.

<br></br>

## Test Command & Limitations (테스트 명령/한계)
```bash
./gradlew :simple_xml:testRobolectric --tests "*SystemBarHelperStateRobolectricTest"
```

- 검증 축: `NotReady`, `NotPresent`, `Visible`, `Stable` 및 좌표 계산
- Robolectric 제약으로 `Hidden`/일부 `Stable` 시나리오(특히 Navigation stable)는 완전 재현이 어렵습니다.

<br></br>

## Feature Documents (기능 문서)
- AGENTS: `simple_xml/docs/feature/system_manager/controller/systembar/AGENTS.md`
- PRD: `simple_xml/docs/feature/system_manager/controller/systembar/PRD.md`
- SPEC: `simple_xml/docs/feature/system_manager/controller/systembar/SPEC.md`
- IMPLEMENTATION_PLAN: `simple_xml/docs/feature/system_manager/controller/systembar/IMPLEMENTATION_PLAN.md`

<br></br>

## Related Extensions (관련 확장 함수)
- `Window.getSystemBarController()`
- `Window.destroySystemBarControllerCache()`
- 전체 목록: [README_SYSTEM_MANAGER_EXTENSIONS.md](../../README_SYSTEM_MANAGER_EXTENSIONS.md)
