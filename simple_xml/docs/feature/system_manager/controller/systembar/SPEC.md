# SystemBar Controller SPEC

## 문서 정보
- 문서명: SystemBar Controller SPEC
- 작성일: 2026-02-09
- 수정일: 2026-02-10
- 대상 모듈: simple_xml
- 패키지: kr.open.library.simple_ui.xml.system_manager.controller.systembar
- 수준: 구현 재현 가능 수준(Implementation-ready)
- 상태: 현행(as-is)

## 전제/참조
- 요구사항: `PRD.md`
- 기능 인덱스: `AGENTS.md`
- 사용자 가이드: `docs/readme/system_manager/controller/xml/README_SYSTEMBAR_CONTROLLER.md`
- 실제 진입 경로: `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/extensions/SystemServiceExtensionsXml.kt`

## 아키텍처 개요

### 구성 요소
- `SystemBarController`
  - 파사드 역할
  - 상태 조회/색상/아이콘/가시성/edge-to-edge/정리 API 제공
- `StatusBarHelper`
  - StatusBar 상태 계산
  - API 35+ status overlay 처리
- `NavigationBarHelper`
  - NavigationBar 상태 계산
  - bottom/left/right 위치 판별
  - API 35+ navigation overlay 처리
- `SystemBarHelperBase`
  - 공통 유틸(`isSystemBarRectReady`, `Insets.isAllZero`, overlay view 공통 설정)
- 상태 모델
  - `SystemBarVisibleState`
  - `SystemBarStableState`

### 확장 함수 계약
- `Window.getSystemBarController()`
  - decorView tag(`R.id.tag_system_bar_controller`) 기반 캐시
  - Window당 1개 인스턴스 보장
- `Window.destroySystemBarControllerCache()`
  - 캐시된 컨트롤러 `onDestroy()` 호출
  - tag null 처리

## 공개 API 명세

```kotlin
public class SystemBarController(window: Window) : BaseSystemService(window.context, null) {
    fun getStatusBarVisibleRect(): Rect?
    fun getStatusBarVisibleState(): SystemBarVisibleState
    fun getStatusBarStableRect(): Rect?
    fun getStatusBarStableState(): SystemBarStableState

    fun getNavigationBarVisibleRect(): Rect?
    fun getNavigationBarVisibleState(): SystemBarVisibleState
    fun getNavigationBarStableRect(): Rect?
    fun getNavigationBarStableState(): SystemBarStableState

    fun setStatusBarColor(color: Int, isDarkIcon: Boolean = false)
    fun setNavigationBarColor(color: Int, isDarkIcon: Boolean = false)
    fun setStatusBarDarkIcon(isDarkIcon: Boolean)
    fun setNavigationBarDarkIcon(isDarkIcon: Boolean)

    fun setStatusBarVisible()
    fun setStatusBarGone()
    fun setNavigationBarVisible()
    fun setNavigationBarGone()

    fun resetStatusBarColor(restoreVisibility: Boolean = true)
    fun resetNavigationBarColor(restoreVisibility: Boolean = true)

    fun setEdgeToEdgeMode(enabled: Boolean)
    fun isEdgeToEdgeEnabled(): Boolean

    override fun onDestroy()
}
```

## 상태 모델 명세

```kotlin
public sealed interface SystemBarVisibleState {
    data object NotReady : SystemBarVisibleState
    data object NotPresent : SystemBarVisibleState
    data object Hidden : SystemBarVisibleState
    data class Visible(val rect: Rect) : SystemBarVisibleState
}

public sealed interface SystemBarStableState {
    data object NotReady : SystemBarStableState
    data object NotPresent : SystemBarStableState
    data class Stable(val rect: Rect) : SystemBarStableState
}
```

## 상태 판정 규칙

### 공통 준비 조건
- `decorView.isAttachedToWindow && decorView.width > 0 && decorView.height > 0` 미충족 또는 `windowInsets == null`이면 `NotReady`.

### StatusBar Visible 판정
- 입력
  - `stableTop = insetsIgnoringVisibility(statusBars).top`
  - `visibleTop = insets(statusBars).top`
- 규칙
  - `stableTop == 0 && visibleTop == 0` -> `NotPresent`
  - `stableTop > 0 && visibleTop == 0` -> `Hidden`
  - 그 외 -> `Visible(Rect(0,0,decorWidth,clampedTop))`

### StatusBar Stable 판정
- 입력
  - `stableTop = insetsIgnoringVisibility(statusBars).top`
- 규칙
  - `stableTop == 0` -> `NotPresent`
  - 그 외 -> `Stable(Rect(0,0,decorWidth,clampedTop))`

### NavigationBar Visible 판정
- 입력
  - `stableInsets = insetsIgnoringVisibility(navigationBars)`
  - `visibleInsets = insets(navigationBars)`
  - `stableZero = stableInsets.isAllZero()`
  - `visibleZero = visibleInsets.isAllZero()`
- 규칙
  - `stableZero && visibleZero` -> `NotPresent`
  - `!stableZero && visibleZero` -> `Hidden`
  - 나머지: `rect = insetsToNavigationBarRect(visibleInsets)`
    - `rect.isEmpty` -> `NotPresent` (방어적 폴백)
    - else -> `Visible(rect)`

### NavigationBar Stable 판정
- 입력
  - `stableInsets = insetsIgnoringVisibility(navigationBars)`
- 규칙
  - `stableInsets.isAllZero()` -> `NotPresent`
  - `rect = insetsToNavigationBarRect(stableInsets)`
    - `rect.isEmpty` -> `NotPresent` (방어적 폴백)
    - else -> `Stable(rect)`

### Navigation rect 변환 규칙 (`insetsToNavigationBarRect`)
- 우선순위: `bottom -> left -> right`
- `bottom > 0`: `Rect(0, decorHeight-bottom, decorWidth, decorHeight)`
- `left > 0`: `Rect(0, 0, left, decorHeight)`
- `right > 0`: `Rect(decorWidth-right, 0, decorWidth, decorHeight)`
- 그 외: `Rect()`

## Legacy Rect API 매핑

### Visible -> Rect?
- `NotReady` -> `null`
- `NotPresent` -> `Rect()`
- `Hidden` -> `Rect()`
- `Visible(rect)` -> `rect`

### Stable -> Rect?
- `NotReady` -> `null`
- `NotPresent` -> `Rect()`
- `Stable(rect)` -> `rect`

## SDK별 동작 명세

### 색상 제어
- API 35+
  - Status/Nav 모두 helper의 overlay 방식 사용
  - root insets 미준비 시 `WindowInsetsCompat.CONSUMED`를 폴백 입력으로 사용
  - overlay는 최초 1회 attach 후 재사용
  - insets listener로 크기/위치 동기화
- API 28~34
  - `window.statusBarColor`, `window.navigationBarColor` 직접 설정

### 가시성 제어
- API 30+
  - `WindowInsetsControllerCompat.show/hide`
- API 28~29
  - Status: `FLAG_FULLSCREEN` 토글
  - Navigation: `SYSTEM_UI_FLAG_HIDE_NAVIGATION`, `SYSTEM_UI_FLAG_IMMERSIVE_STICKY` 토글

### reset 동작
- API 35+
  - overlay cleanup
- API 28~34
  - theme attribute(`android.R.attr.statusBarColor`, `android.R.attr.navigationBarColor`) 복원
- `restoreVisibility=true`이면 reset 후 visible 호출

## API별 전제조건/후조건

### 상태 조회 API
- 전제조건
  - 없음(초기 호출 허용)
- 후조건
  - 미준비면 `NotReady` 또는 `null`(legacy)
  - 준비 완료 시 명시적 상태 또는 좌표 반환

### 색상 API (`setStatusBarColor`, `setNavigationBarColor`)
- 전제조건
  - `Window` 유효
- 후조건
  - SDK 분기별 색상 적용
  - 내부적으로 각각 `setStatusBarDarkIcon(isDarkIcon)`, `setNavigationBarDarkIcon(isDarkIcon)`를 후속 호출해 아이콘 밝기를 동기화

### 가시성 API (`set*Visible`, `set*Gone`)
- 전제조건
  - `Window` 유효
- 후조건
  - SDK 분기별 show/hide 요청 발행
- 참고
  - 내부 `getWindowInsetController()` 호출 시 `systemBarsBehavior`가 `BEHAVIOR_DEFAULT`로 재설정된다.

### lifecycle API (`onDestroy`)
- 전제조건
  - 없음(중복 호출 허용)
- 후조건
  - status/nav overlay listener 제거
  - status/nav overlay view 제거

## 예외/오류 처리 정책
- `SystemBarController.getRootWindowInsetsCompat()`
  - `tryCatchSystemManager(null)`로 보호
- helper 상태 계산
  - `safeCatch(default = NotReady)`로 보호
- helper cleanup
  - `safeCatch {}`로 보호
- 정책
  - 예외를 호출부로 전파하기보다 기본값/명시 상태로 안정성 우선

## 성능/메모리 정책
- overlay view 재사용: 색상 반복 변경 시 새 view 생성 억제
- insets listener 최적화
  - 변경이 없으면 `layoutParams` 재할당 회피
  - Navigation은 width/height/gravity 비교 후 변경 시에만 갱신
- cleanup 시 listener 제거 후 view 제거로 누수 위험 완화

## 스레드/동시성
- 메인 스레드 강제 가드는 별도 두지 않음
- Android view/window API 특성상 메인 스레드 호출을 권장

## 테스트 명세(현행)
- 테스트 파일
  - `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/system_manager/controller/systembar/internal/helper/SystemBarHelperStateRobolectricTest.kt`
- 케이스 수
  - `@Test` 기준 11개
- 검증 축
  - `NotReady`, `NotPresent`, `Visible`, `Stable`
  - status/nav rect 계산
  - navigation bottom/left/right 좌표
  - status clamp
- 한계
  - Robolectric의 `setInsetsIgnoringVisibility` 제약으로 Hidden과 Navigation Stable 일부 시나리오는 완전 재현 불가

## 알려진 리스크
- `getWindowInsetController()`에서 `systemBarsBehavior`를 항상 `BEHAVIOR_DEFAULT`로 세팅하므로, 외부 정책과 충돌 가능성 존재
- `isEdgeToEdgeEnabled()`는 내부 플래그 기반이라 외부에서 직접 `setDecorFitsSystemWindows` 변경 시 불일치 가능성 존재
- controller/cache 전용 정밀 테스트는 helper 테스트 대비 상대적으로 적음

## 추적 매트릭스
- 상태 모델 계약
  - 코드: `model/SystemBarState.kt`
  - 테스트: `SystemBarHelperStateRobolectricTest.kt`
- 캐시/수명주기 계약
  - 코드: `SystemServiceExtensionsXml.kt`
  - 문서: `README_SYSTEMBAR_CONTROLLER.md`
- legacy 매핑 계약
  - 코드: `SystemBarController.kt`의 `toLegacyRectOrNull()`
  - 문서: PRD/SPEC/PLAN
