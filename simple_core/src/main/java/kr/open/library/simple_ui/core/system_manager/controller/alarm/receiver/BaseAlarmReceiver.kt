package kr.open.library.simple_ui.core.system_manager.controller.alarm.receiver

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmConstants.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmConstants.ALARM_KEY
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmConstants.ALARM_KEY_DEFAULT_VALUE
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmConstants.WAKELOCK_TAG
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmConstants.WAKELOCK_TIMEOUT_MS
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmController
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmIdleMode
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmNotificationVO
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVO
import kr.open.library.simple_ui.core.system_manager.controller.notification.SimpleNotificationController
import kr.open.library.simple_ui.core.system_manager.controller.notification.SimpleNotificationType
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.SimpleNotificationOptionBase
import kr.open.library.simple_ui.core.system_manager.extensions.getAlarmController
import kr.open.library.simple_ui.core.system_manager.extensions.getPowerManager

/**
 * Base class for handling alarm broadcasts with safe WakeLock management.<br>
 * Provides a template method pattern for alarm processing with proper resource management.<br><br>
 * WakeLock을 안전하게 관리하며 알람 브로드캐스트를 처리하는 기본 클래스입니다.<br>
 * 적절한 리소스 관리를 위한 템플릿 메서드 패턴을 제공합니다.<br>
 *
 * @constructor Creates a BaseAlarmReceiver instance.<br><br>
 *              BaseAlarmReceiver 인스턴스를 생성합니다.<br>
 */
public abstract class BaseAlarmReceiver : BroadcastReceiver() {
    /**
     * Controller for managing alarm notifications.<br>
     * Must be initialized by subclasses before use.<br><br>
     * 알람 알림 관리를 위한 컨트롤러입니다.<br>
     * 사용 전에 하위 클래스에서 초기화해야 합니다(예: createNotificationChannel 내부에서 설정).<br>
     */
    protected lateinit var notificationController: SimpleNotificationController

    /**
     * Resolves the alarm registration type for a given alarm.<br><br>
     * 알람 데이터에 따른 등록 타입을 결정합니다.<br>
     * AlarmIdleMode에 따라 AlarmController 등록 API가 결정됩니다.<br>
     *
     * @param alarmVo The alarm data to resolve type for.<br><br>
     *                등록 타입 결정을 위한 알람 데이터입니다.<br>
     * @return The resolved registration type.<br><br>
     *         결정된 알람 등록 타입입니다.<br>
     */
    protected open fun resolveRegisterType(alarmVo: AlarmVO): RegisterType =
        when (alarmVo.schedule.idleMode) {
            AlarmIdleMode.NONE -> RegisterType.ALARM_CLOCK
            AlarmIdleMode.INEXACT -> RegisterType.ALARM_AND_ALLOW_WHILE_IDLE
            AlarmIdleMode.EXACT -> RegisterType.ALARM_EXACT_AND_ALLOW_WHILE_IDLE
        }

    /**
     * Class type of the BroadcastReceiver for creating PendingIntents.<br><br>
     * PendingIntent 생성을 위한 BroadcastReceiver 클래스 타입입니다.<br>
     * 일반적으로 현재 Receiver 클래스(this::class.java)를 반환합니다.<br>
     */
    protected abstract val classType: Class<*>

    /**
     * Resolves a namespace used to avoid PendingIntent requestCode collisions.<br><br>
     * PendingIntent requestCode 충돌 방지를 위한 네임스페이스를 반환합니다.<br>
     * null이면 기본 전략(receiver.name)을 사용합니다.<br>
     *
     * @param alarmVo The alarm data to resolve namespace for.<br><br>
     *                네임스페이스 결정을 위한 알람 데이터입니다.<br>
     * @return Optional namespace string, or null to use the default strategy.<br><br>
     *         네임스페이스 문자열이며, null이면 기본 전략을 사용합니다.<br>
     */
    protected open fun resolveAlarmNamespace(alarmVo: AlarmVO): String? = null

    /**
     * Creates a notification channel for alarm notifications.<br>
     * Must be called before showing any notifications on Android O and above.<br><br>
     * 알람 알림 채널을 생성합니다.<br>
     * Android O 이상에서 알림 표시 전에 반드시 호출해야 합니다.<br>
     * 이 시점에 notificationController를 초기화하는 것을 권장합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트입니다.<br>
     * @param notification The notification payload for the alarm.<br><br>
     *                     알람 알림 정보입니다.<br>
     */
    protected abstract fun createNotificationChannel(
        context: Context,
        notification: AlarmNotificationVO,
    )

    /**
     * Builds a notification option for the triggered alarm.<br><br>
     * 트리거된 알람의 알림 표시 옵션을 생성합니다.<br>
     * AlarmNotificationVO 기반으로 옵션을 구성하는 것을 권장합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트입니다.<br>
     * @param alarmVo The alarm data.<br><br>
     *                알람 데이터입니다.<br>
     * @return Notification option for SimpleNotificationController.<br><br>
     *         SimpleNotificationController에 전달할 옵션입니다.<br>
     */
    protected abstract fun buildNotificationOption(
        context: Context,
        alarmVo: AlarmVO,
    ): SimpleNotificationOptionBase

    /**
     * Resolves the notification display type.<br><br>
     * 알림 표시 타입을 결정합니다.<br>
     * SimpleNotificationController.showNotification에 전달됩니다.<br>
     *
     * @param alarmVo The alarm data.<br><br>
     *                알람 데이터입니다.<br>
     * @return Notification display type.<br><br>
     *         알림 표시 타입입니다.<br>
     */
    protected open fun resolveNotificationShowType(alarmVo: AlarmVO): SimpleNotificationType =
        SimpleNotificationType.BROADCAST

    /**
     * Loads all stored alarms from persistent storage.<br>
     * Used for re-registering alarms after device boot or time/timezone changes.<br><br>
     * 저장된 모든 알람을 불러옵니다.<br>
     * 부팅 또는 시간/타임존 변경 후 알람 재등록에 사용됩니다.<br>
     * 로컬 DB/파일/Preference 등 영속 저장소를 기반으로 구현합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트입니다.<br>
     * @return List of all stored alarms.<br><br>
     *         저장된 알람 목록입니다.<br>
     */
    protected abstract fun loadAllAlarmVoList(context: Context): List<AlarmVO>

    /**
     * Loads a specific alarm by its key from the intent extras.<br><br>
     * intent extras에서 특정 알람을 키로 불러옵니다.<br>
     * 단건 알람 로드를 담당합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트입니다.<br>
     * @param intent The received broadcast intent.<br><br>
     *               수신한 브로드캐스트 인텐트입니다.<br>
     * @param key The unique identifier for the alarm.<br><br>
     *            알람 고유 식별자입니다.<br>
     * @return The alarm data, or null if not found.<br><br>
     *         알람 데이터이며, 찾지 못하면 null입니다.<br>
     */
    protected abstract fun loadAlarmVoList(
        context: Context,
        intent: Intent,
        key: Int,
    ): AlarmVO?

    /**
     * The maximum time to hold the WakeLock (in milliseconds).<br>
     * Should be kept as short as possible to preserve battery life.<br><br>
     * WakeLock을 유지할 최대 시간(밀리초)입니다.<br>
     * 배터리 보호를 위해 가능한 짧게 유지해야 합니다.<br>
     * 내부적으로 WAKELOCK_TIMEOUT_MS와 비교하여 더 작은 값이 사용됩니다.<br>
     */
    protected abstract val powerManagerAcquireTime: Long

    /**
     * Handles alarm broadcasts with safe WakeLock management and proper error handling.<br><br>
     * WakeLock을 안전하게 관리하고 적절한 예외 처리를 통해 알람 브로드캐스트를 처리합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트입니다.<br>
     * @param intent The received broadcast intent.<br><br>
     *               수신한 브로드캐스트 인텐트입니다.<br>
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        Logx.d("BaseAlarmReceiver.onReceive called")

        // Early validation
        if (context == null) {
            Logx.e("Context is null, cannot process alarm")
            return
        }
        if (intent == null) {
            Logx.e("Intent is null, cannot process alarm")
            return
        }

        var wakeLock: PowerManager.WakeLock? = null

        try {
            // Acquire WakeLock with safe configuration
            val powerManager = context.getPowerManager()
            wakeLock = powerManager
                .newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, // Only CPU stays awake
                    WAKELOCK_TAG,
                ).apply {
                    // Use timeout as a safety net
                    acquire(minOf(powerManagerAcquireTime, WAKELOCK_TIMEOUT_MS))
                }

            Logx.d("WakeLock acquired for alarm processing")
            processAlarmIntent(context, intent)
        } catch (e: SecurityException) {
            Logx.e("Security exception acquiring WakeLock: ${e.message}")
            // Continue without WakeLock if permission is missing
            processAlarmIntent(context, intent)
        } catch (e: RuntimeException) {
            Logx.e("Unexpected error in alarm processing: ${e.message}")
        } finally {
            // Always release WakeLock in finally block
            wakeLock?.let { wl ->
                try {
                    if (wl.isHeld) {
                        wl.release()
                        Logx.d("WakeLock released successfully")
                    }
                } catch (e: RuntimeException) {
                    Logx.e("Error releasing WakeLock: ${e.message}")
                }
            }
        }
    }

    /**
     * Processes the alarm intent based on its action.<br><br>
     * 액션에 따라 알람 인텐트를 처리합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트입니다.<br>
     * @param intent The received broadcast intent.<br><br>
     *               수신한 브로드캐스트 인텐트입니다.<br>
     */
    private fun processAlarmIntent(context: Context, intent: Intent) {
        try {
            val alarmController = context.getAlarmController()

            Logx.d("Processing alarm intent with action: ${intent.action}")

            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED -> {
                    handleReschedule(context, alarmController, intent.action)
                }
                ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                    handleExactAlarmPermissionChanged(context, alarmController)
                }
                else -> {
                    handleAlarmTrigger(context, intent)
                }
            }
        } catch (e: RuntimeException) {
            Logx.e("Error processing alarm intent: ${e.message}")
        }
    }

    /**
     * Re-registers all active alarms after system events.<br><br>
     * 시스템 이벤트(부팅/시간/타임존 변경) 이후 활성 알람을 재등록합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트입니다.<br>
     * @param alarmController The alarm controller instance.<br><br>
     *                        알람 컨트롤러 인스턴스입니다.<br>
     * @param action The received broadcast action.<br><br>
     *               수신한 브로드캐스트 액션입니다.<br>
     */
    private fun handleReschedule(context: Context, alarmController: AlarmController, action: String?) {
        try {
            val allAlarms = loadAllAlarmVoList(context)
            Logx.d("Re-registering ${allAlarms.size} alarms due to action: $action")

            allAlarms.forEach { alarmVo ->
                if (alarmVo.isActive) {
                    registerAlarm(alarmController, alarmVo)
                }
            }
        } catch (e: RuntimeException) {
            Logx.e("Error re-registering alarms after $action: ${e.message}")
        }
    }

    /**
     * Handles exact alarm permission state change (Android 12+).<br><br>
     * 정확 알람 권한 상태 변경(Android 12+) 이벤트를 처리합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트입니다.<br>
     * @param alarmController The alarm controller instance.<br><br>
     *                        알람 컨트롤러 인스턴스입니다.<br>
     */
    private fun handleExactAlarmPermissionChanged(context: Context, alarmController: AlarmController) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        if (alarmController.canScheduleExactAlarms()) {
            handleReschedule(context, alarmController, ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)
        } else {
            onExactAlarmPermissionDenied(context)
        }
    }

    /**
     * Hook for handling exact alarm permission denial.<br><br>
     * 정확 알람 권한 거부 시 처리 훅입니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트입니다.<br>
     */
    protected open fun onExactAlarmPermissionDenied(context: Context) {
        Logx.w("Exact alarm permission denied. Consider showing 안내 UI.")
    }

    /**
     * Handles alarm trigger by showing notification.<br><br>
     * 알람 트리거를 처리하여 알림을 표시합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트입니다.<br>
     * @param intent The received broadcast intent.<br><br>
     *               수신한 브로드캐스트 인텐트입니다.<br>
     */
    private fun handleAlarmTrigger(context: Context, intent: Intent) {
        safeCatch {
            val key = intent.getIntExtra(ALARM_KEY, ALARM_KEY_DEFAULT_VALUE)

            if (key == ALARM_KEY_DEFAULT_VALUE) {
                Logx.e("Invalid alarm key received: $key")
                return
            }

            val alarmVo = loadAlarmVoList(context, intent, key)
            if (alarmVo != null) {
                createNotificationChannel(context, alarmVo.notification)
                if (!ensureNotificationControllerInitialized()) return
                val hasPostNotificationsPermission =
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                if (!hasPostNotificationsPermission) {
                    Logx.w("POST_NOTIFICATIONS permission is missing. Skip alarm notification for key: $key")
                    return
                }
                val option = buildNotificationOption(context, alarmVo)
                val shown = showAlarmNotification(option, alarmVo)
                if (shown) {
                    Logx.d("Alarm notification shown for key: $key")
                } else {
                    Logx.w("Failed to show alarm notification for key: $key")
                }
            } else {
                Logx.e("Failed to load alarmVo for key: $key")
            }
        }
    }

    /**
     * Ensures notificationController is initialized before use.<br><br>
     * notificationController가 사용 전에 초기화되었는지 확인합니다.<br>
     *
     * @return True if initialized, false otherwise.<br><br>
     *         초기화되었으면 true, 아니면 false입니다.<br>
     */
    private fun ensureNotificationControllerInitialized(): Boolean = if (::notificationController.isInitialized) {
        true
    } else {
        Logx.e("notificationController not initialized. Did you set it in createNotificationChannel()?")
        false
    }

    /**
     * Shows alarm notification after explicit runtime permission validation in caller.<br><br>
     * 호출부에서 런타임 권한 검증 후 알람 알림을 표시합니다.<br>
     */
    @SuppressLint("MissingPermission")
    private fun showAlarmNotification(option: SimpleNotificationOptionBase, alarmVo: AlarmVO): Boolean =
        notificationController.showNotification(option, resolveNotificationShowType(alarmVo))

    /**
     * Registers an alarm using the resolved registration type.<br>
     * Delegates to the appropriate AlarmController method based on resolveRegisterType.<br><br>
     * 결정된 등록 타입에 따라 알람을 등록합니다.<br>
     * resolveRegisterType에 맞는 AlarmController 메서드로 위임합니다.<br>
     *
     * @param alarmController The alarm controller instance.<br><br>
     *                        알람 컨트롤러 인스턴스입니다.<br>
     * @param alarmVo The alarm data to register.<br><br>
     *                등록할 알람 데이터입니다.<br>
     * @return `true` if registration succeeded, `false` otherwise.<br><br>
     *         등록 성공 시 true, 실패 시 false입니다.<br>
     */
    private fun registerAlarm(alarmController: AlarmController, alarmVo: AlarmVO): Boolean = when (resolveRegisterType(alarmVo)) {
        RegisterType.ALARM_AND_ALLOW_WHILE_IDLE ->
            alarmController.registerAlarmAndAllowWhileIdle(classType, alarmVo, resolveAlarmNamespace(alarmVo))

        RegisterType.ALARM_CLOCK ->
            alarmController.registerAlarmClock(classType, alarmVo, resolveAlarmNamespace(alarmVo))

        RegisterType.ALARM_EXACT_AND_ALLOW_WHILE_IDLE ->
            alarmController.registerAlarmExactAndAllowWhileIdle(classType, alarmVo, resolveAlarmNamespace(alarmVo))
    }

    /**
     * Enum defining the available alarm registration types.<br>
     * Each type corresponds to a different AlarmManager scheduling method.<br><br>
     * 사용할 알람 등록 타입을 정의합니다.<br>
     * 각 타입은 AlarmManager의 서로 다른 스케줄링 메서드에 대응합니다.<br>
     */
    public enum class RegisterType {
        /**
         * Standard alarm clock type that shows in status bar and wakes the device.<br><br>
         * 상태바에 표시되고 기기를 깨우는 표준 알람 시계 타입입니다.<br>
         */
        ALARM_CLOCK,

        /**
         * Alarm that can fire while device is idle (less precise).<br><br>
         * 유휴 상태에서도 동작하지만 정확도가 낮은 알람 타입입니다.<br>
         */
        ALARM_AND_ALLOW_WHILE_IDLE,

        /**
         * Exact alarm that can fire while device is idle (precise timing).<br><br>
         * 유휴 상태에서도 정확하게 동작하는 알람 타입입니다.<br>
         */
        ALARM_EXACT_AND_ALLOW_WHILE_IDLE,
    }
}
