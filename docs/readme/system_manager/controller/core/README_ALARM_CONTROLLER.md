# AlarmController vs Plain Android - Complete Comparison Guide
> **AlarmController vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_system_manager` (system manager 전용 모듈 / system_manager 전용 모듈)
- **Package**: `kr.open.library.simple_ui.system_manager.core.controller.alarm` (패키지)

<br>

## Overview (개요)
Provides simple APIs for alarm register/remove/existence checks.  
> 알람 등록/삭제/존재 확인을 간단한 API로 제공합니다.

- Exact alarm APIs (`registerAlarmClock`, `registerAlarmExactAndAllowWhileIdle`) require `SCHEDULE_EXACT_ALARM` on API 31+. Inexact APIs work without this permission.
> Exact 알람 API는 API 31+에서 `SCHEDULE_EXACT_ALARM` 권한이 필요합니다. Inexact API(`registerAlarmAndAllowWhileIdle`, `registerRepeating`)는 권한 없이도 동작합니다.

<br>

## At a Glance (한눈 비교)
| Item (항목) | Plain Android (기본 방식) | Simple UI (Simple UI) | Notes (비고) |
|---|---|---|---|
| Time calculation | Manual Calendar calculation | VO-based auto calculation | Includes today/tomorrow branching<br>오늘/내일 분기 포함 |
| PendingIntent | Manual creation + flag management | Created internally | Key-based management<br>키 기반 관리 |
| AlarmClockInfo | Manual creation | Handled internally | Less boilerplate<br>코드 간소화 |
| Remove / Exists check | Manual query/cancel | `remove()` / `exists()` | One-line call<br>한 줄 호출 |
| Alarm trigger safety | Caller-side permission handling | Internal guard in receiver | Android 13+ `POST_NOTIFICATIONS` pre-check<br>권한 누락 시 안전 스킵 |
| Permission / SDK branching | Handled by caller | Handled internally | Exact alarm permission required<br>Exact 알람 권한 주의 |

<br>

## Why It Matters (중요한 이유)
**Issues / 문제점**
- Manual Calendar calculation
- Manual PendingIntent flag management
- AlarmClockInfo manual creation required
> Calendar 계산 수동 처리
> <br>PendingIntent 플래그 직접 관리
> <br>AlarmClockInfo 직접 생성 필요

**Advantages / 장점:**
- One-line register/remove/exist checks
- Auto Calendar calculation and PendingIntent creation
- Internal exception handling and SDK branching
> 등록/삭제/존재 확인을 한 줄로 처리
> <br>Calendar 계산, PendingIntent 생성 자동 처리
> <br>예외 처리 및 SDK 분기 내부 처리

<br>

## 순수 Android 방식 (Plain Android)
```kotlin
// Traditional Alarm registration method (기존의 Alarm 등록 방법)
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
private fun registerAlarm(hour: Int, minute: Int) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }
    }

    val intent = Intent(this, AlarmReceiver::class.java).apply {
        putExtra("ALARM_KEY", 1)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        this,
        1,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // ⚠️ showIntent == triggerIntent: tapping "next alarm" in status bar fires the broadcast
    val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
}

// Alarm removal - Complex handling (Alarm 삭제 - 복잡한 처리)
private fun removeAlarm(key: Int) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(this, AlarmReceiver::class.java).apply {
        putExtra("ALARM_KEY", key)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        this,
        key,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )

    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
```

<br>

## Simple UI Approach (Simple UI 방식)
```kotlin
// AlarmClock registration with result handling (AlarmClock 등록 + 결과 처리)
private fun registerAlarm(hour: Int, minute: Int) {
    val alarmData = AlarmData.createSimple(
        key = 1,
        title = "Alarm Title",
        message = "Alarm Message",
        hour = hour,
        minute = minute
    )
    when (val result = alarmController.registerAlarmClock(AlarmReceiver::class.java, alarmData)) {
        is SystemResult.Success -> { /* 등록 성공 */ }
        is SystemResult.PermissionDenied -> { /* SCHEDULE_EXACT_ALARM 권한 없음 → 설정 유도 */ }
        is SystemResult.Failure -> { /* 오류 처리 */ }
        else -> Unit
    }
}

// Exact Alarm - fires even in Idle mode (Idle 모드에서도 실행되는 Exact 알람)
private fun registerExactAlarm(hour: Int, minute: Int) {
    val alarmData = AlarmData.createExactIdleAllowed(
        key = 2,
        title = "Exact Alarm",
        message = "Exact Alarm Message",
        hour = hour,
        minute = minute
    )
    when (val result = alarmController.registerAlarmExactAndAllowWhileIdle(AlarmReceiver::class.java, alarmData)) {
        is SystemResult.Success -> { /* 등록 성공 */ }
        is SystemResult.PermissionDenied -> { /* SCHEDULE_EXACT_ALARM 권한 없음 → 설정 유도 */ }
        is SystemResult.Failure -> { /* 오류 처리 */ }
        else -> Unit
    }
}

// Alarm removal - One line (알람 삭제 - 한 줄)
private fun removeAlarm(key: Int) {
    alarmController.remove(key, AlarmReceiver::class.java)
}

// Check Alarm exists - One line (알람 존재 확인 - 한 줄)
private fun checkAlarmExists(key: Int): Boolean {
    return alarmController.exists(key, AlarmReceiver::class.java)
}
```

<br>

## BaseAlarmReceiver Implementation (BaseAlarmReceiver 구현)

The Receiver passed to `AlarmController` extends `BaseAlarmReceiver` and connects alarm storage with notification configuration. The following structure is used by the sample app.
> `AlarmController`에 전달한 Receiver는 `BaseAlarmReceiver`를 상속해 알람 저장소와 알림 구성을 연결합니다. 아래 코드는 샘플 앱에서 실제 사용하는 구조입니다.

```kotlin
// Alarm receiver implementation (알람 리시버 구현)
class AlarmReceiver : BaseAlarmReceiver() {
    override val classType: Class<*> = this::class.java
    override val powerManagerAcquireTime: Long = 5_000L

    override fun loadAllAlarmDataList(context: Context): List<AlarmData> =
        AlarmSampleStore.getAll()

    override fun loadAlarmData(
        context: Context,
        intent: Intent,
        key: Int,
    ): AlarmData? = AlarmSampleStore.get(key)

    override fun createNotificationChannel(
        context: Context,
        notification: AlarmNotificationData,
    ) {
        val channel = NotificationChannel(
            "Alarm_ID",
            "Alarm_Name",
            NotificationManager.IMPORTANCE_HIGH,
        )
        notificationController = context.getNotificationController(channel)
    }

    override fun buildNotificationOption(
        context: Context,
        alarmData: AlarmData,
    ): SimpleNotificationOptionBase =
        DefaultNotificationOption(
            notificationId = alarmData.key,
            smallIcon = android.R.drawable.ic_dialog_info,
            title = alarmData.notification.title,
            content = alarmData.notification.message,
            isAutoCancel = false,
        )
}
```

- Implement `loadAllAlarmDataList()` with persistent storage such as a database, file, or Preference because it is used to restore alarms after boot, time, or timezone changes.
- `loadAlarmData()` must return the alarm matching the key delivered through `AlarmConstants.ALARM_KEY`.
- Initialize `notificationController` inside `createNotificationChannel()`. If it is not initialized, notification option creation and display are safely skipped.
- On Android 13+, notification option creation and display are skipped when `POST_NOTIFICATIONS` is not granted. The app is responsible for the permission request UI.
> `loadAllAlarmDataList()`는 부팅·시간·타임존 변경 후 재등록에 사용되므로 DB, 파일, Preference 같은 영속 저장소로 구현해야 합니다.
> <br>`loadAlarmData()`는 `AlarmConstants.ALARM_KEY`로 전달된 키에 대응하는 알람을 반환해야 합니다.
> <br>`createNotificationChannel()`에서 `notificationController`를 초기화해야 합니다. 초기화하지 않으면 알림 옵션 생성과 표시를 안전하게 건너뜁니다.
> <br>Android 13 이상에서 `POST_NOTIFICATIONS`가 허용되지 않으면 알림 옵션 생성과 표시를 건너뜁니다. 권한 요청 UI는 앱에서 처리해야 합니다.

<br>

## Manifest and Permissions (Manifest 및 권한)

Declare the permissions and system broadcast actions required by the Receiver in the app manifest.
> Receiver에 필요한 권한과 시스템 브로드캐스트 액션을 앱 Manifest에 선언합니다.

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission
    android:name="android.permission.SCHEDULE_EXACT_ALARM"
    android:minSdkVersion="31" />
<uses-permission
    android:name="android.permission.POST_NOTIFICATIONS"
    android:minSdkVersion="33" />

<application>
    <receiver
        android:name=".AlarmReceiver"
        android:enabled="true"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.BOOT_COMPLETED" />
            <action android:name="android.intent.action.TIME_CHANGED" />
            <action android:name="android.intent.action.TIMEZONE_CHANGED" />
            <action android:name="android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED" />
        </intent-filter>
    </receiver>
</application>
```

- The Receiver continues processing without a WakeLock when `WAKE_LOCK` is missing or WakeLock acquisition fails.
- `SCHEDULE_EXACT_ALARM` is required for exact alarm registration on API 31+, but not for inexact alarms.
- Declaring `POST_NOTIFICATIONS` does not grant it automatically. The app must request the runtime permission on API 33+.
> `WAKE_LOCK`이 없거나 WakeLock 획득이 실패해도 Receiver는 WakeLock 없이 처리를 계속합니다.
> <br>`SCHEDULE_EXACT_ALARM`은 API 31 이상의 exact 계열 등록에 필요하며, inexact 계열에는 필요하지 않습니다.
> <br>`POST_NOTIFICATIONS`는 선언만으로 허용되지 않습니다. API 33 이상에서는 앱이 런타임 권한을 요청해야 합니다.

<br>

## Related Extensions (관련 확장 함수)
- `getAlarmController()`
- `getNotificationController(NotificationChannel)`

See full list / 전체 목록: [README_SYSTEM_MANAGER_EXTENSIONS.md](../../README_SYSTEM_MANAGER_EXTENSIONS.md)

<br>
