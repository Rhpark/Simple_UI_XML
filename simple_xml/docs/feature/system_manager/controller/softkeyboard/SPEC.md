# SoftKeyboard Controller SPEC

## 문서 정보
- 문서명: SoftKeyboard Controller SPEC
- 작성일: 2026-02-07
- 수정일: 2026-02-07
- 대상 모듈: simple_xml
- 패키지: kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard
- 수준: 구현 재현 가능 수준(Implementation-ready)
- 상태: 현행(as-is)

## 전제/참조
- 기본 규칙/환경: 루트 AGENTS.md, `docs/rules/*_RULE.md`
- 요구사항/범위: `PRD.md`
- 사용 가이드: `docs/readme/system_manager/controller/xml/README_SOFTKEYBOARD_CONTROLLER.md`

## 모듈 구조 및 책임
- `SoftKeyboardController`
  - 공개 API 제공
  - 요청 발행, 가시성 대기, resize 정책, 스타일러스 처리
- `SoftKeyboardActionResult`
  - 실제 가시성 대기 API의 결과 모델
- `SoftKeyboardFailureReason`
  - 실패 코드 집합
- `SoftKeyboardResizePolicy`
  - resize 정책 집합
- `Context.getSoftKeyboardController()`
  - Controller 생성 확장 함수(호출 시 새 인스턴스 생성)

## 공개 API 시그니처

```kotlin
public open class SoftKeyboardController(context: Context) : BaseSystemService(context, null) {
    companion object {
        const val DEFAULT_IME_VISIBILITY_TIMEOUT_MS: Long = 700L
    }

    fun setAdjustPan(window: Window): Boolean
    fun setSoftInputMode(window: Window, softInputTypes: Int): Boolean

    fun show(v: View, flag: Int = InputMethodManager.SHOW_IMPLICIT): Boolean
    fun showDelay(v: View, delay: Long, flag: Int = InputMethodManager.SHOW_IMPLICIT): Boolean
    suspend fun showAwait(
        v: View,
        delayMillis: Long = 0L,
        flag: Int = InputMethodManager.SHOW_IMPLICIT,
        timeoutMillis: Long = DEFAULT_IME_VISIBILITY_TIMEOUT_MS,
    ): SoftKeyboardActionResult
    fun showAwaitAsync(
        v: View,
        coroutineScope: CoroutineScope,
        delayMillis: Long = 0L,
        flag: Int = InputMethodManager.SHOW_IMPLICIT,
        timeoutMillis: Long = DEFAULT_IME_VISIBILITY_TIMEOUT_MS,
    ): Deferred<SoftKeyboardActionResult>

    fun hide(v: View, flag: Int = 0): Boolean
    fun hideDelay(v: View, delay: Long, flag: Int = 0): Boolean
    suspend fun hideAwait(
        v: View,
        delayMillis: Long = 0L,
        flag: Int = 0,
        timeoutMillis: Long = DEFAULT_IME_VISIBILITY_TIMEOUT_MS,
    ): SoftKeyboardActionResult
    fun hideAwaitAsync(
        v: View,
        coroutineScope: CoroutineScope,
        delayMillis: Long = 0L,
        flag: Int = 0,
        timeoutMillis: Long = DEFAULT_IME_VISIBILITY_TIMEOUT_MS,
    ): Deferred<SoftKeyboardActionResult>?

    fun configureImeResize(window: Window, policy: SoftKeyboardResizePolicy = SoftKeyboardResizePolicy.KEEP_CURRENT_WINDOW): Boolean
    fun setAdjustResize(window: Window): Boolean

    @RequiresApi(TIRAMISU) fun startStylusHandwriting(v: View): Boolean
    @RequiresApi(TIRAMISU) fun startStylusHandwriting(v: View, delay: Long): Boolean
}
```

## 계약 규칙

### 공통 규칙
- 모든 공개 API는 `@MainThread` 계약을 가진다.
- 런타임에서 `ensureMainThread()`로 재검증한다.
- 오프메인 호출 시 경고 로그를 남기고 즉시 실패를 반환한다.
- 모든 공개 API는 `tryCatchSystemManager(defaultValue)`로 예외를 보호한다.

### 결과 계약 규칙
- `show/hide`
  - 요청 경로 호출 성공 여부만 반환
- `showDelay/hideDelay`
  - 큐 등록 성공 여부만 반환
- `showAwait/hideAwait`
  - 실제 가시성 대기 결과 반환
  - `Timeout`은 제한 시간 내 미관측 의미
- `showAwaitAsync`
  - 내부적으로 `Dispatchers.Main.immediate`에서 `showAwait` 실행
  - 오프메인 호출 시 `Failure(OFF_MAIN_THREAD)`를 담은 non-null `Deferred` 반환
- `hideAwaitAsync`
  - 내부적으로 `Dispatchers.Main.immediate`에서 `hideAwait` 실행
  - 오프메인 호출 또는 예외 발생 시 `null` 반환 가능

### 인자 검증 규칙
- `showAwait/hideAwait`
  - `delayMillis < 0` 또는 `timeoutMillis <= 0`이면 `Failure(INVALID_ARGUMENT)`
- `showDelay/hideDelay/startStylusHandwriting(delay)`
  - `delay < 0`이면 `false`

### 취소/예외 규칙
- `showAwait/hideAwait`는 `CancellationException`을 재던진다.
- 일반 예외는 `Failure(EXCEPTION_OCCURRED)`로 변환한다.

## 내부 동작 계약

### `requestShowInternal(v, flag)`
- `ViewCompat.getWindowInsetsController(v)?.show(Type.ime())` 호출
- `imm.showSoftInput(v, flag)` 호출
- 반환값: `controller != null || immResult`

### `requestHideInternal(v, flag)`
- `controller?.hide(Type.ime())` 호출
- `windowToken ?: applicationWindowToken` 확보 시 IMM hide 시도
- 토큰과 컨트롤러 모두 없으면 `WINDOW_TOKEN_MISSING`
- 반환값
  - `controller != null || immResult`면 `REQUEST_ISSUED`
  - 아니면 `REQUEST_REJECTED`

### `awaitImeVisibility(v, expectedVisible, timeoutMillis)`
- 초기 상태가 기대값이면 즉시 `true`
- `OnGlobalLayoutListener` 등록 후 콜백 기반 대기
- 콜백 미도달 구간은 50ms 폴백 폴링으로 보조 확인
- timeout 내 기대값 미도달 시 `false`
- `finally`에서 리스너 제거

### `isImeVisible(v)`
- `ViewCompat.getRootWindowInsets(v)?.isVisible(Type.ime()) == true` 기준

## 실패 사유 매핑
- `OFF_MAIN_THREAD`
  - 오프메인 호출
- `INVALID_ARGUMENT`
  - await API 인자 조건 불만족
- `FOCUS_REQUEST_FAILED`
  - `showAwait`에서 `requestFocus()` 실패
- `WINDOW_TOKEN_MISSING`
  - hide 경로에서 토큰/컨트롤러 모두 부재
- `IME_REQUEST_REJECTED`
  - 요청 발행 실패 또는 hide 요청 거절
- `EXCEPTION_OCCURRED`
  - 일반 예외 포착

## resize 정책 규칙
- `KEEP_CURRENT_WINDOW`
  - API 30+: no-op(현재 윈도우 정책 유지)
  - API 29-: `SOFT_INPUT_ADJUST_RESIZE`
- `LEGACY_ADJUST_RESIZE`
  - 전 API에서 `SOFT_INPUT_ADJUST_RESIZE`
- `FORCE_DECOR_FITS_TRUE`
  - API 30+: `WindowCompat.setDecorFitsSystemWindows(window, true)`
  - API 29-: `SOFT_INPUT_ADJUST_RESIZE`

## 상수/기본값
- `DEFAULT_IME_VISIBILITY_TIMEOUT_MS = 700L`
- `IME_FALLBACK_POLL_INTERVAL_MS = 50L` (private)

## 테스트 명세(현행)
- 테스트 파일
  - `SoftKeyboardControllerRobolectricTest` (Robolectric / 53개)
  - `SoftKeyboardActionResultTest` (Unit / 23개)
  - `SoftKeyboardResizePolicyTest` (Unit / 10개)
- 테스트 수
  - 86개 (Robolectric 53 + Unit 33)
- 검증 축
  - 윈도우 모드/정책 분기
  - show/hide 성공·실패
  - delay 등록 성공·실패
  - 예외 처리 fallback
  - 오프메인 처리
  - await 인자 검증
  - hide 실패 사유 매핑
  - 코루틴 취소 전파
  - Timeout/EXCEPTION_OCCURRED 경로
  - delay/timeout 경계값(0, 음수)
  - configureImeResize API 29 분기
  - SoftKeyboardActionResult sealed 모델 검증
  - SoftKeyboardFailureReason enum 검증
  - SoftKeyboardResizePolicy enum 검증

## 알려진 한계
- Robolectric만으로 제조사 IME별 실제 동작을 완전 재현할 수 없다.
- `KEEP_CURRENT_WINDOW`는 API 30+에서 설정 변경 없이 성공을 반환한다.
- `Context.getSoftKeyboardController()`는 호출마다 새 인스턴스를 생성한다.
