# Alarm Controller PRD

## 문서 정보
- 문서명: Alarm Controller PRD
- 작성일: 2026-01-30
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.controller.alarm
- 상태: 현행(as-is)

## 배경/문제 정의
- Android 알람은 정확/부정확, 유휴 모드, 권한 정책(API 31+) 등 고려 요소가 많아 코드가 분산되기 쉽습니다.
- 부팅/시간/타임존 변경 시 재등록이 누락되면 알람 누락·시간 오차가 발생합니다.
- 알람 등록/갱신/제거, PendingIntent 충돌 방지, 알림 표시 흐름이 앱마다 중복 구현되고 유지보수가 어렵습니다.

## 목표
- 유지보수 관점에서 일관된 알람 등록/갱신/제거/조회 API를 제공한다.
- 정확 알람 권한 정책(API 31+)을 안전하게 처리하고, 권한 거부 시 크래시를 방지한다.
- 부팅/시간/타임존/정확 알람 권한 변경 시 재등록 흐름을 제공한다.
- 스케줄 정보와 알림 표시 정보를 분리해 책임을 명확히 한다.
- WakeLock을 안전하게 관리하고 예외 발생 시에도 알람 처리를 지속한다.

## 비목표
- 알람 저장소(DB/Preference) 구현 제공.
- 알림 UI/디자인(레이아웃, 스타일) 제공.
- 정확 알람 권한 예외 승인/정책 대응(Play Console 정책 등) 자동화.
- 복잡한 반복 규칙(예: CRON/요일 반복) 제공.

## 범위
### 포함 범위
- `AlarmController` 알람 등록/갱신/제거/존재 확인 API
- `AlarmVO` 및 하위 DTO(스케줄/알림 분리)
- `BaseAlarmReceiver` 리시버 처리, WakeLock 관리, 재등록 처리
- `AlarmConstants` 공통 상수
- 정확 알람 권한 확인 및 권한 요청 인텐트 제공

### 제외 범위
- 알람 저장/복원 로직(앱 레벨 구현)
- UI 계층(알림 레이아웃·스타일 구성)
- 시스템 정책 예외 처리(정확 알람 사용 사유 승인 절차)

## 핵심 기능
1. **단발 알람 등록**
   - 알람 시계(AlarmClock) / 정확 알람(Exact+Idle) / 유휴 허용 부정확 알람(Inexact, idle-allowed)
2. **반복 알람 등록**
   - 최소 반복 간격 60초 보정 및 inexact 동작(API 19+)
3. **스케줄 기반 자동 선택 등록**
   - `AlarmScheduleVO.idleMode`에 따라 등록 API 자동 선택
4. **날짜 지정 알람**
   - `AlarmDateVO`로 특정 날짜 지정 및 유효성 검증
5. **알람 업데이트**
   - remove + register 방식의 update API 제공
6. **알람 제거/존재 확인**
   - 동일 requestCode 기준으로 삭제/존재 확인
7. **권한 처리**
   - `canScheduleExactAlarms()` 체크 및 권한 요청 Intent 제공
8. **재등록 처리**
   - 부팅/시간/타임존 변경 시 저장된 알람 재등록
9. **정확 알람 권한 상태 변경 대응**
   - 권한 허용/거부 변화에 따른 재등록/안내 훅 제공
10. **알림 표시 흐름**
   - `SimpleNotificationController` 기반 알림 표시, 옵션 구성은 하위 클래스에서 확장
11. **PendingIntent 충돌 방지**
   - namespace + receiver 기반 requestCode 계산 전략

## 비기능 요구사항
- **안정성**: 예외 발생 시 크래시 대신 안전한 실패(false 반환/로그)로 처리
- **성능**: 브로드캐스트 처리에서 최소 작업 수행, 필요 시 WakeLock만 짧게 유지
- **유지보수성**: 스케줄/알림 DTO 분리 및 명확한 API 책임
- **호환성**: minSdk 28 기준, API 31+ 정확 알람 권한 대응

## 제약/전제
- 알람 저장/복원은 앱 레벨에서 구현한다(`loadAllAlarmVoList`, `loadAlarmVoList`). → 비목표 "알람 저장소(DB/Preference) 구현 제공" 참조.
- 정확 알람(SCHEDULE_EXACT_ALARM)은 API 31+에서 권한 허용이 필요하다.
- WakeLock 권한이 없는 경우 SecurityException을 허용하고, WakeLock 없이 처리한다.
- 리시버는 AndroidManifest에 필요한 intent-filter 및 권한을 등록해야 한다.

## 성공 기준
- 동일 조건에서 알람 등록/재등록/제거 동작이 일관되며 크래시가 발생하지 않는다.
- 정확 알람 권한 미허용 상태에서 안전하게 실패 처리된다.
- 부팅/시간/타임존 변경 후 저장된 알람이 재등록된다.
- PendingIntent 충돌 없이 여러 알람을 안정적으로 등록할 수 있다.

## 관련 파일
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/alarm/AlarmController.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/alarm/AlarmConstants.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/alarm/receiver/BaseAlarmReceiver.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/alarm/vo/AlarmVO.kt` (AlarmVO, AlarmScheduleVO, AlarmDateVO, AlarmNotificationVO, AlarmIdleMode, RegisterType 포함)

## 테스트
- `simple_core/src/test/java/kr/open/library/simple_ui/core/unit/system_manager/controller/alarm/vo/AlarmVoUnitTest.kt`
- `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/controller/alarm/AlarmControllerRobolectricTest.kt`
