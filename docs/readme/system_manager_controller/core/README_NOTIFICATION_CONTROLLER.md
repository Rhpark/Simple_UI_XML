# NotificationController vs Plain Android - Complete Comparison Guide
> **NotificationController vs 순수 Android - 완벽 비교 가이드**

## 📦 Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.controller.notification`

<br></br>

## 개요
알림 표시/진행률 업데이트/채널 관리를 단순화합니다.

<br></br>

## 🔎 At a Glance (한눈 비교)
| Item (항목) | Plain Android (기본 방식) | Simple UI (Simple UI) | Notes (비고) |
|---|---|---|---|
| Channel creation / registration<br>채널 생성/등록 | Manual creation + SDK branching<br>직접 생성 + SDK 분기 | Auto registration via `NotificationChannel` injection<br>`NotificationChannel` 전달로 자동 등록 | Channel switch applies to future notifications only<br>채널 전환은 이후 알림에만 적용 |
| Click PendingIntent<br>클릭 PendingIntent | Manual creation + flag management<br>직접 생성 및 플래그 관리 | `clickIntent` + `pendingIntentFlags`<br>`clickIntent` + `pendingIntentFlags` | Android 12+ flags required<br>Android 12+ 플래그 필수 |
| Progress management<br>진행률 관리 | Manual builder reference retention<br>Builder 참조 수동 보관 | Internal cache + auto cleanup after 30 min idle<br>내부 캐시 + 30분 유휴 자동 정리 | Lower leak risk<br>누수 위험 감소 |
| Permission / exception handling<br>권한/예외 처리 | Handled by caller<br>호출부에서 직접 처리 | `tryCatchSystemManager()` returns `false` on failure<br>`tryCatchSystemManager()`로 실패 시 `false` | Android 13+ permission required<br>Android 13+ 권한 필요 |
| Update result<br>업데이트 결과 | Always notify<br>항상 notify | `false` when same value or target missing<br>동일 값/대상 없음 시 `false` | Avoid redundant updates<br>불필요한 업데이트 방지 |
| Cleanup responsibility<br>정리 책임 | Manual handling<br>별도 처리 | `cleanup()` recommended<br>`cleanup()` 권장 | Release resources on end<br>종료 시 리소스 정리 |

<br></br>

## 💡 Why It Matters (왜 중요한가)
**문제점:**
- Channel 생성/빌더 설정/Intent 구성 수동
- SDK 분기 필요 (Android 8.0+)
- 진행률 알림 Builder 참조 직접 관리

**장점:**
- 채널 전달만으로 알림 구성 간소화
- PendingIntent 자동 생성
- 진행률 알림 자동 정리(유휴 30분)
<br></br>

## 순수 Android 방식 (Plain Android)
```kotlin
// Traditional Notification display method (기존의 Notification 표시 방법)
@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
private fun showNotification() {
    // 1. Acquire NotificationManager (NotificationManager 획득)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 2. Create Notification Channel (Android 8.0+)
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

    // 3. Create PendingIntent
    val intent = Intent(this, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 4. Create Notification Builder
    val builder = NotificationCompat.Builder(this, "channel_id").apply {
        setContentTitle("Title")
        setContentText("Content")
        setSmallIcon(R.drawable.ic_notification)
        setAutoCancel(true)
        setContentIntent(pendingIntent)
    }

    // 5. Display Notification
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

## Simple UI 방식
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

// Simple Notification display
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

// Progress notification - Simple creation and update
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

// Progress update
private fun updateProgress(progress: Int) {
    notificationController.updateProgress(2, progress)
}

// Progress completion
private fun completeProgress() {
    notificationController.completeProgress(2, "Download Complete")
}

// BigText style notification
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

## 주의사항
- Android 13+는 `POST_NOTIFICATIONS` 권한이 필요합니다.
- `pendingIntentFlags`는 본문 클릭(`clickIntent`)에만 적용됩니다.
- `actions`는 호출자가 `NotificationCompat.Action`을 직접 구성해야 합니다.
- `updateProgress()`는 동일 값 또는 대상 없음이면 `false`를 반환합니다.
- 진행률 알림 사용 후 Activity/Service 종료 시 `cleanup()` 호출을 권장합니다.
- `createChannel()`은 이후 생성되는 알림에만 적용됩니다.

<br></br>

## 관련 확장 함수
- `getNotificationController(channel)`  
  자세한 목록: [../xml/README_SYSTEM_MANAGER_EXTENSIONS.md](../xml/README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>
