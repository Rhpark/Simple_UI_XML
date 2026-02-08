# Display Info Implementation Plan (As-Is)

## 문서 정보
- 문서명: Display Info Implementation Plan
- 작성일: 2026-02-08
- 수정일: 2026-02-08
- 대상 모듈: simple_xml
- 패키지: kr.open.library.simple_ui.xml.system_manager.info.display
- 상태: 현행(as-is)

## 목표
- 현재 구현 코드를 기준으로 Display Info 모듈의 실행 흐름을 단계별로 문서화한다.
- 신규 기능 개발 계획이 아니라, 현행 동작 계약을 재현 가능한 형태로 정리한다.

## 구현 범위(현행)
- `DisplayInfo` 공개 API 전반
- API 30+ / API 28~29 분기 로직
- 시스템 바 크기 및 stable insets 산출
- `DisplayInfoSize`, `DisplayInfoBarInsets` 데이터 모델
- `DisplayUnitExtensions` 단위 변환 확장 함수 6종 (`dpToPx`, `pxToDp`, `dpToSp`, `spToDp`, `spToPx`, `pxToSp`)
- `Context.getDisplayInfo()` 생성 확장
- 관련 Unit/Robolectric 테스트

## 구현 상세(파일 기준)

### 1) 인스턴스 초기화
1. `DisplayInfo(context)` 생성
2. `BaseSystemService(context, null)` 초기화
3. `WindowManager` lazy 획득(`context.getWindowManager()`)
4. 리소스 키 상수 준비
   - `status_bar_height`
   - `navigation_bar_height`
   - `navigation_bar_height_landscape`
   - `navigation_bar_width`

### 2) 물리 화면 크기 조회(`getPhysicalScreenSize`)
1. `checkSdkVersion(R)` 분기
2. API 30+:
   - `maximumWindowMetrics.bounds` 사용
3. API 28~29:
   - `defaultDisplay.getRealMetrics` 사용
4. 결과를 `DisplayInfoSize(width, height)`로 반환
5. 예외 시 fallback 기본값 `(0,0)` 반환

### 3) 앱 윈도우 크기 조회(`getAppWindowSize`)
1. `@MainThread` 계약 진입
2. `checkSdkVersion(R)` 분기
3. API 30+:
   - `currentWindowMetrics.bounds` 반환
4. API 28~29:
   - `activity == null`이면 `null`
   - `decorView` 크기 유효 시 해당 값 반환
   - 유효하지 않으면 `resources.displayMetrics` fallback 반환

### 4) 상태바 크기 조회(`getStatusBarSize`)
1. `checkSdkVersion(R)` 분기
2. API 30+:
   - `maximumWindowMetrics` + `statusBars` insets 사용
   - `top > 0`이면 `(물리폭, top)`
   - 아니면 `(0,0)` 정규화
3. API 28~29:
   - `status_bar_height` 리소스 조회
   - 리소스 미존재: `null`
   - 높이 > 0: `(물리폭, 높이)`
   - 높이 == 0: `(0,0)`

### 5) 내비게이션 바 크기 조회(`getNavigationBarSize`)
1. `checkSdkVersion(R)` 분기
2. API 30+(`getNavigationBarSizeSdkR`)
   - `maximumWindowMetrics` + `navigationBars` insets 사용
   - 방향 우선순위: bottom -> top -> left -> right
   - 모두 0이면 `(0,0)`
3. API 28~29(`getNavigationBarSizeSdkNormal`)
   - 리소스 3종 조회
   - 모두 미존재면 `Logx.e` + `null`
   - 세로/가로/기타 방향별 규칙으로 `(width,height)` 산출
   - 점유 영역 없음은 `(0,0)`으로 정규화

### 6) 화면 상태 조회
- `isPortrait()` / `isLandscape()`
  - `Configuration.orientation` 기반 boolean 반환
- `isInMultiWindowMode(activity)`
  - `@MainThread` 계약
  - Activity의 `isInMultiWindowMode` 직접 반환

### 7) stable insets 조회
- `getNavigationBarStableInsets(activity?)`
  1. `@MainThread` 계약
  2. API 30+: `currentWindowMetrics.windowInsets.navigationBars`
  3. API 28~29:
     - `activity` 또는 `rootWindowInsets` 없으면 `null`
     - `top=0`, `bottom/left/right`는 `stableInset*` 사용
- `getStatusBarStableInsets(activity?)`
  1. `@MainThread` 계약
  2. API 30+: `currentWindowMetrics.windowInsets.statusBars`
  3. API 28~29:
     - `activity` 또는 `rootWindowInsets` 없으면 `null`
     - `top=stableInsetTop`, 나머지 0

### 8) 데이터 모델 동작
- `DisplayInfoSize`
  - width/height 보관용 값 객체
- `DisplayInfoBarInsets`
  - top/bottom/left/right 보관용 값 객체
  - `thickness = maxOf(left, top, right, bottom)`
  - `isEmpty = thickness == 0`

### 9) 확장 함수 진입점
- `Context.getDisplayInfo()` 호출 시 새 `DisplayInfo` 인스턴스 반환
- 내부 캐시/싱글턴 없음

### 10) 오류 처리/로깅 정책
- 공개 API는 `tryCatchSystemManager`로 보호
- 측정 불가 시 메서드별 기본값/`null` 반환
- 내비게이션 리소스 미존재는 `Logx.e`로 기록

## 구현 흐름 요약
1. 호출부가 `context.getDisplayInfo()` 또는 `DisplayInfo(context)` 생성
2. 공개 API 호출 시 SDK 분기 수행
3. 플랫폼 API/리소스로 값 측정
4. 결과를 `DisplayInfoSize` 또는 `DisplayInfoBarInsets`로 정규화
5. 측정 불가 시 `null`, 점유 영역 없음 시 0 계열 값 반환

## 테스트 현황(현행)
- Unit
  - `DisplayInfoSizeTest` (12)
  - `DisplayInfoBarInsetsTest` (26)
- Robolectric
  - `DisplayInfoRobolectricTest` (41)
  - `DisplayUnitExtensionsRobolectricTest` (36)
  - `SystemServiceExtensionsTest` 내 `getDisplayInfo` 생성 검증

## 운영/유지보수 체크리스트
- API 28~29에서 앱 윈도우/Insets 측정 시 Activity 전달 여부를 확인할 것
- `null`과 0 반환 의미를 호출부 로직에서 분리해서 처리할 것
- `activity.window.decorView` 경로 메서드는 메인 스레드에서 호출할 것
- README/PRD/SPEC/PLAN의 반환 계약 문구를 동일하게 유지할 것
