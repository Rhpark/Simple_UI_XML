# SoftKeyboard Controller PRD

## 문서 정보
- 문서명: SoftKeyboard Controller PRD
- 작성일: 2026-02-07
- 수정일: 2026-02-07
- 대상 모듈: simple_xml
- 패키지: kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard
- 상태: 현행(as-is)

## 배경/문제 정의
- Android IME 제어는 `InputMethodManager` 호출 성공과 실제 키보드 가시성 변화가 분리되어 있습니다.
- 호출 스레드(메인/오프메인), 포커스 상태, 윈도우 토큰 유무에 따라 실패 원인이 달라집니다.
- 호출부에서 `show/hide` 결과를 실제 표시/숨김 성공으로 오해하기 쉽습니다.
- 화면별로 resize 처리 방식이 달라 정책을 명시적으로 관리할 필요가 있습니다.

## 목표
- 키보드 제어를 단일 Controller API로 제공한다.
- 결과 의미를 3계층(요청 결과 / 큐 등록 결과 / 실제 가시성 결과)으로 명확히 분리한다.
- 메인 스레드 오용, 잘못된 인자, 예외를 일관된 결과 모델로 처리한다.
- resize 정책을 명시적으로 선택 가능하게 하여 화면별 부작용을 줄인다.
- 비동기 대기 경로에서 코루틴 취소가 정상 전파되도록 한다.

## 비목표
- 모든 단말/키보드 앱 조합에서 IME 표시·숨김 100% 보장
- 키보드 UI 테마/렌더링/입력기 동작 커스터마이징
- Compose 전용 API 제공
- Controller 전역 싱글턴/자동 캐시 관리

## 범위

### 포함 범위
- 공개 API
  - `show/hide`
  - `showDelay/hideDelay`
  - `showAwait/hideAwait`
  - `showAwaitAsync/hideAwaitAsync`
  - `configureImeResize`, `setAdjustResize`, `setAdjustPan`, `setSoftInputMode`
  - `startStylusHandwriting`(즉시/지연)
- 결과 모델
  - `SoftKeyboardActionResult`
  - `SoftKeyboardFailureReason`
  - `SoftKeyboardResizePolicy`
- 확장 함수
  - `Context.getSoftKeyboardController()`
- 샘플 및 Robolectric 테스트

### 제외 범위
- Activity/Fragment 라이프사이클 자동 연동
- 단말 제조사별 IME 튜닝 로직
- IME 애니메이션 프레임 동기화용 고급 API 래핑

## 핵심 계약

### 결과 계약
- `show/hide`
  - 요청 경로 호출 성공 여부만 반환한다.
  - 실제 가시성 변화는 보장하지 않는다.
- `showDelay/hideDelay`
  - 지연 Runnable 큐 등록 성공 여부만 반환한다.
  - 실제 실행/가시성 결과는 포함하지 않는다.
- `showAwait/hideAwait`
  - 실제 가시성 결과(`Success`, `Timeout`, `Failure`)를 반환한다.
- `showAwaitAsync`
  - 실제 가시성 결과를 담은 non-null `Deferred`를 반환한다.
  - 오프메인 호출 시에도 `Failure(OFF_MAIN_THREAD)`를 담아 반환한다.
- `hideAwaitAsync`
  - 실제 가시성 결과를 담은 nullable `Deferred`를 반환한다.
  - 오프메인 호출 또는 내부 예외 시 `null`이 반환될 수 있다.

### Timeout 의미
- `Timeout`은 제한 시간 내 미관측을 의미한다.
- 영구 실패를 의미하지 않는다.

## 예외 처리 정책
- 오프메인 호출
  - 경고 로그 후 `false`, `null`, 또는 `Failure(OFF_MAIN_THREAD)` 반환
  - `showAwaitAsync`는 `null` 대신 `Failure(OFF_MAIN_THREAD)`를 담은 Deferred 반환
- 인자 오류
  - `delayMillis < 0` 또는 `timeoutMillis <= 0`이면 `Failure(INVALID_ARGUMENT)` 반환
  - delay API의 음수 delay는 `false` 반환
- 포커스 실패
  - `Failure(FOCUS_REQUEST_FAILED)` 반환
- 숨김 컨텍스트 누락
  - `Failure(WINDOW_TOKEN_MISSING)` 반환
- IME 요청 거절
  - `Failure(IME_REQUEST_REJECTED)` 반환
- 기타 예외
  - `Failure(EXCEPTION_OCCURRED)` 또는 기본값 반환 + Logx 기록
- 코루틴 취소
  - `CancellationException`은 재던져 취소를 전파한다

## 비기능 요구사항
- 안정성
  - 예외 상황에서 크래시 대신 기본값/결과 모델 반환
- 성능
  - 콜백 우선 + 50ms 폴백 관찰
  - 바쁜 루프를 피하고 timeout 범위 내에서만 대기
- 호환성
  - minSdk 28, API 30+/29- resize 분기, API 33+ 스타일러스 분기
- 유지보수성
  - 실패 사유 enum 분리
  - resize 정책 enum 분리

## 제약/전제
- 모든 공개 API는 메인 스레드 호출이 전제다.
- `Context.getSoftKeyboardController()`는 호출마다 새 인스턴스를 생성한다.
- `KEEP_CURRENT_WINDOW`는 API 30+에서 no-op으로 동작한다.
- IME 가시성은 시스템/키보드 앱 상태에 영향받아 미관측 가능성이 있다.

## 성공 기준
- README/PRD/SPEC/PLAN의 결과 계약 용어가 일치한다.
- 오프메인/인자오류/취소전파/실패사유 매핑이 테스트로 검증된다.
- 호출부가 “요청 결과”와 “실제 결과”를 구분해 사용할 수 있다.

## 관련 파일
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/softkeyboard/SoftKeyboardController.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/softkeyboard/SoftKeyboardActionResult.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/softkeyboard/SoftKeyboardResizePolicy.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/extensions/SystemServiceExtensionsXml.kt`
- `docs/readme/system_manager/controller/xml/README_SOFTKEYBOARD_CONTROLLER.md`
- `app/src/main/java/kr/open/library/simpleui_xml/system_service_manager/controller/softkeyboard/SoftKeyboardControllerActivity.kt`

## 테스트
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/system_manager/controller/softkeyboard/SoftKeyboardControllerRobolectricTest.kt`
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/unit/system_manager/controller/softkeyboard/SoftKeyboardActionResultTest.kt`
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/unit/system_manager/controller/softkeyboard/SoftKeyboardResizePolicyTest.kt`
