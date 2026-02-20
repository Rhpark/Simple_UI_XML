# SystemBar Controller PRD

## 문서 정보
- 문서명: SystemBar Controller PRD
- 작성일: 2026-02-09
- 수정일: 2026-02-10
- 대상 모듈: simple_xml
- 패키지: kr.open.library.simple_ui.xml.system_manager.controller.systembar
- 상태: 현행(as-is)
- 기준 코드:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/SystemBarController.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/model/SystemBarState.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/internal/helper/StatusBarHelper.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/internal/helper/NavigationBarHelper.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/extensions/SystemServiceExtensionsXml.kt`

## 배경/문제 정의
- 시스템 바 제어는 API 레벨, 내비게이션 모드(버튼/제스처), 화면 회전, 멀티윈도우 상태에 따라 분기 비용이 큽니다.
- 호출부가 `WindowInsets`를 직접 다루면 `미준비/미존재/숨김` 의미가 혼재되어 버그가 발생하기 쉽습니다.
- Android 15(API 35)+의 시스템 바 색상 처리 변화로, 기존 `window.statusBarColor/navigationBarColor`만으로는 일관된 UX를 보장하기 어렵습니다.
- 동일 `Window`에 여러 컨트롤러를 만들면 오버레이 중복/정리 누락/상태 불일치가 생길 수 있습니다.

## 제품 목표
- 상태바/내비게이션바 제어를 `SystemBarController` 단일 파사드로 제공한다.
- 실제 사용 경로를 `Window.getSystemBarController()`로 표준화한다.
- 상태 반환을 `SystemBarVisibleState`, `SystemBarStableState`로 타입 분리해 의미를 명확히 한다.
- 기존 `Rect?` API와 하위 호환을 유지해 기존 사용자 마이그레이션 비용을 낮춘다.
- API 35+ 오버레이의 생성/업데이트/정리까지 안전한 수명주기 계약을 제공한다.

## 비목표
- 제조사별 UI 커스터마이징(아이콘 대비 자동 튜닝, 제스처 민감도 최적화)
- Compose 전용 system bar API 제공
- 시스템 바 애니메이션 프레임 단위 제어
- 앱 전체 edge-to-edge 자동 insets 분배

## 범위

### 포함 범위
- 공개 API
  - 상태 조회: `getStatusBarVisibleState`, `getStatusBarStableState`, `getNavigationBarVisibleState`, `getNavigationBarStableState`
  - 호환 조회: `getStatusBarVisibleRect`, `getStatusBarStableRect`, `getNavigationBarVisibleRect`, `getNavigationBarStableRect`
  - 색상/아이콘: `setStatusBarColor`, `setNavigationBarColor`, `setStatusBarDarkIcon`, `setNavigationBarDarkIcon`
  - 가시성: `setStatusBarVisible/Gone`, `setNavigationBarVisible/Gone`
  - 공통: `setEdgeToEdgeMode`, `isEdgeToEdgeEnabled`, `onDestroy`
- 상태 모델
  - `SystemBarVisibleState`, `SystemBarStableState`
- 진입/종료 확장 함수
  - `Window.getSystemBarController()`
  - `Window.destroySystemBarControllerCache()`
- API 35+ 오버레이 처리
  - 색상 오버레이 생성/재사용/동기화/정리

### 제외 범위
- Compose 전용 시스템 바 API
- 시스템 바 애니메이션 프레임 제어
- 제조사별 UX 튜닝 자동화
- 다중 Window 간 동기화 오케스트레이션

## 사용자/이해관계자
- 라이브러리 사용자(앱 개발자): Activity/Fragment/Dialog에서 안전한 시스템 바 제어 필요
- 라이브러리 유지보수자: SDK 분기와 상태 의미를 일관되게 유지해야 함
- QA/리뷰어: 상태 계약과 테스트 증적을 빠르게 검증해야 함

## 핵심 시나리오
1. Activity 진입 시 `window.getSystemBarController()`로 컨트롤러를 얻고 색상/아이콘/가시성을 제어한다.
2. 회전/멀티윈도우 전환 시 상태 조회 API가 크래시 없이 명시적 상태(`NotReady/NotPresent/Hidden/Visible/Stable`)를 반환한다.
3. API 35+에서 색상 적용 시 내부 오버레이가 재사용되고, 종료 시 정리된다.
4. 기존 호출부는 `Rect?` API를 유지하면서 점진적으로 상태 API로 이관한다.

## 요구사항 (우선순위)

### P0 (필수)
- FR-01 진입/캐시 계약
  - `Window.getSystemBarController()`는 Window당 1개 인스턴스를 캐시한다.
  - `Window.destroySystemBarControllerCache()`는 `onDestroy()` 호출 후 캐시를 제거한다.
- FR-02 상태 의미 계약
  - visible: `NotReady`, `NotPresent`, `Hidden`, `Visible(rect)`
  - stable: `NotReady`, `NotPresent`, `Stable(rect)`
  - StatusBar/NavigationBar 모두 동일 의미 체계를 따른다.
- FR-03 하위 호환 계약
  - `Rect?` API는 상태 API 매핑으로 동작한다.
  - `NotReady -> null`, `NotPresent/Hidden -> Rect()`, `Visible/Stable -> rect`.
- FR-04 API 35+ 색상 처리
  - 상태바/내비게이션바 색상은 오버레이 기반으로 반영한다.
  - 오버레이는 중복 생성 없이 재사용한다.
- FR-05 수명주기 정리
  - `onDestroy()`에서 status/nav 오버레이와 insets listener를 제거한다.

### P1 (중요)
- FR-06 가시성 제어
  - API 30+는 `WindowInsetsControllerCompat.show/hide`를 사용한다.
  - API 29-는 legacy 플래그 경로를 사용한다.
- FR-07 Edge-to-edge 토글
  - `setEdgeToEdgeMode(enabled)`로 `WindowCompat.setDecorFitsSystemWindows(window, !enabled)`를 적용한다.
  - `isEdgeToEdgeEnabled()`로 마지막 설정값을 조회 가능해야 한다.
- FR-08 Insets 미준비 폴백
  - API 35+ 색상 적용 시 root insets가 아직 없으면 `WindowInsetsCompat.CONSUMED`를 폴백으로 사용한다.
  - 초기 높이/너비가 0으로 붙더라도 이후 `requestApplyInsets`와 insets listener로 보정 가능해야 한다.

### P2 (개선)
- FR-09 문서/테스트 추적성
  - PRD/SPEC/PLAN/README 용어를 동일하게 유지한다.
  - 상태 계약, 캐시 계약, 오버레이 정리 경로를 테스트로 확인 가능해야 한다.

## 비기능 요구사항
- 안정성: 미준비/예외 상황에서 크래시 대신 명시적 상태 또는 호환 기본값 반환
- 성능:
  - 오버레이 뷰 재사용
  - insets 리스너에서 레이아웃 변경 시에만 `layoutParams` 재할당
- 호환성:
  - minSdk 28
  - API 35+ 색상 오버레이 대응
- 유지보수성:
  - 바 타입별 helper 분리
  - 공통 유틸(`isSystemBarRectReady`, `Insets.isAllZero`) 재사용

## 성공 지표
- 문서 정합성
  - PRD/SPEC/PLAN/README에서 상태 용어와 진입 경로가 동일하다.
- 기능 정합성
  - 상태 API와 legacy API 매핑 결과가 코드 계약과 일치한다.
- 회귀 안정성
  - `:simple_xml:testDebugUnitTest` 통과
  - systembar 관련 Robolectric 테스트 통과

## 수용 기준 (Acceptance Criteria)
1. 개발자가 `window.getSystemBarController()`만으로 상태 조회/색상/가시성 제어를 수행할 수 있다.
2. `getStatusBarVisibleState()`와 `getNavigationBarVisibleState()`가 동일 의미 계약(`NotReady/NotPresent/Hidden/Visible`)을 따른다.
3. API 35+에서 색상 적용 후 `onDestroy()` 또는 `destroySystemBarControllerCache()` 호출 시 오버레이 리소스가 정리된다.
4. 기존 `Rect?` API 사용 코드는 동작을 유지한다.

## 제약/전제
- 컨트롤러는 `Window`가 유효하고 `decorView`가 존재한다는 전제를 가진다.
- 호출 타이밍이 너무 이르면 `WindowInsets` 미준비로 `NotReady`가 반환될 수 있다.
- `isEdgeToEdgeEnabled()`는 컨트롤러 내부 플래그 기준이며 외부 직접 변경과 완전 동기화하지 않는다.

## 리스크 및 완화 전략
- 리스크: 상태 의미 오해(`NotPresent` vs `Hidden`)
  - 완화: 상태 모델/표/예제 동시 문서화
- 리스크: API 35+ 오버레이 정리 누락
  - 완화: `destroySystemBarControllerCache()` 사용 경로를 기본 종료 경로로 가이드
- 리스크: 테스트 환경 한계(Robolectric)
  - 완화: 문서에 한계 명시, 디바이스 검증 시나리오 별도 유지

## 관련 파일
- 코드
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/SystemBarController.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/model/SystemBarState.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/internal/helper/StatusBarHelper.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/internal/helper/NavigationBarHelper.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/extensions/SystemServiceExtensionsXml.kt`
- 문서
  - `simple_xml/docs/feature/system_manager/controller/systembar/SPEC.md`
  - `simple_xml/docs/feature/system_manager/controller/systembar/IMPLEMENTATION_PLAN.md`
  - `docs/readme/system_manager/controller/xml/README_SYSTEMBAR_CONTROLLER.md`

## 테스트
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/system_manager/controller/systembar/internal/helper/SystemBarHelperStateRobolectricTest.kt`
