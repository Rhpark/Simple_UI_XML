# Vibrator Controller Implementation Plan (As-Is)

## 문서 정보

- 문서명: Vibrator Controller Implementation Plan
- 작성일: 2026-01-30
- 수정일: 2026-01-31
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.controller.vibrator
- 상태: 현행(as-is)

## 목표

- 현재 구현된 진동 컨트롤러의 동작 규칙을 문서로 정리한다.
- 코드 없이도 구현 흐름을 재현할 수 있도록 단계별 로직을 명시한다.

## 구현 범위(현행)

- `VibratorController`의 진동 실행/패턴/프리셋/취소/지원 여부 API
- SDK 31+ VibratorManager 분기, 레거시 Vibrator 분기
- 입력 검증/예외 안전 처리/로그 정책
- 매니페스트 미선언 권한 경고(BaseSystemService 공통)
- Robolectric 테스트

## 구현 상세(파일 기준)

### 1) VibratorController 초기화

- `BaseSystemService(context, listOf(VIBRATE))`를 상속하여 VIBRATE 권한 자동 관리
- `vibrator`: `by lazy { context.getVibrator() }` — SDK 30 이하용 레거시 인스턴스
- `vibratorManager`: `by lazy { checkSdkVersion(S, positiveWork = { context.getVibratorManager() }, negativeWork = { throwMinSdkVersion(it) }) }` — SDK 31+ 전용. SDK < S에서 접근 시 `UnsupportedOperationException` 발생 (모든 메서드가 `checkSdkVersion(S)`로 분기하므로 실제 접근되지 않음)

### 2) 실행 API 공통 패턴

- `tryCatchSystemManager(false) { ... }` 로 감싸서:
  1. 권한 미부여 → false 반환 + Logx.w
  2. 입력 검증 실패 → false 반환 + Logx.e
  3. SDK 분기 → `checkSdkVersion(S)` 로 `vibratorManager` vs `vibrator` 선택
  4. 예외 발생 → false 반환 + Logx.e
  - VIBRATE는 일반 권한이라 런타임 거부 시나리오는 거의 발생하지 않으며, 매니페스트 미선언 경고가 주요 안전장치

### 3) vibrate() 위임 구조

- `vibrate(milliseconds)`는 `tryCatchSystemManager`를 직접 사용하지 않는다.
- 자체 입력 검증(`milliseconds > 0`) 후 `createOneShot(milliseconds)`에 위임한다.
- `createOneShot` 내부의 `tryCatchSystemManager`에 의해 간접 보호된다.

### 4) 입력 검증

- 단발 진동: 시간은 0보다 커야 하며, 강도(amplitude)는 -1(DEFAULT_AMPLITUDE) 또는 1..255
- 웨이브폼: 배열 비어있음 금지, 길이 동일, time >= 0, amplitude 유효 범위, repeat 인덱스 검증
- 패턴: 배열 비어있음 금지, 값 0 이상, repeat 인덱스 검증
- repeat가 0 이상이면 `cancel()` 호출 전까지 반복됨

### 5) 프리셋 진동

- `@RequiresApi(Q)` 어노테이션으로 컴파일 타임 보호
- 내부 `checkSdkVersion(Q)` 런타임 이중 보호:
  - SDK Q+: `VibrationEffect.createPredefined()` 생성 후 SDK S 분기를 거쳐 진동 실행
  - pre-Q: 한글 로그(`"createPredefined는 Android Q(29) 이상에서만 지원됩니다."`) 출력 후 false 반환

### 6) 조회 API

- `hasVibrator()`와 `hasAmplitudeControl()`은 `safeCatch(false)`로 감싼다 (`tryCatchSystemManager` 아님).
- 권한 없이도 호출 가능 (하드웨어 정보 조회)
- SDK S 분기하여 `vibratorManager.defaultVibrator` 또는 `vibrator`의 결과 반환

### 7) 공통 권한/매니페스트 경고

- `BaseSystemService` init에서 매니페스트 미선언 권한 경고 로그 출력
- `getDeclaredManifestPermissions()`로 매니페스트에 선언된 권한 목록을 조회 (SDK TIRAMISU 분기 포함)
- 필요 권한이 매니페스트에 선언되지 않았으면 Logx.w 경고 로그 출력
- 런타임 권한 부재 시에는 `tryCatchSystemManager`에서 false 반환
  - VIBRATE는 일반 권한이라 런타임 승인 절차가 없으며, 매니페스트 경고가 주요 안전장치

## 구현 흐름 요약

1. 컨트롤러 생성 → `BaseSystemService` init (매니페스트 검증 + 권한 상태 캐시) → 시스템 서비스 lazy 초기화
2. 실행 API 호출 → 권한 검증 → 입력 검증 → SDK 분기 → 진동 실행
3. 조회 API 호출 → SDK 분기 → 하드웨어 정보 반환 (권한 검증 없음)
4. 예외/권한 문제 발생 시 false 반환 + Logx 로그

## 테스트 현황

- `VibratorControllerRobolectricTest`
  - SDK P/Q/S 분기 검증
  - 입력 검증/예외 처리/권한 미부여 검증
  - `createPredefined()` pre-Q 안전 반환 검증
  - `hasAmplitudeControl()` SDK 분기 및 권한 무관 동작 검증
  - `hasVibrator()` SDK 분기 및 권한 무관 동작 검증
  - Robolectric 환경에서 권한 보호 수준을 위험 권한으로 가정하여 권한 미부여 시나리오를 검증

## 운영/유지보수 체크리스트

- AndroidManifest에 `android.permission.VIBRATE` 선언 여부 확인
- SDK 29+에서만 프리셋 진동 호출
- 강도 조절 지원 여부를 필요 시 `hasAmplitudeControl()`로 확인
- 로그에서 입력 오류/권한 경고 여부 확인

## 관련 파일

- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/vibrator/VibratorController.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/base/BaseSystemService.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/extensions/SystemServiceExtensions.kt`
- `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/controller/vibrator/VibratorControllerRobolectricTest.kt`
- `app/src/main/java/kr/open/library/simpleui_xml/system_service_manager/controller/vibrator/VibratorControllerActivity.kt` (샘플 앱)
- `app/src/main/res/layout/activity_vibrator_controller.xml` (샘플 레이아웃)
