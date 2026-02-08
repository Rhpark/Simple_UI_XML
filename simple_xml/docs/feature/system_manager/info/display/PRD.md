# Display Info PRD

## 문서 정보
- 문서명: Display Info PRD
- 작성일: 2026-02-08
- 수정일: 2026-02-08
- 대상 모듈: simple_xml
- 패키지: kr.open.library.simple_ui.xml.system_manager.info.display
- 상태: 현행(as-is)

## 배경/문제 정의
- Android 디스플레이 API는 API 30(R) 전후로 `DisplayMetrics`와 `WindowMetrics/WindowInsets` 사용 방식이 달라 버전 분기 코드가 반복됩니다.
- 물리 화면 크기와 앱 윈도우 크기(멀티윈도우 반영)는 의미가 달라, 호출부에서 개념이 섞이면 UI 계산 오류가 발생하기 쉽습니다.
- 상태바/내비게이션 바 크기와 insets는 기기/방향/내비게이션 모드(버튼/제스처)에 따라 값 형태가 달라집니다.
- 일부 값은 측정 불가(`null`)와 점유 영역 없음(`0`)을 구분해야 하지만, 호출부에서 정책이 일관되지 않으면 오해가 발생합니다.

## 목표
- 디스플레이 정보 조회를 `DisplayInfo` 단일 API로 제공한다.
- API 30+와 API 28~29의 분기 로직을 내부로 캡슐화한다.
- 물리 화면, 앱 윈도우, 시스템 바 크기/insets를 일관된 모델(`DisplayInfoSize`, `DisplayInfoBarInsets`)로 제공한다.
- `null`과 `0`의 의미를 구분해 호출부의 해석 일관성을 높인다.
- 예외 상황에서 기본값 또는 `null` 반환으로 크래시를 방지한다.

## 비목표
- 화면 회전/멀티윈도우 변경 이벤트 스트림 제공
- 시스템 바/윈도우 레이아웃을 자동으로 조정하는 UI 제어 기능 제공
- 제조사별 특수 디스플레이 정책 튜닝
- Compose 전용 디스플레이 API 제공

## 범위

### 포함 범위
- `DisplayInfo` 공개 API
  - 화면 크기: `getPhysicalScreenSize`, `getAppWindowSize`
  - 시스템 바 크기: `getStatusBarSize`, `getNavigationBarSize`
  - 시스템 바 insets: `getStatusBarStableInsets`, `getNavigationBarStableInsets`
  - 상태/유틸: `isPortrait`, `isLandscape`, `isInMultiWindowMode`
- 값 객체
  - `DisplayInfoSize`
  - `DisplayInfoBarInsets` (`thickness`, `isEmpty`)
- 단위 변환 확장 함수
  - `Number.dpToPx`, `Number.pxToDp`, `Number.dpToSp`, `Number.spToDp`, `Number.spToPx`, `Number.pxToSp`
- 확장 함수
  - `Context.getDisplayInfo()`

### 제외 범위
- Activity/Fragment UI 렌더링 코드
- 시스템 바 색상/가시성 제어(Controller 영역)
- 화면 크기 변경 콜백/관찰 스트림
- 별도 캐시/싱글턴 수명주기 관리

## 핵심 기능

### 1) 화면 크기 조회
- `getPhysicalScreenSize()`: 멀티윈도우 여부와 무관한 물리 화면 크기 반환
- `getAppWindowSize(activity?)`: 현재 앱 윈도우 크기 반환(멀티윈도우 반영)

### 2) 시스템 바 크기 조회
- `getStatusBarSize()`: 상태바 크기 반환
- `getNavigationBarSize()`: 내비게이션 바 크기 반환

### 3) 시스템 바 insets 조회
- `getStatusBarStableInsets(activity?)`
- `getNavigationBarStableInsets(activity?)`

### 4) 화면 상태 조회
- `isPortrait()`, `isLandscape()`
- `isInMultiWindowMode(activity)`

## 반환 계약(핵심)
- `null`: 측정 불가(예: API 28~29에서 Activity 누락, Insets 미수신, 리소스 조회 실패)
- `DisplayInfoSize(0, 0)` 또는 `DisplayInfoBarInsets(0,0,0,0)`: 측정 성공 + 시스템 바 점유 영역 없음
- `getPhysicalScreenSize()`는 non-null이며 예외 fallback으로 `(0,0)`을 반환할 수 있음

## 예외 처리 정책
- 공개 API는 `tryCatchSystemManager`로 예외를 보호한다.
- 내비게이션 바 리소스가 모두 없는 경우 로그(`Logx.e`)를 남기고 `null`을 반환한다.
- 메인 스레드가 필요한 메서드(`activity.window.decorView` 경로)는 `@MainThread` 계약으로 명시한다.

## 비기능 요구사항
- 안정성: 예외 상황에서도 크래시 대신 기본값/`null` 반환
- 성능: 단순 조회 API(폴링/백그라운드 루프 없음), 필요 시점 호출 기반
- 호환성: minSdk 28, API 30 기준으로 분기 처리
- 유지보수성: 리소스 키 상수화, 데이터 클래스 기반 결과 모델 분리

## 제약/전제
- API 28~29에서 `getAppWindowSize`/stable insets는 Activity 전달 여부에 따라 결과가 달라진다.
- 앱 초기 시점(뷰 미측정/Insets 미수신)에는 `null` 또는 0 계열 값이 반환될 수 있다.
- 리소스 기반 추정값(`status_bar_height`, `navigation_bar_*`)은 기기/환경에 따라 unavailable일 수 있다.
- `Context.getDisplayInfo()`는 호출마다 새 인스턴스를 생성한다.

## 성공 기준
- API 28~29 / 30+ 모두에서 동일한 공개 API로 화면/바 정보를 조회할 수 있다.
- 반환 의미(`null` vs `0`)가 문서와 구현에서 일치한다.
- 주요 메서드 동작이 단위/로보일렉트릭 테스트로 검증된다.
- README와 PRD/SPEC/PLAN의 용어가 정합하게 유지된다.

## 관련 파일
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/info/display/DisplayInfo.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/info/display/DisplayInfoSize.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/info/display/DisplayInfoBarInsets.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/extensions/SystemServiceExtensionsXml.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/extensions/display/DisplayUnitExtensions.kt`
- `docs/readme/system_manager/info/xml/README_DISPLAY_INFO.md`

## 테스트
- Unit
  - `simple_xml/src/test/java/kr/open/library/simple_ui/xml/unit/system_manager/info/display/DisplayInfoSizeTest.kt` (12)
  - `simple_xml/src/test/java/kr/open/library/simple_ui/xml/unit/system_manager/info/display/DisplayInfoBarInsetsTest.kt` (26)
- Robolectric
  - `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/system_manager/info/display/DisplayInfoRobolectricTest.kt` (41)
  - `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/presenter/extensions/display/DisplayUnitExtensionsRobolectricTest.kt` (36)
  - `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/system_manager/extensions/SystemServiceExtensionsTest.kt` (생성 확장 포함)
