package kr.open.library.simple_ui.core.system_manager.controller.alarm

import android.Manifest.permission.SCHEDULE_EXACT_ALARM
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmIdleMode
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVO
import kr.open.library.simple_ui.core.system_manager.extensions.getAlarmManager
import java.util.Calendar

/**
 * Controller for managing Android alarm operations with enhanced safety and reliability.<br>
 * Provides methods to register different types of alarms and handles calendar logic properly.<br><br>
 * Android 알람 작업을 안전하고 안정적으로 관리하는 컨트롤러입니다.<br>
 * 다양한 알람 등록 메서드를 제공하며 캘린더 로직을 안전하게 처리합니다.<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트입니다.<br>
 */
public open class AlarmController(
    context: Context,
) : BaseSystemService(
        context,
        checkSdkVersion<List<String>?>(Build.VERSION_CODES.S) {
            listOf(SCHEDULE_EXACT_ALARM)
        },
    ) {
    /**
     * Lazy-initialized AlarmManager instance for scheduling and managing alarms.<br><br>
     * 알람 예약 및 관리를 위한 지연 초기화 AlarmManager 인스턴스입니다.<br>
     */
    public val alarmManager: AlarmManager by lazy { context.getAlarmManager() }

    /**
     * Registers an alarm clock that will wake up the device and show in the status bar.<br><br>
     * 기기를 깨우고 상태바에 표시되는 알람 시계를 등록합니다.<br>
     *
     * @param receiver The BroadcastReceiver class to handle the alarm.<br><br>
     *                 알람을 처리할 BroadcastReceiver 클래스입니다.<br>
     * @param alarmVo The alarm data containing schedule and notification metadata.<br><br>
     *                스케줄과 알림 메타데이터를 포함한 알람 데이터입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @return Boolean true if alarm was registered successfully, false otherwise.<br><br>
     *         알람 등록 성공 시 true, 실패 시 false입니다.<br>
     */
    @RequiresPermission(SCHEDULE_EXACT_ALARM, conditional = true)
    public fun registerAlarmClock(receiver: Class<*>, alarmVo: AlarmVO, namespace: String? = null): Boolean = tryCatchSystemManager(false) {
        if (!ensureExactAlarmAllowedOrLog()) return@tryCatchSystemManager false

        val calendar = getCalendar(alarmVo)
        val pendingIntent = getAlarmPendingIntent(receiver, alarmVo.key, namespace) ?: return@tryCatchSystemManager false

        val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        Logx.d("Alarm clock registered for key: ${alarmVo.key} at ${calendar.time}")
        return true
    }

    /**
     * Registers an exact alarm that can fire while the device is in idle mode.<br><br>
     * 기기가 유휴 모드일 때도 정확하게 동작하는 알람을 등록합니다.<br>
     *
     * @param receiver The BroadcastReceiver class to handle the alarm.<br><br>
     *                 알람을 처리할 BroadcastReceiver 클래스입니다.<br>
     * @param alarmVo The alarm data containing schedule and notification metadata.<br><br>
     *                스케줄과 알림 메타데이터를 포함한 알람 데이터입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @return Boolean true if alarm was registered successfully, false otherwise.<br><br>
     *         알람 등록 성공 시 true, 실패 시 false입니다.<br>
     */
    @RequiresPermission(SCHEDULE_EXACT_ALARM, conditional = true)
    public fun registerAlarmExactAndAllowWhileIdle(receiver: Class<*>, alarmVo: AlarmVO, namespace: String? = null): Boolean =
        tryCatchSystemManager(false) {
            if (!ensureExactAlarmAllowedOrLog()) return@tryCatchSystemManager false

            val calendar = getCalendar(alarmVo)
            val pendingIntent = getAlarmPendingIntent(receiver, alarmVo.key, namespace) ?: return@tryCatchSystemManager false

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            Logx.d("Exact alarm registered for key: ${alarmVo.key} at ${calendar.time}")
            return true
        }

    /**
     * Registers an alarm that can fire while the device is in idle mode (less precise than exact).<br><br>
     * 유휴 모드에서도 동작하지만 정확도가 낮은 알람을 등록합니다.<br>
     *
     * @param receiver The BroadcastReceiver class to handle the alarm.<br><br>
     *                 알람을 처리할 BroadcastReceiver 클래스입니다.<br>
     * @param alarmVo The alarm data containing schedule and notification metadata.<br><br>
     *                스케줄과 알림 메타데이터를 포함한 알람 데이터입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @return Boolean true if alarm was registered successfully, false otherwise.<br><br>
     *         알람 등록 성공 시 true, 실패 시 false입니다.<br>
     */
    public fun registerAlarmAndAllowWhileIdle(receiver: Class<*>, alarmVo: AlarmVO, namespace: String? = null): Boolean =
        tryCatchSystemManager(false) {
            val calendar = getCalendar(alarmVo)
            val pendingIntent = getAlarmPendingIntent(receiver, alarmVo.key, namespace) ?: return@tryCatchSystemManager false

            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            Logx.d("Idle-allowed alarm registered for key: ${alarmVo.key} at ${calendar.time}")
            return true
        }

    /**
     * Registers a repeating alarm (inexact on API 19+).<br><br>
     * API 19+부터는 배터리 최적화로 inexact로 동작하는 반복 알람을 등록합니다.<br>
     * intervalMillis가 최소값보다 작으면 보정(클램핑)됩니다.<br>
     *
     * @param receiver The BroadcastReceiver class to handle the alarm.<br><br>
     *                 알람을 처리할 BroadcastReceiver 클래스입니다.<br>
     * @param alarmVo The alarm data containing schedule and notification metadata.<br><br>
     *                스케줄과 알림 메타데이터를 포함한 알람 데이터입니다.<br>
     * @param intervalMillis Repeat interval in milliseconds (minimum 60,000ms).<br><br>
     *                      반복 주기(밀리초)이며 최소 60,000ms입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @param type Alarm type such as RTC_WAKEUP or ELAPSED_REALTIME_WAKEUP.<br><br>
     *             RTC_WAKEUP 또는 ELAPSED_REALTIME_WAKEUP 등 알람 타입입니다.<br>
     * @return Boolean true if alarm was registered successfully, false otherwise.<br><br>
     *         알람 등록 성공 시 true, 실패 시 false입니다.<br>
     */
    public fun registerRepeating(
        receiver: Class<*>,
        alarmVo: AlarmVO,
        intervalMillis: Long,
        namespace: String? = null,
        type: Int = AlarmManager.RTC_WAKEUP,
    ): Boolean = tryCatchSystemManager(false) {
        val safeInterval = resolveRepeatingInterval(intervalMillis)
        val calendar = getCalendar(alarmVo)
        val pendingIntent = getAlarmPendingIntent(receiver, alarmVo.key, namespace)
            ?: return@tryCatchSystemManager false

        alarmManager.setRepeating(type, calendar.timeInMillis, safeInterval, pendingIntent)
        Logx.d("Repeating alarm registered for key: ${alarmVo.key} at ${calendar.time}, interval: $safeInterval")
        return true
    }

    /**
     * Registers an alarm based on AlarmScheduleVO.idleMode.<br><br>
     * AlarmScheduleVO.idleMode 값을 기준으로 알람 등록 API를 자동 선택합니다.<br>
     * NONE은 알람 시계(상태바 표시) 타입을 사용합니다.<br>
     *
     * @param receiver The BroadcastReceiver class to handle the alarm.<br><br>
     *                 알람을 처리할 BroadcastReceiver 클래스입니다.<br>
     * @param alarmVo The alarm data containing schedule and notification metadata.<br><br>
     *                스케줄과 알림 메타데이터를 포함한 알람 데이터입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @return Boolean true if alarm was registered successfully, false otherwise.<br><br>
     *         알람 등록 성공 시 true, 실패 시 false입니다.<br>
     */
    public fun registerBySchedule(
        receiver: Class<*>,
        alarmVo: AlarmVO,
        namespace: String? = null,
    ): Boolean = when (alarmVo.schedule.idleMode) {
        AlarmIdleMode.NONE -> registerAlarmClock(receiver, alarmVo, namespace)
        AlarmIdleMode.INEXACT -> registerAlarmAndAllowWhileIdle(receiver, alarmVo, namespace)
        AlarmIdleMode.EXACT -> registerAlarmExactAndAllowWhileIdle(receiver, alarmVo, namespace)
    }

    /**
     * Updates an alarm clock by removing and re-registering.<br><br>
     * 알람 시계를 삭제 후 재등록하여 업데이트합니다.<br>
     *
     * @param receiver The BroadcastReceiver class to handle the alarm.<br><br>
     *                 알람을 처리할 BroadcastReceiver 클래스입니다.<br>
     * @param alarmVo The alarm data containing schedule and notification metadata.<br><br>
     *                스케줄과 알림 메타데이터를 포함한 알람 데이터입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @return Boolean true if alarm was updated successfully, false otherwise.<br><br>
     *         알람 업데이트 성공 시 true, 실패 시 false입니다.<br>
     */
    public fun updateAlarmClock(
        receiver: Class<*>,
        alarmVo: AlarmVO,
        namespace: String? = null,
    ): Boolean = updateInternal(alarmVo.key, receiver, namespace) {
        registerAlarmClock(receiver, alarmVo, namespace)
    }

    /**
     * Updates an exact idle-allowed alarm by removing and re-registering.<br><br>
     * 정확 + 유휴 모드 허용 알람을 삭제 후 재등록하여 업데이트합니다.<br>
     *
     * @param receiver The BroadcastReceiver class to handle the alarm.<br><br>
     *                 알람을 처리할 BroadcastReceiver 클래스입니다.<br>
     * @param alarmVo The alarm data containing schedule and notification metadata.<br><br>
     *                스케줄과 알림 메타데이터를 포함한 알람 데이터입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @return Boolean true if alarm was updated successfully, false otherwise.<br><br>
     *         알람 업데이트 성공 시 true, 실패 시 false입니다.<br>
     */
    public fun updateExactAndAllowWhileIdle(
        receiver: Class<*>,
        alarmVo: AlarmVO,
        namespace: String? = null,
    ): Boolean = updateInternal(alarmVo.key, receiver, namespace) {
        registerAlarmExactAndAllowWhileIdle(receiver, alarmVo, namespace)
    }

    /**
     * Updates an idle-allowed alarm by removing and re-registering.<br><br>
     * 유휴 모드 허용 알람을 삭제 후 재등록하여 업데이트합니다.<br>
     *
     * @param receiver The BroadcastReceiver class to handle the alarm.<br><br>
     *                 알람을 처리할 BroadcastReceiver 클래스입니다.<br>
     * @param alarmVo The alarm data containing schedule and notification metadata.<br><br>
     *                스케줄과 알림 메타데이터를 포함한 알람 데이터입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @return Boolean true if alarm was updated successfully, false otherwise.<br><br>
     *         알람 업데이트 성공 시 true, 실패 시 false입니다.<br>
     */
    public fun updateAllowWhileIdle(
        receiver: Class<*>,
        alarmVo: AlarmVO,
        namespace: String? = null,
    ): Boolean = updateInternal(alarmVo.key, receiver, namespace) {
        registerAlarmAndAllowWhileIdle(receiver, alarmVo, namespace)
    }

    /**
     * Updates a repeating alarm by removing and re-registering.<br><br>
     * 반복 알람을 삭제 후 재등록하여 업데이트합니다.<br>
     *
     * @param receiver The BroadcastReceiver class to handle the alarm.<br><br>
     *                 알람을 처리할 BroadcastReceiver 클래스입니다.<br>
     * @param alarmVo The alarm data containing schedule and notification metadata.<br><br>
     *                스케줄과 알림 메타데이터를 포함한 알람 데이터입니다.<br>
     * @param intervalMillis Repeat interval in milliseconds (minimum 60,000ms).<br><br>
     *                      반복 주기(밀리초)이며 최소 60,000ms입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @param type Alarm type such as RTC_WAKEUP or ELAPSED_REALTIME_WAKEUP.<br><br>
     *             RTC_WAKEUP 또는 ELAPSED_REALTIME_WAKEUP 등 알람 타입입니다.<br>
     * @return Boolean true if alarm was updated successfully, false otherwise.<br><br>
     *         알람 업데이트 성공 시 true, 실패 시 false입니다.<br>
     */
    public fun updateRepeating(
        receiver: Class<*>,
        alarmVo: AlarmVO,
        intervalMillis: Long,
        namespace: String? = null,
        type: Int = AlarmManager.RTC_WAKEUP,
    ): Boolean = updateInternal(alarmVo.key, receiver, namespace) {
        registerRepeating(receiver, alarmVo, intervalMillis, namespace, type)
    }

    /**
     * Creates a PendingIntent for alarm operations with proper error handling.<br><br>
     * 알람 작업을 위한 PendingIntent를 안전하게 생성합니다.<br>
     *
     * @param receiver The BroadcastReceiver class to handle the alarm.<br><br>
     *                 알람을 처리할 BroadcastReceiver 클래스입니다.<br>
     * @param key The unique identifier for the alarm.<br><br>
     *            알람의 고유 식별자입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @return PendingIntent for the alarm, or null if creation fails.<br><br>
     *         알람 PendingIntent이며, 생성 실패 시 null입니다.<br>
     */
    private fun getAlarmPendingIntent(receiver: Class<*>, key: Int, namespace: String?): PendingIntent? = tryCatchSystemManager(null) {
        val requestCode = resolveRequestCode(receiver, key, namespace)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, receiver).apply { putExtra(AlarmConstants.ALARM_KEY, key) },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /**
     * Builds a stable requestCode using receiver and namespace.<br><br>
     * receiver.name을 기본 네임스페이스로 사용하며, namespace가 비어있으면 기본값을 씁니다.<br>
     * 동일 key라도 namespace가 다르면 서로 다른 requestCode가 됩니다.<br>
     *
     * @param receiver The BroadcastReceiver class used for PendingIntent.<br><br>
     *                 PendingIntent에 사용할 BroadcastReceiver 클래스입니다.<br>
     * @param key The unique identifier for the alarm.<br><br>
     *            알람 고유 식별자입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @return Calculated requestCode for PendingIntent.<br><br>
     *         계산된 PendingIntent requestCode입니다.<br>
     */
    private fun resolveRequestCode(receiver: Class<*>, key: Int, namespace: String?): Int {
        val effectiveNamespace = namespace?.takeIf { it.isNotBlank() } ?: receiver.name
        return (effectiveNamespace.hashCode() * 31) xor key
    }

    /**
     * Ensures the repeating interval respects a minimum bound.<br><br>
     * 반복 주기가 최소값보다 작으면 최소값으로 보정합니다.<br>
     *
     * @param intervalMillis Requested repeating interval in milliseconds.<br><br>
     *                       요청된 반복 주기(밀리초)입니다.<br>
     * @return Safe interval in milliseconds.<br><br>
     *         보정된 반복 주기(밀리초)입니다.<br>
     */
    private fun resolveRepeatingInterval(intervalMillis: Long): Long = if (intervalMillis < MIN_REPEATING_INTERVAL_MS) {
        Logx.w("intervalMillis($intervalMillis) < $MIN_REPEATING_INTERVAL_MS ms. Clamping to $MIN_REPEATING_INTERVAL_MS ms.")
        MIN_REPEATING_INTERVAL_MS
    } else {
        intervalMillis
    }

    /**
     * Common update routine: remove if exists, then register.<br><br>
     * 기존 알람이 있으면 제거 후 등록합니다.<br>
     *
     * @param key The unique identifier for the alarm.<br><br>
     *            알람 고유 식별자입니다.<br>
     * @param receiver The BroadcastReceiver class used when creating the alarm.<br><br>
     *                 알람 생성에 사용된 BroadcastReceiver 클래스입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @param registerBlock Registration function to execute.<br><br>
     *                      실제 등록을 수행하는 함수입니다.<br>
     * @return Boolean true if update succeeded, false otherwise.<br><br>
     *         업데이트 성공 시 true, 실패 시 false입니다.<br>
     */
    private inline fun updateInternal(
        key: Int,
        receiver: Class<*>,
        namespace: String?,
        registerBlock: () -> Boolean,
    ): Boolean = tryCatchSystemManager(false) {
        if (exists(key, receiver, namespace)) {
            remove(key, receiver, namespace)
        }
        return@tryCatchSystemManager registerBlock()
    }

    /**
     * Creates a Calendar instance for the alarm time, properly handling next-day scheduling.<br><br>
     * 알람 스케줄 시간으로 Calendar를 생성하며, 오늘 시간이 지났으면 다음 날로 보정합니다.<br>
     * 특정 날짜가 지정된 경우 과거 날짜면 예외를 발생시킵니다.<br>
     *
     * @param alarmVo The alarm data containing schedule information.<br><br>
     *                스케줄 정보를 포함한 알람 데이터입니다.<br>
     * @return Calendar instance set to the appropriate alarm time.<br><br>
     *         알람 시간으로 설정된 Calendar 인스턴스입니다.<br>
     * @throws IllegalArgumentException If the specified date/time is in the past.<br><br>
     *                                 특정 날짜/시간이 과거인 경우 발생합니다.<br>
     */
    private fun getCalendar(alarmVo: AlarmVO): Calendar {
        val now = Calendar.getInstance()
        val schedule = alarmVo.schedule
        val alarmTime = Calendar.getInstance().apply {
            schedule.date?.let { date ->
                set(Calendar.YEAR, date.year)
                set(Calendar.MONTH, date.month - 1)
                set(Calendar.DAY_OF_MONTH, date.day)
            }
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, schedule.second)
            set(Calendar.MILLISECOND, 0) // Reset milliseconds for precise comparison
        }

        if (schedule.date == null) {
            // If the alarm time has already passed today, schedule it for tomorrow
            if (alarmTime.before(now)) {
                alarmTime.add(Calendar.DATE, 1)
                Logx.d("Alarm time has passed today, scheduling for tomorrow: ${alarmTime.time}")
            } else {
                Logx.d("Alarm scheduled for today: ${alarmTime.time}")
            }
        } else {
            require(!alarmTime.before(now)) { "Specified date/time is in the past: ${alarmTime.time}" }
            Logx.d("Alarm scheduled for specific date: ${alarmTime.time}")
        }

        return alarmTime
    }

    /**
     * Removes an existing alarm by its key.<br><br>
     * 키로 기존 알람을 제거합니다.<br>
     *
     * @param key The unique identifier for the alarm to remove.<br><br>
     *            제거할 알람의 고유 식별자입니다.<br>
     * @param receiver The BroadcastReceiver class used when creating the alarm.<br><br>
     *                 알람 생성에 사용된 BroadcastReceiver 클래스입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @return Boolean true if alarm was found and cancelled, false if not found.<br><br>
     *         알람을 찾아 취소했으면 true, 찾지 못하면 false입니다.<br>
     */
    public fun remove(key: Int, receiver: Class<*>, namespace: String? = null): Boolean = tryCatchSystemManager(false) {
        val intent = Intent(context, receiver).apply {
            putExtra(AlarmConstants.ALARM_KEY, key)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            resolveRequestCode(receiver, key, namespace),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )

        return if (pendingIntent != null) {
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
     * Checks if an alarm with the given key exists.<br><br>
     * 주어진 키의 알람 존재 여부를 확인합니다.<br>
     *
     * @param key The unique identifier for the alarm.<br><br>
     *            알람의 고유 식별자입니다.<br>
     * @param receiver The BroadcastReceiver class used when creating the alarm.<br><br>
     *                 알람 생성에 사용된 BroadcastReceiver 클래스입니다.<br>
     * @param namespace Optional namespace to avoid requestCode collisions.<br><br>
     *                  requestCode 충돌 방지를 위한 선택 네임스페이스입니다.<br>
     * @return Boolean true if alarm exists, false otherwise.<br><br>
     *         알람이 존재하면 true, 없으면 false입니다.<br>
     */
    public fun exists(key: Int, receiver: Class<*>, namespace: String? = null): Boolean = tryCatchSystemManager(false) {
        val intent = Intent(context, receiver).apply {
            putExtra(AlarmConstants.ALARM_KEY, key)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            resolveRequestCode(receiver, key, namespace),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        return pendingIntent != null
    }

    /**
     * Checks whether exact alarms can be scheduled on this device.<br><br>
     * 이 기기에서 정확 알람을 등록할 수 있는지 확인합니다.<br>
     *
     * @return True if exact alarms are allowed, false otherwise.<br><br>
     *         정확 알람 허용 시 true, 미허용 시 false입니다.<br>
     */
    public fun canScheduleExactAlarms(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    /**
     * Builds an intent to request exact alarm permission in Settings.<br><br>
     * 설정 화면에서 정확 알람 권한을 요청하기 위한 Intent를 생성합니다.<br>
     *
     * @return Intent for permission request, or null if not needed.<br><br>
     *         권한 요청이 필요 없으면 null입니다.<br>
     */
    public fun buildExactAlarmPermissionIntent(): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        if (alarmManager.canScheduleExactAlarms()) return null

        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Checks exact alarm permission and logs warning when denied (API 31+).<br><br>
     * API 31+에서 정확 알람 권한을 확인하고, 거부 시 경고 로그를 남깁니다.<br>
     *
     * @return True if allowed or pre-S, false otherwise.<br><br>
     *         허용 또는 S 미만이면 true, 그 외 false입니다.<br>
     */
    private fun ensureExactAlarmAllowedOrLog(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        if (alarmManager.canScheduleExactAlarms()) return true

        Logx.w("Exact alarm permission denied. Request permission in Settings first.")
        return false
    }

    private companion object {
        /**
         * Minimum repeating interval in milliseconds (1 minute).<br><br>
         * 반복 알람 최소 간격(밀리초)이며 1분입니다.<br>
         */
        private const val MIN_REPEATING_INTERVAL_MS: Long = 60_000L
    }
}
