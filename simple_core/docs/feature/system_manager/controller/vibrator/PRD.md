# Vibrator Controller PRD

## 문서 정보
- 문서명: Vibrator Controller PRD
- 작성일: 2026-01-30
- 수정일: 2026-01-31
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.controller.vibrator
- 상태: 현행(as-is)

## 배경/문제 정의
- Android 진동 API는 SDK 31(S)에서 VibratorManager로 구조가 변경되어 분기 처리가 필수입니다.
- 진동 패턴/웨이브폼/프리셋 등 다양한 API가 존재해 호출부 보일러플레이트가 증가합니다.
- 입력 검증(시간/강도/배열) 누락 시 예외 또는 예측 불가 동작이 발생합니다.
- 권한 및 API 레벨 차이를 호출부가 직접 처리하면 유지보수가 어렵습니다.

## 목표
- SDK 버전 차이를 내부에서 처리하여 단일 진동 API를 제공한다.
- 입력 검증과 안전한 예외 처리로 크래시를 방지한다.
- 진동 패턴/웨이브폼/프리셋을 단일 컨트롤러로 제공한다.
- 진동 하드웨어/강도 지원 여부를 쉽게 확인할 수 있게 한다.

## 비목표
- 햅틱 UX 가이드(사용 타이밍/강도 추천) 제공.
- 커스텀 패턴 빌더/디자이너 제공.
- 시스템 앱 전용 권한/정책(예: 고급 진동 정책) 자동 처리.

## 범위

### 포함 범위
- `VibratorController`의 진동 실행/패턴/프리셋/취소/지원 여부 API
- SDK 31+ VibratorManager와 레거시 Vibrator 분기 처리
- 입력 검증(시간, 강도, 배열, 반복 인덱스)
- 예외 안전 처리 및 Logx 기반 로깅
- 매니페스트 미선언 권한 경고(BaseSystemService 공통 경고 로그)

### 제외 범위
- UI 레벨 예제/디자인 구성
- 커스텀 햅틱 효과 생성/저장
- 진동 설정 화면/권한 요청 UX 제공

## 핵심 기능

### 실행 API (매니페스트 선언 필요, `@RequiresPermission(VIBRATE)`)

1. **단순 진동**: `vibrate(milliseconds)` — 내부적으로 `createOneShot()`에 위임
2. **단발 진동(강도 포함)**: `createOneShot(timer, amplitude)`
3. **웨이브폼 진동**: `createWaveform(times, amplitudes, repeat)`
4. **패턴 진동(타이밍만)**: `vibratePattern(pattern, repeat)`
5. **프리셋 진동(Q+)**: `createPredefined(vibrationEffectClick)` — `@RequiresApi(Q)` + 내부 SDK Q 분기 이중 보호
6. **진동 취소**: `cancel()`

### 조회 API (권한 불필요)

1. **진동 지원 여부**: `hasVibrator()`
2. **강도 조절 지원 여부**: `hasAmplitudeControl()`

## 예외 처리 정책

- **실행 API**: `tryCatchSystemManager(false)`로 감싸 권한 검증 + 예외 처리 통합
- **조회 API**: `safeCatch(false)`로 감싸 권한 검증 없이 예외만 처리 (하드웨어 정보 조회이므로 권한 불필요)
  - VIBRATE는 일반 권한이므로 런타임 거부 시나리오는 거의 발생하지 않으며, 매니페스트 미선언 경고가 주요 안전장치입니다.

## 비기능 요구사항
- **안정성**: 예외 발생 시 false 반환 및 로그 기록으로 크래시 방지
- **성능**: 시스템 서비스 접근은 lazy 초기화로 최소화
- **호환성**: minSdk 28 기준, SDK 31+ 분기 지원
- **유지보수성**: 분기/검증 로직을 중앙화하여 호출부 단순화

## 제약/전제

- `android.permission.VIBRATE`는 **매니페스트 선언이 필요**하며, 누락 시 `BaseSystemService`가 경고 로그를 출력합니다.
- `android.permission.VIBRATE`는 일반 권한(normal permission)으로 **런타임 사용자 승인 절차가 없습니다**.
- `createPredefined()`는 Android Q(29)+에서만 지원됩니다. pre-Q에서는 false 반환 + 로그를 출력합니다.
- 강도 조절은 기기 하드웨어에 따라 무시될 수 있습니다. `hasAmplitudeControl()`로 사전 확인 가능합니다.
- `repeat`가 0 이상인 경우, 진동은 `cancel()` 호출 전까지 반복됩니다.

## 성공 기준
- SDK 버전별 분기 오류 없이 동일한 호출 패턴으로 진동 실행 가능
- 잘못된 입력은 false 반환 + Logx 경고로 종료
- 프리셋 진동이 pre-Q에서 크래시 없이 실패 처리
- 진동/강도 지원 여부를 API로 확인 가능

## 관련 파일
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/vibrator/VibratorController.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/base/BaseSystemService.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/extensions/SystemServiceExtensions.kt`
- `app/src/main/java/kr/open/library/simpleui_xml/system_service_manager/controller/vibrator/VibratorControllerActivity.kt` (샘플 앱)
- `app/src/main/res/layout/activity_vibrator_controller.xml` (샘플 레이아웃)

## 테스트
- `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/controller/vibrator/VibratorControllerRobolectricTest.kt`
  - Robolectric 환경에서 권한 보호 수준을 위험 권한으로 가정하여 권한 미부여 시나리오를 검증합니다.
