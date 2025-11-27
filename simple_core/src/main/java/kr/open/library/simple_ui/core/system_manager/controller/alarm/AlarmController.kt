package kr.open.library.simple_ui.core.system_manager.controller.alarm

import android.Manifest.permission.SCHEDULE_EXACT_ALARM
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmConstants
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVo
import kr.open.library.simple_ui.core.system_manager.extensions.getAlarmManager


/**
 * Controller for managing Android alarm operations with enhanced safety and reliability.
 * Provides methods to register different types of alarms and handles calendar logic properly.
 * 
 * Android 알람 작업을 안전하고 신뢰성 있게 관리하는 컨트롤러입니다.
 * 다양한 유형의 알람을 등록하는 메서드를 제공하고 캘린더 로직을 올바르게 처리합니다.
 *
 * @param context The application context
 */
public open class AlarmController(context: Context) :
    BaseSystemService(context,
        checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = { listOf(SCHEDULE_EXACT_ALARM) },
            negativeWork = { emptyList() }
        )
    ) {

    /**
     * Lazy-initialized AlarmManager instance for scheduling and managing alarms.<br><br>
     * 알람 스케줄링 및 관리를 위한 지연 초기화된 AlarmManager 인스턴스입니다.<br>
     */
    public val alarmManager: AlarmManager by lazy { context.getAlarmManager() }

    /**
     * Registers an alarm clock that will wake up the device and show in the status bar.
     * 기기를 깨우고 상태 표시줄에 표시되는 알람 시계를 등록합니다.
     *
     * @param receiver The BroadcastReceiver class to handle the alarm
     * @param alarmVo The alarm data containing time and metadata
     * @return Boolean true if alarm was registered successfully, false otherwise
     */
    public fun registerAlarmClock(receiver: Class<*>, alarmVo: AlarmVo): Boolean = tryCatchSystemManager(false) {
        val calendar = getCalendar(alarmVo)
        val pendingIntent = getAlarmPendingIntent(receiver, alarmVo.key) ?: return@tryCatchSystemManager false

        val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        Logx.d("Alarm clock registered for key: ${alarmVo.key} at ${calendar.time}")
        true
    }

    /**
     * Registers an exact alarm that can fire while the device is in idle mode.
     * 기기가 유휴 모드일 때도 실행될 수 있는 정확한 알람을 등록합니다.
     *
     * @param receiver The BroadcastReceiver class to handle the alarm
     * @param alarmVo The alarm data containing time and metadata
     * @return Boolean true if alarm was registered successfully, false otherwise
     */
    public fun registerAlarmExactAndAllowWhileIdle(receiver: Class<*>, alarmVo: AlarmVo): Boolean = tryCatchSystemManager(false) {
        val calendar = getCalendar(alarmVo)
        val pendingIntent = getAlarmPendingIntent(receiver, alarmVo.key) ?: return@tryCatchSystemManager false

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Logx.d("Exact alarm registered for key: ${alarmVo.key} at ${calendar.time}")
        true
    }

    /**
     * Registers an alarm that can fire while the device is in idle mode (less precise than exact).
     * 기기가 유휴 모드일 때도 실행될 수 있는 알람을 등록합니다 (정확한 알람보다 덜 정밀함).
     *
     * @param receiver The BroadcastReceiver class to handle the alarm
     * @param alarmVo The alarm data containing time and metadata
     * @return Boolean true if alarm was registered successfully, false otherwise
     */
    public fun registerAlarmAndAllowWhileIdle(receiver: Class<*>, alarmVo: AlarmVo): Boolean = tryCatchSystemManager(false) {
        val calendar = getCalendar(alarmVo)
        val pendingIntent = getAlarmPendingIntent(receiver, alarmVo.key) ?: return@tryCatchSystemManager false

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Logx.d("Idle-allowed alarm registered for key: ${alarmVo.key} at ${calendar.time}")
        true
    }

    /**
     * Creates a PendingIntent for alarm operations with proper error handling.
     * 알람 작업을 위한 PendingIntent를 안전하게 생성합니다.
     */
    private fun getAlarmPendingIntent(receiver: Class<*>, key: Int): PendingIntent? = tryCatchSystemManager(null) {
        PendingIntent.getBroadcast(
            context,
            key,
            Intent(context, receiver).apply {
                putExtra(AlarmConstants.ALARM_KEY, key)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Creates a Calendar instance for the alarm time, properly handling next-day scheduling.
     * 알람 시간에 대한 Calendar 인스턴스를 생성하며, 다음 날 스케줄링을 올바르게 처리합니다.
     *
     * @param alarmVo The alarm data containing hour, minute, and second
     * @return Calendar instance set to the appropriate alarm time
     */
    private fun getCalendar(alarmVo: AlarmVo): Calendar {
        val now = Calendar.getInstance()
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarmVo.hour)
            set(Calendar.MINUTE, alarmVo.minute)
            set(Calendar.SECOND, alarmVo.second)
            set(Calendar.MILLISECOND, 0) // Reset milliseconds for precise comparison
        }
        
        // If the alarm time has already passed today, schedule it for tomorrow
        if (alarmTime.before(now)) {
            alarmTime.add(Calendar.DATE, 1)
            Logx.d("Alarm time has passed today, scheduling for tomorrow: ${alarmTime.time}")
        } else {
            Logx.d("Alarm scheduled for today: ${alarmTime.time}")
        }
        
        return alarmTime
    }

    /**
     * Removes an existing alarm by its key.
     * 키를 사용하여 기존 알람을 제거합니다.
     *
     * @param key The unique identifier for the alarm to remove
     * @param receiver The BroadcastReceiver class used when creating the alarm
     * @return Boolean true if alarm was found and cancelled, false if not found
     */
    public fun remove(key: Int, receiver: Class<*>): Boolean = tryCatchSystemManager(false) {
        val intent = Intent(context, receiver).apply {
            putExtra(AlarmConstants.ALARM_KEY, key)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            key,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Logx.d("Alarm with key $key cancelled successfully")
            true
        } else {
            Logx.w("No alarm found with key $key")
            false
        }
    }
    
    /**
     * Checks if an alarm with the given key exists.
     * 주어진 키를 가진 알람이 존재하는지 확인합니다.
     *
     * @param key The unique identifier for the alarm
     * @param receiver The BroadcastReceiver class used when creating the alarm
     * @return Boolean true if alarm exists, false otherwise
     */
    public fun exists(key: Int, receiver: Class<*>): Boolean = tryCatchSystemManager(false) {
        val intent = Intent(context, receiver).apply {
            putExtra(AlarmConstants.ALARM_KEY, key)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            key,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }
}