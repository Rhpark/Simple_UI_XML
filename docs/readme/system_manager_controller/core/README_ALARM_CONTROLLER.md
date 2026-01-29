# AlarmController vs Plain Android - Complete Comparison Guide
> **AlarmController vs 순수 Android - 완벽 비교 가이드**

## 📦 Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.controller.alarm`

<br></br>

## 개요
알람 등록/삭제/존재 확인을 간단한 API로 제공합니다.

<br></br>

## 🔎 At a Glance (한눈 비교)
| Item (항목) | Plain Android (기본 방식) | Simple UI (Simple UI) | Notes (비고) |
|---|---|---|---|
| Time calculation<br>시간 계산 | Manual Calendar calculation<br>Calendar 직접 계산 | VO-based auto calculation<br>VO 기반 자동 계산 | Includes today/tomorrow branching<br>오늘/내일 분기 포함 |
| PendingIntent<br>PendingIntent | Manual creation + flag management<br>직접 생성 + 플래그 관리 | Created internally<br>내부에서 생성 | Key-based management<br>키 기반 관리 |
| AlarmClockInfo<br>AlarmClockInfo | Manual creation<br>직접 생성 | Handled internally<br>내부 처리 | Less boilerplate<br>코드 간소화 |
| Remove / Exists check<br>삭제/존재 확인 | Manual query/cancel<br>직접 조회/취소 | `remove()` / `exists()`<br>`remove()`/`exists()` | One-line call<br>한 줄 호출 |
| Permission / SDK branching<br>권한/SDK 분기 | Handled by caller<br>호출부에서 직접 처리 | Handled internally<br>내부 처리 | Exact alarm permission required<br>Exact 알람 권한 주의 |

<br></br>

## 💡 Why It Matters (왜 중요한가)
**문제점:**
- Calendar 계산 수동 처리
- PendingIntent 플래그 직접 관리
- AlarmClockInfo 직접 생성 필요

**장점:**
- 등록/삭제/존재 확인을 한 줄로 처리
- Calendar 계산, PendingIntent 생성 자동 처리
- 예외 처리 및 SDK 분기 내부 처리
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

## Simple UI 방식
```kotlin
// Simple Alarm registration - One line (간단한 Alarm 등록 - 한 줄)
private fun registerAlarm(hour: Int, minute: Int) {
    val alarmVo = AlarmVo(
        key = 1,
        title = "Alarm Title",
        message = "Alarm Message",
        hour = hour,
        minute = minute,
        second = 0
    )
    getAlarmController().registerAlarmClock(AlarmReceiver::class.java, alarmVo)
}

// Exact Alarm (Idle 모드에서도 실행)
private fun registerExactAlarm(hour: Int, minute: Int) {
    val alarmVo = AlarmVo(
        key = 2,
        title = "Exact Alarm",
        message = "Exact Alarm Message",
        hour = hour,
        minute = minute
    )
    getAlarmController().registerAlarmExactAndAllowWhileIdle(AlarmReceiver::class.java, alarmVo)
}

// Alarm removal - One line
private fun removeAlarm(key: Int) {
    getAlarmController().remove(key, AlarmReceiver::class.java)
}

// Check Alarm exists - One line
private fun checkAlarmExists(key: Int): Boolean {
    return getAlarmController().exists(key, AlarmReceiver::class.java)
}
```

<br></br>

## 관련 확장 함수
- `getAlarmController()`  
  자세한 목록: [../xml/README_SYSTEM_MANAGER_EXTENSIONS.md](../xml/README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>
