# SoftKeyboard Controller Implementation Plan (As-Is)

## 문서 정보
- 문서명: SoftKeyboard Controller Implementation Plan
- 작성일: 2026-02-07
- 수정일: 2026-02-07
- 대상 모듈: simple_xml
- 패키지: kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard
- 상태: 현행(as-is)

## 목표
- 현재 구현 코드를 기준으로 SoftKeyboardController의 실행 흐름을 단계별로 문서화한다.
- PRD/SPEC/README와 구현 계약의 정합성을 유지한다.

## 구현 범위(현행)
- 요청 API: `show/hide`
- 지연 API: `showDelay/hideDelay`
- 실제 대기 API: `showAwait/hideAwait`, `showAwaitAsync/hideAwaitAsync`
- resize API: `configureImeResize`, `setAdjustResize`, `setAdjustPan`, `setSoftInputMode`
- 스타일러스 API: `startStylusHandwriting`(API 33+)
- 결과 모델/실패 코드 모델
- Robolectric 테스트

## 구현 단계(코드 기준)

### 1) 인스턴스 초기화
1. `SoftKeyboardController(context)` 생성
2. `BaseSystemService(context, null)` 초기화
3. `InputMethodManager` lazy 준비
4. 상수 준비
   - 기본 timeout: 700ms
   - 폴백 폴링: 50ms

### 2) 메인 스레드 가드
1. 공개 API 진입
2. `ensureMainThread(methodName)` 호출
3. 오프메인이면 경고 로그 + 즉시 실패 반환
4. 온메인이면 다음 단계 진행

### 3) 요청 API 실행
- `show(v, flag)`
  1. `requestFocus()` 실패 시 `false`
  2. `requestShowInternal(v, flag)` 호출
  3. 결과 반환
- `hide(v, flag)`
  1. `requestHideInternal(v, flag)` 호출
  2. `REQUEST_ISSUED` 여부를 Boolean 반환

### 4) 지연 API 실행
- `showDelay/hideDelay`
  1. delay 음수 검사
  2. `postDelayed(Runnable { show/hide(...) }, delay)` 호출
  3. 큐 등록 결과(Boolean) 반환

### 5) 실제 가시성 대기 API 실행
- `showAwait/hideAwait`
  1. 인자 검사(`delayMillis`, `timeoutMillis`)
  2. 필요 시 delay 대기
  3. 내부 요청 발행
  4. `awaitImeVisibility(...)`로 timeout 내 관찰
  5. 결과 반환
     - 관찰 성공: `Success`
     - 미관찰: `Timeout`
     - 실패 사유: `Failure(reason)`
  6. `CancellationException`은 재던짐

### 6) 비동기 래퍼 실행
- `showAwaitAsync`
  1. 메인 스레드 검사
  2. 오프메인이면 `Failure(OFF_MAIN_THREAD)`를 담은 즉시 완료 Deferred 반환
  3. 온메인이면 `coroutineScope.async(Dispatchers.Main.immediate)`로 `showAwait` 실행
  4. 반환 타입은 non-null `Deferred<SoftKeyboardActionResult>`
- `hideAwaitAsync`
  1. 메인 스레드 검사
  2. `coroutineScope.async(Dispatchers.Main.immediate)`로 `hideAwait` 실행
  3. 반환 타입은 nullable `Deferred<SoftKeyboardActionResult>?`
  4. 오프메인/예외 시 `null` 가능

### 7) 실제 가시성 관찰 알고리즘
1. 현재 가시성 즉시 확인
2. `OnGlobalLayoutListener` 등록
3. timeout 루프 진입
4. 콜백 완료 또는 폴백 폴링으로 기대값 확인
5. 성공 시 `true`, timeout 시 `false`
6. `finally`에서 리스너 제거

### 8) 내부 요청 발행 규칙
- show 경로
  - Insets controller show 시도
  - IMM show 시도
  - 둘 중 하나라도 성공 경로면 요청 성공
- hide 경로
  - Insets controller hide 시도
  - 토큰 확보 시 IMM hide 시도
  - 토큰/컨트롤러 모두 없으면 `WINDOW_TOKEN_MISSING`
  - 요청 거절이면 `REQUEST_REJECTED`

### 9) resize 정책 적용
- `configureImeResize(window, policy)` 분기
- `KEEP_CURRENT_WINDOW`
  - API 30+: no-op
  - API 29-: legacy adjust resize
- `LEGACY_ADJUST_RESIZE`
  - 전 API legacy adjust resize
- `FORCE_DECOR_FITS_TRUE`
  - API 30+: `setDecorFitsSystemWindows(true)`
  - API 29-: legacy adjust resize

### 10) 스타일러스 실행(API 33+)
- `startStylusHandwriting(v)`
  1. `requestFocus()` 성공 시 필기 시작
- `startStylusHandwriting(v, delay)`
  1. delay 음수 검증
  2. `postDelayed`로 지연 실행

## 오류 처리/로그 정책
- 공개 API: `tryCatchSystemManager(defaultValue)`로 보호
- 실패 원인: `SoftKeyboardFailureReason`로 명시
- 로깅: `Logx`로 통일
- 취소: `CancellationException` 전파 유지

## 테스트 현황(현행)
- 테스트 파일: 3개
  - `SoftKeyboardControllerRobolectricTest` (Robolectric / 53개)
  - `SoftKeyboardActionResultTest` (Unit / 23개)
  - `SoftKeyboardResizePolicyTest` (Unit / 10개)
- 테스트 케이스 수: 86개 (Robolectric 53 + Unit 33)
- 검증 항목
  - API 기본 동작
  - delay 인자 검증
  - resize 정책 분기
  - 예외 fallback
  - 오프메인 실패
  - await 실패 사유 매핑
  - 취소 전파
  - Timeout/EXCEPTION_OCCURRED 경로
  - delay/timeout 경계값(0, 음수)
  - configureImeResize API 29 분기
  - SoftKeyboardActionResult sealed 모델 검증
  - SoftKeyboardFailureReason enum 검증
  - SoftKeyboardResizePolicy enum 검증

## 운영/유지보수 체크리스트
- 실제 결과가 필요하면 `showAwait*`/`hideAwait*`를 사용한다.
- API 호출은 메인 스레드에서 수행한다.
- `KEEP_CURRENT_WINDOW`의 no-op 특성을 팀 내 공유한다.
- timeout/폴링 값의 UX·성능 영향도를 호출 화면 기준으로 점검한다.
- 문서(PRD/SPEC/PLAN/README) 용어를 항상 동일하게 유지한다.
