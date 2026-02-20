# AlarmController vs Plain Android - Complete Comparison Guide
> **AlarmController vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.controller.alarm` (패키지)

<br></br>

## Overview (개요)
Provides simple APIs for alarm register/remove/existence checks.  
> 알람 등록/삭제/존재 확인을 간단한 API로 제공합니다.

- Android 13+ notification trigger path checks `POST_NOTIFICATIONS` first and safely skips when missing.
> Android 13+에서 알림 트리거 시 `POST_NOTIFICATIONS` 권한을 먼저 확인하고, 권한이 없으면 크래시 없이 스킵합니다.

<br></br>

## At a Glance (한눈 비교)
| Item (항목) | Plain Android (기본 방식) | Simple UI (Simple UI) | Notes (비고) |
|---|---|---|---|
| Time calculation | Manual Calendar calculation | VO-based auto calculation | Includes today/tomorrow branching<br>오늘/내일 분기 포함 |
| PendingIntent | Manual creation + flag management | Created internally | Key-based management<br>키 기반 관리 |
| AlarmClockInfo | Manual creation | Handled internally | Less boilerplate<br>코드 간소화 |
| Remove / Exists check | Manual query/cancel | `remove()` / `exists()` | One-line call<br>한 줄 호출 |
| Alarm trigger safety | Caller-side permission handling | Internal guard in receiver | Android 13+ `POST_NOTIFICATIONS` pre-check<br>권한 누락 시 안전 스킵 |
| Permission / SDK branching | Handled by caller | Handled internally | Exact alarm permission required<br>Exact 알람 권한 주의 |

<br></br>

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

<br></br>

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

<br></br>

## Simple UI Approach (Simple UI 방식)
```kotlin
// Simple Alarm registration - One line (간단한 Alarm 등록 - 한 줄)
private fun registerAlarm(hour: Int, minute: Int) {
    val AlarmVO = AlarmVO(
        key = 1,
        title = "Alarm Title",
        message = "Alarm Message",
        hour = hour,
        minute = minute,
        second = 0
    )
    getAlarmController().registerAlarmClock(AlarmReceiver::class.java, AlarmVO)
}

// Exact Alarm (Idle 모드에서도 실행)
private fun registerExactAlarm(hour: Int, minute: Int) {
    val AlarmVO = AlarmVO(
        key = 2,
        title = "Exact Alarm",
        message = "Exact Alarm Message",
        hour = hour,
        minute = minute
    )
    getAlarmController().registerAlarmExactAndAllowWhileIdle(AlarmReceiver::class.java, AlarmVO)
}

// Alarm removal - One line (알람 삭제 - 한 줄)
private fun removeAlarm(key: Int) {
    getAlarmController().remove(key, AlarmReceiver::class.java)
}

// Check Alarm exists - One line (알람 존재 확인 - 한 줄)
private fun checkAlarmExists(key: Int): Boolean {
    return getAlarmController().exists(key, AlarmReceiver::class.java)
}
```

<br></br>

## Related Extensions (관련 확장 함수)
- `getAlarmController()`  
  See full list / 전체 목록: [README_SYSTEM_MANAGER_EXTENSIONS.md](../../README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>


