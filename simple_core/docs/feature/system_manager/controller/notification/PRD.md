# 알림 컨트롤러(PRD)

## 문서 목적
`kr.open.library.simple_ui.core.system_manager.controller.notification` 패키지의 **요구사항과 제품 목표**를 정의합니다.  
현행 코드 동작을 기준으로 작성하며, 기능 범위와 제약을 명확히 합니다.

## 배경 / 문제 정의
- Android 알림은 **채널 생성**, **빌더 구성**, **PendingIntent 생성**, **버전별 권한 처리** 등 보일러플레이트가 많습니다.
- 진행률 알림은 업데이트/정리 로직이 별도 필요하여 누수 위험이 있습니다.
- 프로젝트 내에서 **일관된 API**로 알림을 관리할 필요가 있습니다.

## 목표
- 알림 생성 흐름을 단순화하고 일관된 API 제공
- 기본/확장(빅텍스트/빅픽처)/진행률 알림을 간편하게 구성
- Android 13+ 권한 처리와 안전한 예외 처리 패턴 제공
- 진행률 알림의 자동 정리로 리소스 누수 최소화

## 대상 사용자
- 앱 내 알림을 쉽게 표출하고 싶은 개발자
- 다양한 알림 스타일을 빠르게 적용해야 하는 개발자

## 범위
### 포함 범위(In-Scope)
- `SimpleNotificationController`를 통한 알림 생성/갱신/완료/취소
- `SimpleNotificationOptionBase` 및 파생 옵션(기본/빅텍스트/빅픽처/진행률)
- `SimpleNotificationType` 기반 클릭 동작 타입(Activity/Service/Broadcast)
- `NotificationChannel` 생성/등록 및 채널 전환
- 진행률 알림 자동 정리(유휴 30분 기준)

### 제외 범위(Out-of-Scope)
- 커스텀 RemoteViews/Heads-up 스타일 커스터마이징
- 알림 그룹/요약(그룹 키) 고급 정책 관리
- 시스템 수준 알림 정책 권한(Do-Not-Disturb 정책 변경 등)
- 알림 채널의 중요도 변경 정책(플랫폼 제약 사항)

## 기능 요구사항
1. **알림 생성**
   - 기본/빅텍스트/빅픽처/진행률 스타일 지원
   - 클릭 동작 타입(Activity/Service/Broadcast) 지원
2. **채널 관리**
   - 생성자에서 전달받은 `NotificationChannel` 등록
   - `createChannel()`로 채널 교체 가능
3. **진행률 관리**
   - `showNotification()`으로 진행률 알림 생성
   - `updateProgress()`로 진행률 업데이트
   - `completeProgress()`로 완료 처리
   - 유휴 30분 경과 시 자동 정리
4. **직접 알림 표출**
   - `notify(notificationId, Notification)`로 완전 커스텀 알림 표출 가능
5. **안전한 실행**
   - `showNotification`/`updateProgress`/`completeProgress`/`notify`는 `tryCatchSystemManager()` 래핑으로 권한 미허용·예외 발생 시 안전한 기본값(`false`) 반환 및 자동 로깅
   - `createChannel`/`cancelAll`/`cleanup`은 예외 래핑 없이 직접 호출되므로 **호출부에서 예외 처리 책임**을 가짐

## 비기능 요구사항
- **성능**: 진행률 알림 정리 스케줄러는 최소한의 주기(5분)로 동작
- **안정성**: 예외 발생 시 실패 처리 후 앱 크래시 방지
- **스레드 안전성**: 진행률 상태 관리와 스케줄러 생명주기는 동시 접근에 안전해야 하며, 진행률 업데이트는 원자적으로 처리되어야 함 (구현 상세는 SPEC 참조)
- **유지보수성**: 옵션 기반 구조로 확장 가능한 설계
- **호환성**: Android 13+ 권한(POST_NOTIFICATIONS) 런타임 고려
- **전제**: 본 모듈 minSdk는 28이며 알림 채널(API 26+) 기반

## 사용자 시나리오
1. 기본 알림을 한 줄로 표시하고 싶다.
2. 다운로드 진행률을 표시하고 주기적으로 갱신한다.
3. 빅텍스트/빅픽처 스타일을 적용한 알림을 생성한다.
4. 특정 채널로 알림을 분리하여 표시하고 싶다.

## 제약 및 리스크
- **알림 채널 불변성**: 채널 ID가 동일한 경우 중요도 등은 변경 불가(플랫폼 제약).
- **Android 13+ 권한**: 권한 미부여 시 알림 표시 실패.
- **PendingIntent 플래그**: Android 12+에서 `FLAG_IMMUTABLE` 또는 `FLAG_MUTABLE` 필수.
- **스케줄러 자원**: 정리 스케줄러는 **최초 진행률 알림 생성 시에만** 활성화되며, 진행률 알림이 없으면 스케줄러도 생성되지 않음. 활성화 후에는 5분 주기로 유휴 30분 초과 항목을 자동 제거하고, 맵이 비면 스케줄러가 자동 종료됨.
- **리소스 정리 책임**: Activity/Service 종료 시 반드시 `cleanup()` 호출 필요. 미호출 시 `ScheduledExecutorService` 스레드가 해제되지 않아 **리소스 누수 발생**. 진행률 알림을 사용하지 않았더라도 `cleanup()` 호출은 안전함(no-op).
- **채널 전환 범위**: `createChannel()`은 이후 생성되는 알림에만 적용됨.
- **액션 구성**: `actions`는 호출자가 `NotificationCompat.Action`을 직접 구성해야 함.

## 성공 기준(품질 지표)
- 기본/진행률 알림이 정상 표시됨
- 권한 미허용/예외 상황에서 앱 크래시 없음
- 진행률 알림 유휴 시 자동 정리 동작 확인

## 관련 파일
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/notification/SimpleNotificationController.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/notification/internal/SimpleNotificationBuilder.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/notification/option/NotificationOption.kt`
- `simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/notification/SimpleNotificationConstants.kt`

## 테스트 파일
- `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/controller/notification/SimpleNotificationControllerRobolectricTest.kt`
