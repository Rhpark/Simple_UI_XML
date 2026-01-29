# 알림 컨트롤러(SPEC)

## 문서 목적
`kr.open.library.simple_ui.core.system_manager.controller.notification` 패키지의 **API 동작과 세부 스펙**을 정의합니다.  
현행 코드 기준으로 작성합니다.

## 패키지 구성
- `SimpleNotificationController`
- `SimpleNotificationType`
- `NotificationDefaultChannel`
- `internal/SimpleNotificationBuilder`
- `option/NotificationOption.kt` (옵션 클래스 및 PendingIntent 옵션)

## 공통 전제
- Android 13+(API 33)에서 알림 표시 시 `POST_NOTIFICATIONS` 권한이 필요합니다.
- `showNotification()`, `updateProgress()`, `completeProgress()`, `notify()`는 `tryCatchSystemManager()`로 예외를 안전하게 처리합니다.
- `createChannel()`, `cancelAll()`, `cleanup()`은 직접 호출되며 예외 래핑이 없습니다.
- 컨트롤러는 `applicationContext`를 사용합니다.
- 본 모듈의 `minSdk`는 28이며, 알림 채널은 API 26+ 전제입니다.

## 공개 API 상세

### 1) SimpleNotificationController
**생성자**
```kotlin
SimpleNotificationController(context: Context, notificationChannel: NotificationChannel)
```
- `notificationChannel`은 필수이며, 생성자에서 즉시 `createNotificationChannel()`로 등록됩니다.
- Android 13+에서는 권한 리스트에 `POST_NOTIFICATIONS`가 자동 설정됩니다.

**프로퍼티**
- `notificationManager: NotificationManager`  
  - 지연 초기화(lazy). `context.applicationContext` 기준.

**메서드**
- `showNotification(option: SimpleNotificationOptionBase, showType: SimpleNotificationType): Boolean`
  - 옵션 타입에 따라 빌더 생성 후 `notify` 수행
  - 성공 시 `true`, 실패 시 `false`

- `createChannel(notificationChannel: NotificationChannel)`
  - 내부 채널을 교체하고 등록

- `notify(notificationId: Int, build: Notification): Boolean`
  - 완전 커스텀 알림을 직접 등록

- `updateProgress(notificationId: Int, progressPercent: Int): Boolean`
  - `0..100` 범위를 벗어나면 `false` 반환
  - 내부적으로 `ProgressUpdateResult`(`Updated`/`NoChange`/`NotFound`)로 분기하여 `Updated`일 때만 `true` 반환
  - 동일 값(`NoChange`) 또는 대상 없음(`NotFound`): `false`

- `completeProgress(notificationId: Int, completedContent: String? = null): Boolean`
  - 완료 처리 후 알림 재표시
  - 대상 없음: `false`

- `cancelNotification(tag: String? = null, notificationId: Int): Boolean`
  - `tag`가 `null`이면 ID만으로 취소, `null`이 아니면 tag+ID로 취소
  - 진행률 빌더도 함께 제거

- `cancelAll()`
  - 모든 알림 취소 및 진행률 빌더 제거

- `cleanup()`
  - 진행률 빌더 및 스케줄러 정리
  - Activity/Service의 `onDestroy()` 시점에 호출 권장
  - 미호출 시 `ScheduledExecutorService` 스레드가 해제되지 않아 리소스 누수 발생 가능

### 2) SimpleNotificationType
```kotlin
enum class SimpleNotificationType { ACTIVITY, SERVICE, BROADCAST }
```
- 클릭 동작 유형 결정
- `clickIntent`가 있을 때만 PendingIntent 생성

### 3) NotificationDefaultChannel
- 기본 채널 ID/이름/설명 상수 제공

## 옵션 클래스(SimpleNotificationOptionBase 계열)

### SimpleNotificationOptionBase
공통 필드:
- `notificationId: Int`
- `smallIcon: Int`
- `title: String`
- `content: String?`
- `isAutoCancel: Boolean`
- `onGoing: Boolean`
- `clickIntent: Intent?`
- `actions: List<NotificationCompat.Action>?`
- `pendingIntentFlags: Int`

**검증 규칙**
- `clickIntent != null` 이고 **Android 12+** 인 경우  
  `pendingIntentFlags`는 **FLAG_IMMUTABLE 또는 FLAG_MUTABLE 중 하나**를 반드시 포함해야 합니다.  
  둘 다 포함하면 예외(`IllegalArgumentException`)가 발생합니다.

### DefaultNotificationOption
기본 알림 옵션. `pendingIntentFlags` 기본값은  
`FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`.

### BigTextNotificationOption
추가 필드:
- `snippet: String`

### BigPictureNotificationOption
추가 필드:
- `bigPicture: Bitmap`

### ProgressNotificationOption
추가 필드:
- `progressPercent: Int`
  - `0..100` 범위를 벗어나면 예외 발생

### SimplePendingIntentOption
- `actionId: Int`
- `clickIntent: Intent`
- `flags: Int`

**액션(PendingIntent) 유의사항**
- `actions`는 `NotificationCompat.Action` 객체를 직접 전달해야 합니다.
- `pendingIntentFlags`는 본문 클릭(`clickIntent`)에만 적용됩니다.

## 내부 동작(SimpleNotificationBuilder)

### 빌더 생성
- `getBuilder()`  
  기본 빌더 구성(아이콘/제목/내용/자동해제/온고잉/액션)
- `getBigTextBuilder()`  
  BigTextStyle 적용
- `getBigPictureBuilder()`  
  BigPictureStyle 적용
- `getProgressBuilder()`  
  진행률 설정 및 `setOnlyAlertOnce(true)`

### 진행률 알림 상태 관리
- `progressBuilders: ConcurrentHashMap<Int, ProgressNotificationInfo>`
  - `ProgressNotificationInfo`는 `builder`, `lastUpdateTime`, `progressPercent` 포함

### 진행률 정리 스케줄러
- 최초 진행률 알림 생성 시 스케줄러 시작
- **5분 주기**로 실행
- **30분 이상 갱신되지 않은 알림**을 제거
- 제거 후 비어있으면 스케줄러 종료

### updateProgress 결과
`ProgressUpdateResult`:
- `Updated`: 갱신 성공
- `NoChange`: 동일 값으로 갱신 생략
- `NotFound`: 대상 없음

### completeProgress 동작
완료 처리 시:
1. 진행률 제거
2. `setOnlyAlertOnce(false)`로 완료 알림 재알림 허용
3. `setOngoing(false)`, `setAutoCancel(true)` 적용
4. 맵에서 제거 후 스케줄러 종료 여부 확인

## 오류 처리/로깅 정책
- `showNotification()`/`updateProgress()`/`completeProgress()`/`notify()`는 권한 미허용 또는 예외 발생 시 `false` 반환
- 내부 예외는 Logx에 기록
- 진행률 대상 없음/변경 없음도 Logx에 기록

## 스레드/동시성
- 진행률 맵은 `ConcurrentHashMap` 사용
- 진행률 업데이트는 `ConcurrentHashMap.compute()`로 원자적 처리
- 스케줄러 생성/종료는 `@Volatile` + `synchronized` double-checked locking 적용
- 빌더 객체(`NotificationCompat.Builder`)는 단일 인스턴스를 재사용하므로, 동시에 `setProgress()`와 `build()`가 호출되면 경합 가능 → 주로 메인 스레드 호출 전제

## 제한 사항
- 알림 채널의 중요도 변경은 플랫폼 제약으로 동작하지 않을 수 있음
- `clickIntent` 없이 `showNotification()` 호출 시 클릭 PendingIntent는 생성되지 않음
- `createChannel()`로 채널을 변경해도 **이후 생성되는 알림에만 적용**됨

## 테스트 파일
- `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/system_manager/controller/notification/SimpleNotificationControllerRobolectricTest.kt`
