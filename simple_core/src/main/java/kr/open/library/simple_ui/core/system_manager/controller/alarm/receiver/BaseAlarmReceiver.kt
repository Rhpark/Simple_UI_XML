package kr.open.library.simple_ui.core.system_manager.controller.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmController
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmConstants.ALARM_KEY
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmConstants.ALARM_KEY_DEFAULT_VALUE
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmConstants.WAKELOCK_TAG
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmConstants.WAKELOCK_TIMEOUT_MS
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVo
import kr.open.library.simple_ui.core.system_manager.controller.notification.SimpleNotificationController
import kr.open.library.simple_ui.core.system_manager.extensions.getAlarmController
import kr.open.library.simple_ui.core.system_manager.extensions.getPowerManager


/**
 * Base class for handling alarm broadcasts with safe WakeLock management.
 * Provides a template method pattern for alarm processing with proper resource management.
 * 
 * WakeLock을 안전하게 관리하며 알람 브로드캐스트를 처리하는 기본 클래스입니다.
 * 적절한 리소스 관리와 함께 알람 처리를 위한 템플릿 메서드 패턴을 제공합니다.
 *
 * @constructor Creates a BaseAlarmReceiver instance
 */
public abstract class BaseAlarmReceiver() : BroadcastReceiver() {

    /**
     * Controller for managing alarm notifications.<br>
     * Must be initialized by subclasses before use.<br><br>
     * 알람 알림 관리를 위한 컨트롤러입니다.<br>
     * 사용하기 전에 서브클래스에서 초기화해야 합니다.<br>
     */
    protected lateinit var notificationController: SimpleNotificationController

    /**
     * Type of alarm registration method to use.<br><br>
     * 사용할 알람 등록 방법의 타입입니다.<br>
     */
    protected abstract val registerType: RegisterType

    /**
     * Class type of the BroadcastReceiver for creating PendingIntents.<br><br>
     * PendingIntent 생성을 위한 BroadcastReceiver의 클래스 타입입니다.<br>
     */
    protected abstract val classType: Class<*>

    /**
     * Creates a notification channel for alarm notifications.<br>
     * Must be called before showing any notifications on Android O and above.<br><br>
     * 알람 알림을 위한 알림 채널을 생성합니다.<br>
     * Android O 이상에서 알림을 표시하기 전에 반드시 호출해야 합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트.
     * @param alarmVo The alarm data.<br><br>
     *                알람 데이터.
     */
    protected abstract fun createNotificationChannel(context: Context, alarmVo: AlarmVo)

    /**
     * Displays a notification for the triggered alarm.<br><br>
     * 트리거된 알람에 대한 알림을 표시합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트.
     * @param alarmVo The alarm data containing notification details.<br><br>
     *                알림 세부정보를 포함하는 알람 데이터.
     */
    protected abstract fun showNotification(context: Context, alarmVo: AlarmVo)

    /**
     * Loads all stored alarms from persistent storage.<br>
     * Used for re-registering alarms after device boot.<br><br>
     * 영구 저장소에서 저장된 모든 알람을 로드합니다.<br>
     * 기기 부팅 후 알람을 다시 등록하는 데 사용됩니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트.
     * @return List of all stored alarms.<br><br>
     *         저장된 모든 알람의 목록.<br>
     */
    protected abstract fun loadAllalarmVoList(context: Context): List<AlarmVo>

    /**
     * Loads a specific alarm by its key from the intent extras.<br><br>
     * 인텐트 extras에서 키로 특정 알람을 로드합니다.<br>
     *
     * @param context The application context.<br><br>
     *                애플리케이션 컨텍스트.
     * @param intent The received broadcast intent.<br><br>
     *               수신된 브로드캐스트 인텐트.
     * @param key The unique identifier for the alarm.<br><br>
     *            알람의 고유 식별자.
     * @return The alarm data, or null if not found.<br><br>
     *         알람 데이터, 찾을 수 없는 경우 null.<br>
     */
    protected abstract fun loadalarmVoList(context:Context, intent: Intent, key:Int): AlarmVo?

    /**
     * The maximum time to hold the WakeLock (in milliseconds).
     * Should be kept as short as possible to preserve battery life.
     * WakeLock을 유지하는 최대 시간(밀리초 단위).
     * 배터리 수명을 보존하기 위해 가능한 한 짧게 유지해야 합니다.
     */
    protected abstract val powerManagerAcquireTime: Long

    /**
     * Handles alarm broadcasts with safe WakeLock management and proper error handling.
     * WakeLock을 안전하게 관리하고 적절한 오류 처리로 알람 브로드캐스트를 처리합니다.
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
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,  // Only CPU stays awake
                WAKELOCK_TAG
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
        } catch (e: Exception) {
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
     * Processes the alarm intent based on its action.
     * 액션에 따라 알람 인텐트를 처리합니다.
     */
    private fun processAlarmIntent(context: Context, intent: Intent) {
        try {
            val alarmController = context.getAlarmController()
            
            Logx.d("Processing alarm intent with action: ${intent.action}")
            
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED -> {
                    handleBootCompleted(context, alarmController)
                }
                else -> {
                    handleAlarmTrigger(context, intent)
                }
            }
        } catch (e: Exception) {
            Logx.e("Error processing alarm intent: ${e.message}")
        }
    }
    
    /**
     * Handles device boot completion by re-registering all active alarms.
     * 기기 부팅 완료를 처리하여 모든 활성 알람을 다시 등록합니다.
     */
    private fun handleBootCompleted(context: Context, alarmController: AlarmController) {
        try {
            val allAlarms = loadAllalarmVoList(context)
            Logx.d("Re-registering ${allAlarms.size} alarms after boot")
            
            allAlarms.forEach { alarmVo ->
                if (alarmVo.isActive) {
                    registerAlarm(alarmController, alarmVo)
                }
            }
        } catch (e: Exception) {
            Logx.e("Error re-registering alarms after boot: ${e.message}")
        }
    }
    
    /**
     * Handles alarm trigger by showing notification.
     * 알람 트리거를 처리하여 알림을 표시합니다.
     */
    private fun handleAlarmTrigger(context: Context, intent: Intent) {
        try {
            val key = intent.getIntExtra(ALARM_KEY, ALARM_KEY_DEFAULT_VALUE)
            
            if (key == ALARM_KEY_DEFAULT_VALUE) {
                Logx.e("Invalid alarm key received: $key")
                return
            }
            
            val alarmVo = loadalarmVoList(context, intent, key)
            if (alarmVo != null) {
                createNotificationChannel(context, alarmVo)
                showNotification(context, alarmVo)
                Logx.d("Alarm notification shown for key: $key")
            } else {
                Logx.e("Failed to load alarmVo for key: $key")
            }
        } catch (e: Exception) {
            Logx.e("Error handling alarm trigger: ${e.message}")
        }
    }

    /**
     * Registers an alarm using the configured registration type.<br>
     * Delegates to the appropriate AlarmController method based on registerType.<br><br>
     * 구성된 등록 타입을 사용하여 알람을 등록합니다.<br>
     * registerType에 따라 적절한 AlarmController 메서드로 위임합니다.<br>
     *
     * @param alarmController The alarm controller instance.<br><br>
     *                        알람 컨트롤러 인스턴스.
     * @param alarmVo The alarm data to register.<br><br>
     *                등록할 알람 데이터.
     * @return `true` if registration succeeded, `false` otherwise.<br><br>
     *         등록 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    private fun registerAlarm(alarmController: AlarmController, alarmVo: AlarmVo): Boolean =
        when (registerType) {
            RegisterType.ALARM_AND_ALLOW_WHILE_IDLE -> {
                alarmController.registerAlarmAndAllowWhileIdle(classType, alarmVo)
            }

            RegisterType.ALARM_CLOCK -> {
                alarmController.registerAlarmClock(classType, alarmVo)
            }

            RegisterType.ALARM_EXACT_AND_ALLOW_WHILE_IDLE -> {
                alarmController.registerAlarmExactAndAllowWhileIdle(classType, alarmVo)
            }
        }

    /**
     * Enum defining the available alarm registration types.<br>
     * Each type corresponds to a different AlarmManager scheduling method.<br><br>
     * 사용 가능한 알람 등록 타입을 정의하는 열거형입니다.<br>
     * 각 타입은 다른 AlarmManager 스케줄링 방법에 해당합니다.<br>
     */
    public enum class RegisterType {
        /**
         * Standard alarm clock type that shows in status bar and wakes the device.<br><br>
         * 상태 표시줄에 표시되고 기기를 깨우는 표준 알람 시계 타입입니다.<br>
         */
        ALARM_CLOCK,

        /**
         * Alarm that can fire while device is idle (less precise).<br><br>
         * 기기가 유휴 상태일 때도 실행될 수 있는 알람(덜 정밀함)입니다.<br>
         */
        ALARM_AND_ALLOW_WHILE_IDLE,

        /**
         * Exact alarm that can fire while device is idle (precise timing).<br><br>
         * 기기가 유휴 상태일 때도 실행될 수 있는 정확한 알람(정밀한 타이밍)입니다.<br>
         */
        ALARM_EXACT_AND_ALLOW_WHILE_IDLE
    }
}
