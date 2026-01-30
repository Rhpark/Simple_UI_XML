# Alarm Controller Implementation Plan (As-Is)

## 문서 정보
- 문서명: Alarm Controller Implementation Plan
- 작성일: 2026-01-30
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.controller.alarm
- 상태: 현행(as-is)

## 목표
- 현재 구현된 알람 컨트롤러/리시버/VO 구조와 동작을 유지보수 관점에서 정리한다.
- 코드 없이 문서만으로 동일한 구현을 재현할 수 있도록 동작 규칙을 명시한다.

## 구현 범위(현행)
- AlarmManager 래핑 컨트롤러(`AlarmController`)
- 알람 도메인 모델(`AlarmVO`, `AlarmScheduleVO`, `AlarmDateVO`, `AlarmNotificationVO`, `AlarmIdleMode`)
- 브로드캐스트 리시버 템플릿(`BaseAlarmReceiver`)
- 공통 상수(`AlarmConstants`)
- 단위/로보렉트릭 테스트

## 구현 상세(파일 기준)
### 1) AlarmConstants
- 알람 키, WakeLock 태그/타임아웃, 기본 acquire 시간, 정확 알람 권한 변경 액션을 정의
- 상수는 `AlarmController`/`BaseAlarmReceiver`/`AlarmVO`에서 사용

### 2) AlarmVO 및 하위 DTO
- `AlarmDateVO`: Calendar lenient=false로 실제 날짜 유효성 검증
- `AlarmScheduleVO`: 시간 범위 검증, `date`가 있으면 특정 날짜 알람
- `AlarmIdleMode`: NONE/INEXACT/EXACT로 등록 API 결정
- `AlarmNotificationVO`: title/message 필수, 진동 패턴 유효성 검증
- `AlarmVO`: key/acquireTime 유효성, 편의 메서드 제공

### 3) AlarmController
- 정확 알람(AlarmClock/Exact+Idle) 등록 시 권한 체크 수행
- `registerAlarmAndAllowWhileIdle`는 부정확 알람으로 권한 체크 없음
- `registerRepeating`는 최소 60초로 보정 후 setRepeating 사용
- `registerBySchedule`는 idleMode 기반으로 등록 API 선택
- update 계열은 존재 확인 후 remove + register 순서로 처리
- requestCode는 `namespace(또는 receiver.name)` + `key` 조합으로 계산
- `buildExactAlarmPermissionIntent`로 권한 요청 Intent 제공

### 4) BaseAlarmReceiver
- onReceive에서 WakeLock을 안전하게 획득/해제
- 부팅/시간/타임존 변경 시 `loadAllAlarmVoList` 기반 재등록
- 정확 알람 권한 변경 시 재등록 또는 거부 훅 호출
- 알람 트리거 시 알림 채널 생성 → 옵션 생성 → 알림 표시
- `notificationController`는 `lateinit`으로 선언되며, `handleAlarmTrigger` 흐름에서 `ensureNotificationControllerInitialized()`를 통해 초기화됨

## 구현 흐름 요약
1. 알람 등록
   - AlarmVO 생성 → AlarmController.register* 호출
   - requestCode 계산 → PendingIntent 생성 → AlarmManager 등록
2. 알람 트리거
   - BaseAlarmReceiver 수신 → key 추출 → AlarmVO 로딩 → 알림 표시
3. 재등록
   - BOOT/TIME/TIMEZONE 변경 → 저장된 알람 재등록
4. 권한 변경
   - 정확 알람 권한 허용 시 재등록, 거부 시 안내 훅 호출

## 테스트 현황
- `AlarmVoUnitTest`: VO/상수/팩토리/유효성/AlarmDateVO/AlarmScheduleVO/AlarmIdleMode 검증 (~56개 테스트)
- `AlarmControllerRobolectricTest`: 등록/해제/업데이트/반복/스케줄기반/namespace/날짜지정 검증 (~19개 테스트)

## 운영/유지보수 체크리스트
- Manifest에 BOOT/TIME/TIMEZONE/정확 알람 권한 변경 액션 등록 여부
- 정확 알람 권한 허용 상태 확인 로직 유지
- 알람 저장소 구현 누락 여부(`loadAllAlarmVoList`, `loadAlarmVoList`)
- namespace 전략을 등록/삭제/조회 모두 동일하게 사용하는지 확인
- `registerRepeating`의 intervalMillis 최소값(60,000ms) 클램핑 동작 확인
- `AlarmDateVO` 날짜 유효성(Calendar isLenient=false) 동작 확인

## 관련 파일
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/alarm/AlarmController.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/alarm/AlarmConstants.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/alarm/receiver/BaseAlarmReceiver.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/alarm/vo/AlarmVO.kt`
- `simple_core/src/test/java/kr/open/library/simple_ui/core/unit/system_manager/controller/alarm/vo/AlarmVoUnitTest.kt`
- `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/controller/alarm/AlarmControllerRobolectricTest.kt`
