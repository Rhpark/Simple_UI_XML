# Display Info SPEC

## 문서 정보
- 문서명: Display Info SPEC
- 작성일: 2026-02-08
- 수정일: 2026-02-08
- 대상 모듈: simple_xml
- 패키지: kr.open.library.simple_ui.xml.system_manager.info.display
- 수준: 구현 재현 가능 수준(Implementation-ready)
- 상태: 현행(as-is)

## 전제/참조
- 기본 규칙/환경은 루트 `AGENTS.md`, `docs/rules/*_RULE.md`를 따른다.
- 요구사항/범위는 `PRD.md`를 따른다.
- 사용 가이드는 `docs/readme/system_manager/info/xml/README_DISPLAY_INFO.md`를 따른다.

## 모듈 구조 및 책임
- `DisplayInfo`
  - 디스플레이 정보 공개 API
  - SDK 분기, 리소스 조회, WindowInsets 조회를 내부 처리
- `DisplayInfoSize`
  - 2D 크기 값 객체(`width`, `height`)
- `DisplayInfoBarInsets`
  - 바 insets 값 객체(`top`, `bottom`, `left`, `right`)
  - 파생 속성: `thickness`, `isEmpty`
- `DisplayUnitExtensions`
  - `Number` 확장 함수 6종: `dpToPx`, `pxToDp`, `dpToSp`, `spToDp`, `spToPx`, `pxToSp`
  - Context의 `displayMetrics` 기반 변환
- `Context.getDisplayInfo()`
  - `DisplayInfo` 인스턴스 생성 확장 함수

## 공개 API 시그니처

```kotlin
public open class DisplayInfo(context: Context) : BaseSystemService(context, null) {
    fun getPhysicalScreenSize(): DisplayInfoSize

    @MainThread
    fun getAppWindowSize(activity: Activity? = null): DisplayInfoSize?

    fun getStatusBarSize(): DisplayInfoSize?
    fun getNavigationBarSize(): DisplayInfoSize?

    fun isPortrait(): Boolean
    fun isLandscape(): Boolean

    @MainThread
    fun isInMultiWindowMode(activity: Activity): Boolean

    @MainThread
    fun getNavigationBarStableInsets(activity: Activity? = null): DisplayInfoBarInsets?

    @MainThread
    fun getStatusBarStableInsets(activity: Activity? = null): DisplayInfoBarInsets?
}

public data class DisplayInfoSize(
    val width: Int,
    val height: Int
)

public data class DisplayInfoBarInsets(
    val top: Int,
    val bottom: Int,
    val left: Int,
    val right: Int
) {
    val thickness: Int
    val isEmpty: Boolean
}

public fun Context.getDisplayInfo(): DisplayInfo
```

## 공통 동작 규칙

### 1) SDK 분기
- 기준: `Build.VERSION_CODES.R` (API 30)
- API 30+
  - `WindowMetrics` + `WindowInsets` 기반 측정
- API 28~29
  - `DisplayMetrics`/`defaultDisplay` 및 시스템 리소스(`status_bar_height`, `navigation_bar_*`) 기반 측정

### 2) 반환 계약
- `null`
  - 측정 불가(예: API 28~29에서 Activity 없음, rootWindowInsets 미도달, 리소스 조회 실패)
- `DisplayInfoSize(0, 0)` / `DisplayInfoBarInsets(0,0,0,0)`
  - 측정 성공 + 시스템 바 점유 영역 없음
- `getPhysicalScreenSize()`
  - non-null 고정
  - 내부 예외 시 fallback 기본값 `(0,0)` 가능

### 3) 스레드 계약
- 아래 메서드는 `@MainThread` 계약:
  - `getAppWindowSize`
  - `isInMultiWindowMode`
  - `getNavigationBarStableInsets`
  - `getStatusBarStableInsets`
- 이유: `activity.window.decorView` 및 Activity 상태 접근

### 4) 오류 처리/로깅
- 공개 API는 `tryCatchSystemManager(defaultValue)`로 예외 보호 (직접 또는 내부 위임 메서드를 통한 간접 보호)
- 내비게이션 리소스를 전혀 찾지 못하면 `Logx.e` 기록 후 `null` 반환

## 메서드별 계약 상세

### `getPhysicalScreenSize()`
- API 30+: `maximumWindowMetrics.bounds`
- API 28~29: `defaultDisplay.getRealMetrics`
- 반환: `DisplayInfoSize` (non-null)

### `getAppWindowSize(activity?)`
- API 30+: `currentWindowMetrics.bounds` 사용 (Activity 파라미터 무시)
- API 28~29:
  - `activity == null`이면 `null`
  - `decorView.width/height > 0`이면 해당 값 반환
  - 아니면 `resources.displayMetrics` 기반 fallback 반환

### `getStatusBarSize()`
- API 30+: `maximumWindowMetrics` + `statusBars` insets
  - `status.top > 0`: `DisplayInfoSize(물리폭, top)`
  - 그 외: `DisplayInfoSize(0,0)`
- API 28~29:
  - `status_bar_height` 리소스 ID 조회
  - ID 없음: `null`
  - 높이 > 0: `DisplayInfoSize(물리폭, 높이)`
  - 높이 == 0: `DisplayInfoSize(0,0)`

### `getNavigationBarSize()`
- API 30+: `getNavigationBarSizeSdkR()`
- API 28~29: `getNavigationBarSizeSdkNormal()`

#### `getNavigationBarSizeSdkR()`
- `maximumWindowMetrics` + `navigationBars` insets 사용
- 우선순위
  - `bottom > 0`: `DisplayInfoSize(물리폭, bottom)`
  - `top > 0`: `DisplayInfoSize(물리폭, top)`
  - `left > 0`: `DisplayInfoSize(left, 물리높이)`
  - `right > 0`: `DisplayInfoSize(right, 물리높이)`
  - 모두 0: `DisplayInfoSize(0,0)`

#### `getNavigationBarSizeSdkNormal()`
- 리소스 후보
  - `navigation_bar_height`
  - `navigation_bar_height_landscape`
  - `navigation_bar_width`
- 모두 미존재: `Logx.e` 후 `null`
- 방향별 규칙
  - 세로:
    - 세로 높이 null: `null`
    - 높이 > 0: `DisplayInfoSize(물리폭, 높이)`
    - 높이 == 0: `DisplayInfoSize(0,0)`
  - 가로:
    - `side > 0 && (bottom <= 0 || side >= bottom)`이면 측면 우선
    - 아니면 하단 우선
    - 둘 다 0이면 `DisplayInfoSize(0,0)`
  - 기타 방향(UNDEFINED/SQUARE):
    - `vertical > 0` 우선, 다음 `side > 0`, 모두 0이면 `(0,0)`

### `isPortrait()` / `isLandscape()`
- `context.resources.configuration.orientation` 비교 결과 반환

### `isInMultiWindowMode(activity)`
- `activity.isInMultiWindowMode` 반환

### `getNavigationBarStableInsets(activity?)`
- API 30+: `currentWindowMetrics.windowInsets.navigationBars`
- API 28~29:
  - `activity == null` 또는 `rootWindowInsets == null`: `null`
  - `DisplayInfoBarInsets(top=0, bottom=stableInsetBottom, left=stableInsetLeft, right=stableInsetRight)`

### `getStatusBarStableInsets(activity?)`
- API 30+: `currentWindowMetrics.windowInsets.statusBars`
- API 28~29:
  - `activity == null` 또는 `rootWindowInsets == null`: `null`
  - `DisplayInfoBarInsets(top=stableInsetTop, bottom=0, left=0, right=0)`

## 데이터 모델 계약

### `DisplayInfoSize`
- 단순 불변 값 객체
- 음수 값 검증 로직 없음(호출 측 계약)

### `DisplayInfoBarInsets`
- 단순 불변 값 객체
- `thickness = maxOf(left, top, right, bottom)`
- `isEmpty = thickness == 0`

## 테스트 명세(현행)
- Unit
  - `DisplayInfoSizeTest` (12)
  - `DisplayInfoBarInsetsTest` (26)
- Robolectric
  - `DisplayInfoRobolectricTest` (41)
  - `DisplayUnitExtensionsRobolectricTest` (36)
  - `SystemServiceExtensionsTest`에서 `getDisplayInfo` 생성 경로 검증(교차 테스트)
- 검증 축
  - API 33 / API 28 분기 동작
  - nullable/non-null 계약
  - 반환 계약(null vs zero): 메서드별 null 조건과 (0,0) 의미 검증
  - 방향/일관성(physical >= app window) 기본 검증
  - 데이터 클래스 동등성/복사/파생 속성 검증

## 알려진 한계
- API 28~29의 일부 값은 리소스 기반 추정치로 측정 정밀도가 기기별로 다를 수 있다.
- 초기 레이아웃 시점에는 insets 미도달로 `null`이 반환될 수 있다.
- `getPhysicalScreenSize()`의 `(0,0)`은 점유 영역 없음이 아닌 예외 fallback 의미일 수 있다.
