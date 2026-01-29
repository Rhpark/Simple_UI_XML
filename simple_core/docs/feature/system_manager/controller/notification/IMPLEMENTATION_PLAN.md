# 알림 컨트롤러(IMPLEMENTATION PLAN)

## 문서 목적
알림 컨트롤러의 **구현 단계와 검증 항목**을 정의합니다.  
현행 코드 구조를 기준으로 작성합니다.

## 범위
대상 패키지:  
`kr.open.library.simple_ui.core.system_manager.controller.notification`

포함 클래스:
- `SimpleNotificationController`
- `SimpleNotificationType`
- `NotificationDefaultChannel`
- `SimpleNotificationBuilder` (internal)
- `NotificationOption` 계열

## 구현 단계(요약)

### 1. 설계
- 알림 스타일별 옵션 구조 정의(기본/빅텍스트/빅픽처/진행률)
- 클릭 동작 타입(Activity/Service/Broadcast) 정의
- 채널 처리 정책(생성자 등록 + 전환 메서드) 확정
- 권한 처리(POST_NOTIFICATIONS) 및 실패 처리 정책 결정

### 2. 핵심 구현
- 컨트롤러 구현 (`SimpleNotificationController.kt`)
  - 채널 등록 및 교체 로직
  - 알림 표시/갱신/완료/취소 API 제공
  - `notify()` 직접 표출 API (완전 커스텀 알림용)
  - `showNotification`/`updateProgress`/`completeProgress`/`notify`는 `tryCatchSystemManager()` 래핑으로 예외 시 기본값 반환
  - `createChannel`/`cancelAll`/`cleanup`은 직접 호출(예외 래핑 없음)
- 빌더 구현 (`internal/SimpleNotificationBuilder.kt`)
  - 스타일별 NotificationCompat.Builder 생성
  - 진행률 상태 관리(`ConcurrentHashMap`)
  - 자동 정리 스케줄러(5분 주기, 유휴 30분 제거)
  - 스레드 안전성: `@Volatile` + `synchronized` double-checked locking(스케줄러), `ConcurrentHashMap.compute()`(진행률 업데이트)
- 옵션/검증 구현 (`option/NotificationOption.kt`)
  - 진행률 범위 검증(0~100)
  - Android 12+ PendingIntent 플래그 검증
  - `actions`는 호출자가 `NotificationCompat.Action`을 직접 구성하여 전달
- 상수/열거형 정의 (`SimpleNotificationConstants.kt`)
  - `SimpleNotificationType` (Activity/Service/Broadcast)
  - `NotificationDefaultChannel` (기본 채널 ID/이름/설명)

### 3. 테스트
- 단위 테스트
  - 옵션 값 저장/검증 로직
  - PendingIntent 플래그 검증 조건
- 로보렉트릭 테스트
  - 알림 표시/취소 동작
  - 진행률 업데이트/완료 시나리오
  - 정리 스케줄러 동작

### 4. 문서화
- PRD/SPEC/IMPLEMENTATION_PLAN 정합성 유지

### 5. 유지보수/확장
- 신규 옵션 추가 시 `SimpleNotificationOptionBase` 확장
- 스케줄러 정책 변경 시 `SimpleNotificationBuilder`에서 관리

## 체크리스트(검증 포인트)
- [ ] Android 13+ 권한 미허용 시 안전하게 실패 처리되는가?
- [ ] `showNotification()`이 옵션 타입별로 정상 표시되는가?
- [ ] `notify()`로 직접 표출한 알림이 정상 등록되는가?
- [ ] `updateProgress()` 동일 값 입력 시 알림이 갱신되지 않는가?
- [ ] `completeProgress()` 수행 후 진행률 알림이 맵에서 제거되는가?
- [ ] 30분 이상 유휴된 진행률 알림이 자동 정리되는가?
- [ ] `pendingIntentFlags` 검증 예외 메시지가 명확한가?
- [ ] 스케줄러가 동시 호출에서 중복 생성되지 않는가? (`@Volatile` + double-checked locking)
- [ ] 진행률 업데이트가 `compute()`로 원자적으로 처리되는가?
- [ ] `cleanup()` 호출 시 스케줄러가 정상 종료되는가?
- [ ] `cleanup()` 미호출 시 리소스 누수 시나리오를 문서화했는가?
- [ ] `createChannel()`로 채널 전환 시 기존 알림에는 영향 없고 이후 알림에만 적용되는가?
- [ ] `actions`의 PendingIntent는 호출자가 직접 구성하며, `pendingIntentFlags`는 본문 클릭(`clickIntent`)에만 적용되는가?

## 참고
관련 문서:
- `simple_core/docs/feature/system_manager/controller/notification/PRD.md`
- `simple_core/docs/feature/system_manager/controller/notification/SPEC.md`

테스트 파일:
- `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/controller/notification/SimpleNotificationControllerRobolectricTest.kt`
