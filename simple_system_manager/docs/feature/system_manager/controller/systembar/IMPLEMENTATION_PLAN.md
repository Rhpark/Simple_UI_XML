# SystemBar Controller Implementation Plan (As-Is)

## 문서 정보
- 문서명: SystemBar Controller Implementation Plan
- 작성일: 2026-02-09
- 수정일: 2026-02-10
- 대상 모듈: simple_xml
- 패키지: kr.open.library.simple_ui.xml.system_manager.controller.systembar
- 상태: 현행(as-is)

## 목적
- 현재 구현 코드를 기준으로 실행 흐름을 단계별로 재현 가능하게 정리한다.
- 유지보수 시 계약 위반 지점을 빠르게 찾을 수 있도록 검증 포인트를 명시한다.
- PRD/SPEC/README와 구현 정합성을 지속적으로 유지한다.

## 구현 범위
- 진입/캐시
  - `Window.getSystemBarController()`
  - `Window.destroySystemBarControllerCache()`
- 상태 조회
  - state API + legacy Rect API 매핑
- 제어
  - 색상/아이콘/가시성/edge-to-edge
- API 35+ overlay
  - 생성/재사용/insets 동기화/정리
- lifecycle
  - `onDestroy()` 정리

## 단계별 실행 흐름 (코드 기준)

### 1) 진입 및 캐시 획득
1. 호출부가 `window.getSystemBarController()` 호출
2. `decorView.getTag(R.id.tag_system_bar_controller)` 조회
3. 캐시가 있으면 반환
4. 캐시가 없으면 `SystemBarController(window)` 생성 후 tag 저장

검증 포인트
- 같은 Window에서 동일 인스턴스 재사용
- 강제 재생성은 `destroySystemBarControllerCache()` 후 재호출

### 2) 상태 조회 (state API)
1. `getRootWindowInsetsCompat()` 호출
2. StatusBar 조회는 `StatusBarHelper`로 위임
3. NavigationBar 조회는 `NavigationBarHelper`로 위임
4. 결과는 sealed state로 반환

검증 포인트
- 미준비 상태에서 `NotReady`
- 숨김/미존재 구분 일관성
- Navigation 좌/우/하단 좌표 계산 정확성

### 3) 상태 조회 (legacy Rect API)
1. legacy API는 내부적으로 state API 호출
2. `toLegacyRectOrNull()`로 변환
   - `NotReady -> null`
   - `NotPresent/Hidden -> Rect()`
   - `Visible/Stable -> rect`

검증 포인트
- 기존 호출부 하위 호환 유지
- state API 결과와 legacy 매핑 불일치가 없는지 점검

### 4) 색상/아이콘 제어
1. `setStatusBarColor`, `setNavigationBarColor` 호출
2. SDK 분기
   - API 35+: helper overlay 경로
   - API 28~34: window color 직접 설정
3. 아이콘 밝기(`set*DarkIcon`) 적용

검증 포인트
- API 35+에서 오버레이 중복 생성이 없는지
- 색상 반복 변경 시 view 재사용 여부

### 5) 가시성 제어
1. `setStatusBarVisible/Gone`, `setNavigationBarVisible/Gone` 호출
2. SDK 분기
   - API 30+: insets controller show/hide
   - API 28~29: legacy flag 경로

검증 포인트
- 상태바/내비게이션바 토글 후 상태 조회 결과 일관성
- 구형 SDK 분기에서 side effect(immersive flag) 확인

### 6) Edge-to-edge 제어
1. `setEdgeToEdgeMode(enabled)` 호출
2. `WindowCompat.setDecorFitsSystemWindows(window, !enabled)` 적용
3. 내부 플래그 갱신, `isEdgeToEdgeEnabled()`로 반환

검증 포인트
- 토글 반복 시 플래그와 반환값 일치
- 외부 직접 변경과의 불일치 가능성 문서화

### 7) API 35+ overlay 동기화
1. 최초 색상 적용 시 overlay attach
2. insets listener 연결
   - Status: height 중심 갱신
   - Navigation: width/height/gravity 갱신
3. 변경이 있을 때만 layoutParams 재할당
4. 필요 시 `requestApplyInsets` 트리거

검증 포인트
- 회전 시 overlay 위치/크기 정상 추종
- listener 중복 등록이 없는지

### 8) 종료/정리
1. 종료 시 `window.destroySystemBarControllerCache()` 권장
2. 내부적으로 `controller.onDestroy()` 실행
3. status/nav overlay listener 제거 + view 제거
4. decorView tag 제거

검증 포인트
- 중복 정리 호출 시 크래시 없음
- 종료 후 재생성 시 정상 동작

## 운영 체크리스트
- 기본 진입 경로는 항상 `Window.getSystemBarController()` 사용
- 직접 생성과 캐시 경로를 혼용하지 않음
- state API를 우선 사용하고 legacy API는 하위 호환 용도로 제한
- API 35+ 사용 화면은 종료 시 cache destroy 경로를 명시
- 문서(PRD/SPEC/PLAN/README) 용어 변경 시 동시 업데이트

## 테스트/검증 계획

### 로컬 검증 명령
- `./gradlew :simple_xml:testRobolectric --tests "*SystemBarHelperStateRobolectricTest"`
- `./gradlew :simple_xml:testRobolectric`

### 검증 항목
- 상태 계약
  - `NotReady`, `NotPresent`, `Visible`, `Stable` 반환
  - navigation 좌표(bottom/left/right)
- 안정성
  - 예외 시 기본 상태 폴백
  - overlay cleanup 경로 안정성
- 테스트 환경 한계 인지
  - Robolectric 제약으로 Hidden/Navigation Stable 일부 시나리오는 완전 재현되지 않음을 전제로 해석
- 회귀
  - simple_xml 전체 unit/robolectric 회귀

## 오류 처리/로그 정책
- `SystemBarController`의 insets 획득은 `tryCatchSystemManager(default = null)`로 보호한다.
- helper 상태 계산/정리는 `safeCatch` 기반으로 보호한다.
- 예외 발생 시 크래시 대신 `NotReady` 또는 호환 기본값(`null`, `Rect()`)을 반환한다.
- 예외 정보는 내부 로깅(`Logx`) 경로에서 추적 가능해야 한다.

## 문서 동기화 규칙
- 코드 계약 변경 시 다음 문서를 같은 PR/커밋 범위에서 동시 갱신
  - `PRD.md`
  - `SPEC.md`
  - `IMPLEMENTATION_PLAN.md`
  - `docs/readme/system_manager/controller/xml/README_SYSTEMBAR_CONTROLLER.md`
  - 필요 시 `docs/readme/README_ACTIVITY_FRAGMENT.md`

## 개선 백로그 (선택)
- controller/cache 전용 테스트 보강
  - 동일 Window 재사용 검증
  - destroy 후 재생성 검증
- known risk 명시 강화
  - `systemBarsBehavior` overwrite 정책 옵션화 검토
  - edge-to-edge 외부 변경 동기화 전략 검토

## 산출물 기준
- 문서 기준으로 구현 재현이 가능해야 함
- 요구사항 -> 명세 -> 구현 단계 -> 테스트 증적이 추적 가능해야 함
- 상태 의미(미준비/미존재/숨김/표시)가 호출부에서 오해 없이 해석 가능해야 함
