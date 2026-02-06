# NotificationController vs Plain Android - Complete Comparison Guide
> **NotificationController vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.controller.notification`

<br></br>

## Overview (개요)
Simplifies notification display, progress updates, and channel management.  
> 알림 표시/진행률 업데이트/채널 관리를 단순화합니다.

<br></br>

## At a Glance (한눈 비교)
| Item (항목)                         | Plain Android (기본 방식)              | Simple UI (Simple UI)                                   | Notes (비고) |
|-----------------------------------|------------------------------------|---------------------------------------------------------|---|
| Channel creation / registration   | Manual creation + SDK branching    | Auto registration via `NotificationChannel` injection   | Channel switch applies to future notifications only<br>채널 전환은 이후 알림에만 적용 |
| Click PendingIntent               | Manual creation + flag management  | `clickIntent` + `pendingIntentFlags`                    | Android 12+ flags required<br>Android 12+ 플래그 필수 |
| Progress management               | Manual builder reference retention | Internal cache + auto cleanup after 30 min idle         | Lower leak risk<br>리소스 누수 위험 감소 |
| Permission / exception handling   | Handled by caller                  | `tryCatchSystemManager()` returns `false` on failure    | Android 13+ permission required<br>Android 13+ 권한 필요 |
| Update result                     | Always notify                      | `false` when same value or target missing               | Avoid redundant updates<br>불필요한 업데이트 방지 |
| Cleanup responsibility            | Manual handling                    | `cleanup()` recommended                                 | Release resources on end<br>종료 시 리소스 정리 |

<br></br>

## Why It Matters (중요한 이유)
**Issues / 문제점**
- Manual channel creation, builder setup, and intent wiring
- SDK branching required (Android 8.0+)
- Manual progress notification builder retention
> 채널 생성/빌더 설정/Intent 구성을 수동으로 처리
> <br>SDK 분기 필요 (Android 8.0+)
> <br>진행률 알림 Builder 참조를 직접 유지

**Advantages / 장점:**
- Simplified notification setup by passing a channel
- PendingIntent auto creation
- Progress notification auto cleanup (idle 30 min)
> 채널만 전달하면 알림 구성이 단순화됨
> <br>PendingIntent 자동 생성
> <br>진행률 알림 자동 정리(유휴 30분)

<br></br>

## 순수 Android 방식 (Plain Android)
```kotlin
// Traditional Notification display method (기존 Notification 표시 방식)
@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
private fun showNotification() {
    // 1. Acquire NotificationManager (NotificationManager 획득)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 2. Create Notification Channel (Android 8.0+) (알림 채널 생성)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "channel_id",
            "Channel Name",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel Description"
        }
        notificationManager.createNotificationChannel(channel)
    }

    // 3. Create PendingIntent (PendingIntent 생성)
    val intent = Intent(this, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 4. Create Notification Builder (Notification Builder 생성)
    val builder = NotificationCompat.Builder(this, "channel_id").apply {
        setContentTitle("Title")
        setContentText("Content")
        setSmallIcon(R.drawable.ic_notification)
        setAutoCancel(true)
        setContentIntent(pendingIntent)
    }

    // 5. Display Notification (알림 표시)
    notificationManager.notify(1, builder.build())
}

// Progress notification - Complex implementation (진행률 알림 - 복잡한 구현)
private var progressBuilder: NotificationCompat.Builder? = null

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
private fun showProgressNotification(progress: Int) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (progressBuilder == null) {
        progressBuilder = NotificationCompat.Builder(this, "channel_id").apply {
            setContentTitle("Downloading")
            setContentText("File Download")
            setSmallIcon(R.drawable.ic_download)
            setOngoing(true)
            setPriority(NotificationCompat.PRIORITY_LOW)
        }
    }

    progressBuilder?.setProgress(100, progress, false)
    notificationManager.notify(2, progressBuilder!!.build())
}
```

<br></br>

## Simple UI Approach (Simple UI 방식)
```kotlin
private val notificationChannel by lazy {
    NotificationChannel(
        "default_channel",
        "Default Notifications",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Default notification channel"
    }
}

private val notificationController by lazy {
    getNotificationController(notificationChannel)
}

// Simple Notification display (간단한 알림 표시)
private fun showNotification() {
    val option = DefaultNotificationOption(
        notificationId = 1,
        title = "Title",
        content = "Content",
        smallIcon = R.drawable.ic_notification,
        isAutoCancel = true,
        clickIntent = Intent(this, MainActivity::class.java)
    )
    notificationController.showNotification(option, SimpleNotificationType.ACTIVITY)
}

// Progress notification - Simple creation and update (진행률 알림 - 간단 생성 및 업데이트)
private fun showProgressNotification() {
    val option = ProgressNotificationOption(
        notificationId = 2,
        title = "Downloading",
        content = "File Download",
        smallIcon = R.drawable.ic_download,
        progressPercent = 0,
        onGoing = true
    )
    notificationController.showNotification(option, SimpleNotificationType.ACTIVITY)
}

// Progress update (진행률 업데이트)
private fun updateProgress(progress: Int) {
    notificationController.updateProgress(2, progress)
}

// Progress completion (진행률 완료)
private fun completeProgress() {
    notificationController.completeProgress(2, "Download Complete")
}

// BigText style notification (BigText 스타일 알림)
private fun showBigTextNotification() {
    val option = BigTextNotificationOption(
        notificationId = 3,
        title = "Long Text Notification",
        content = "Summary",
        snippet = "Very long text will be displayed here. " +
                 "You can see the full content when expanded.",
        smallIcon = R.drawable.ic_notification
    )
    notificationController.showNotification(option, SimpleNotificationType.ACTIVITY)
}
```

<br></br>

## Notes (주의사항)
- Android 13+ requires the `POST_NOTIFICATIONS` permission.
- `pendingIntentFlags` applies only to the primary click (`clickIntent`).
- Callers must build `NotificationCompat.Action` themselves for `actions`.
- `updateProgress()` returns `false` when value is unchanged or target is missing.
- After using progress notifications, call `cleanup()` when the Activity/Service ends.
- `createChannel()` applies only to notifications created afterward.
> Android 13+에서는 `POST_NOTIFICATIONS` 권한이 필요합니다.
> `pendingIntentFlags`는 기본 클릭(`clickIntent`)에만 적용됩니다.
> `actions`는 호출자가 `NotificationCompat.Action`을 직접 구성해야 합니다.
> `updateProgress()`는 값이 동일하거나 대상이 없으면 `false`를 반환합니다.
> 진행률 알림 사용 후 Activity/Service 종료 시 `cleanup()` 호출을 권장합니다.
> `createChannel()`은 이후 생성되는 알림에만 적용됩니다.

<br></br>

## Related Extensions (관련 확장 함수)
- `getNotificationController(channel)`  
  See full list / 전체 목록: [README_SYSTEM_MANAGER_EXTENSIONS.md](../../README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>


