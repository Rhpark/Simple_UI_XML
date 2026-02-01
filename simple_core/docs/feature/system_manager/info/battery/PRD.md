# Battery Info PRD

## 문서 정보
- 문서명: Battery Info PRD
- 작성일: 2026-02-01
- 수정일: 2026-02-01
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.info.battery
- 상태: 현행(as-is)

## 배경/문제 정의
- Android 배터리 정보 수집은 BatteryManager + BroadcastReceiver + Intent extra 조합이 필요해 코드가 길고 반복됩니다.
- 일부 값(전류/평균 전류 등)은 브로드캐스트만으로 갱신되지 않아 주기 폴링이 필요합니다.
- 온도/전압 단위 변환, 유효 범위 검증, 예외 처리 등이 호출부마다 흩어지기 쉽습니다.
- 배터리 총 용량은 기기/OS마다 접근 방식이 달라 fallback 전략이 필요합니다.

## 목표
- 배터리 상태를 단일 API로 제공하고, 수집/변환/발행을 내부에서 일관 처리한다.
- 실시간 이벤트(브로드캐스트) + 주기 갱신(폴링)을 통합하여 안정적인 관찰을 제공한다.
- 환경/시스템 예외는 기본값 반환 + Logx 로그로 처리하여 크래시를 방지한다.
- 개발자 실수(잘못된 updateCycleTime)는 예외로 빠르게 드러나게 한다.

## 비목표
- 배터리 사용 패턴 분석/예측 모델 제공
- 시스템/프리로드 전용 권한(BATTERY_STATS) 확보/요청 UI 제공
- UI/시각화 컴포넌트 제공
- 백그라운드 스케줄러/워크매니저 연동 제공

## 범위

### 포함 범위
- `BatteryStateInfo`의 등록/해제/정리 API 및 각종 조회 메서드
- `sfUpdate` SharedFlow 기반 이벤트 스트림
- `BatteryStateEvent`, `BatteryStateConstants` 정의
- `BatteryStateReceiver`의 브로드캐스트 수신 + 주기 갱신 로직
- `BatteryPropertyReader`의 값 변환/검증 및 총 용량 추정
- PowerProfile 반사(reflection) 기반 총 용량 조회

### 제외 범위
- 앱/화면 레벨 UI 코드
- 배터리 최적화 정책(백그라운드 제한 대응 등)
- 프리로드 앱 전용 권한 제어
- 데이터 저장/분석 파이프라인

## 핵심 기능

### 수집/이벤트
- `registerStart(coroutine, updateCycleTime)`로 수집 시작
- `sfUpdate`로 11종 이벤트 발행
- `onDestroy()` 또는 `unRegister()`로 정리

### 조회 API
- 용량/전류/상태/온도/전압/건강 상태 등 다양한 getter 제공
- 편의 메서드: `isCharging`, `isDischarging`, `isFull` 등
- 충전 타입 문자열 리스트 제공 (`getChargePlugList`)

### 총 용량 추정
- PowerProfile → chargeCounter 기반 추정 → 실패 시 오류 값 반환

## 예외 처리 정책
- **개발자 실수**: `updateCycleTime < MIN_UPDATE_CYCLE_TIME`이면 `IllegalArgumentException` 발생
- **환경/시스템 이슈**: `tryCatchSystemManager`/`safeCatch`로 예외를 처리하고 기본값 반환
- **유효 범위 검증 실패**: 온도·전압은 로그 없이 오류 값 반환, 용량 추정 실패는 `Logx.w`로 기록 후 오류 값 반환
- **BATTERY_STATS**: 시스템/프리로드 전용 권한이며 검증하지 않음(매니페스트 경고 미출력)

## 비기능 요구사항
- **안정성**: 예외 발생 시 기본값 반환 + Logx 로그, 유효 범위 검증 실패는 오류 값 반환 가능
- **성능**: 주기 폴링 주기 조절 가능, DISABLE로 단일 갱신 가능
- **호환성**: minSdk 28, TIRAMISU 이상에서 `RECEIVER_NOT_EXPORTED` 사용
- **유지보수성**: 수집/변환/발행 책임 분리

## 제약/전제
- 일부 값은 기기/OS에 따라 미지원이며 오류 값으로 반환될 수 있음
- `sfUpdate`는 스냅샷이 아니라 이벤트이며 순서 보장이 없음
- SharedFlow 버퍼 정책에 따라 이벤트가 드롭될 수 있음
- PowerProfile reflection은 기기에 따라 실패할 수 있음
- `CoroutineScope`에 Job이 없으면 자동 정리가 보장되지 않음 (수동 `unRegister()` 필요)

## 성공 기준
- 단일 API로 배터리 상태를 안정적으로 수집/관찰할 수 있다.
- 잘못된 파라미터는 예외로 즉시 확인 가능하다.
- 오류 상황에서 앱 크래시 없이 기본값을 반환한다.
- 테스트에서 주요 값 변환/범위/등록 흐름이 검증된다.
  - Unit: BatteryStateEmitterUnitTest(16개), BatteryPropertyReaderEventUnitTest
  - Robolectric: BatteryStateReceiverRobolectricTest(27개), BatteryStateInfoRobolectricTest, BatteryPropertyReaderRobolectricTest

## 관련 파일
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/battery/BatteryStateInfo.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/battery/BatteryStateConstants.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/battery/BatteryStateEvent.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/battery/internal/helper/BatteryStateReceiver.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/battery/internal/helper/BatteryStateEmitter.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/battery/internal/helper/BatteryPropertyReader.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/battery/internal/helper/power/PowerProfile.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/battery/internal/helper/power/PowerProfileVO.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/battery/internal/model/BatteryStateData.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/extensions/SystemServiceExtensions.kt`

## 테스트
- `simple_core/src/test/java/kr/open/library/simple_ui/core/unit/system_manager/info/battery/BatteryPropertyReaderEventUnitTest.kt`
- `simple_core/src/test/java/kr/open/library/simple_ui/core/unit/system_manager/info/battery/BatteryStateEmitterUnitTest.kt`
- `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/info/battery/BatteryStateInfoRobolectricTest.kt`
- `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/info/battery/BatteryStateReceiverRobolectricTest.kt`
- `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/info/battery/BatteryPropertyReaderRobolectricTest.kt`
