package kr.open.library.simple_ui.system_manager.controller.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.system_manager.controller.alarm.AlarmController
import kr.open.library.simple_ui.system_manager.controller.alarm.vo.AlarmConstants.ALARM_KEY
import kr.open.library.simple_ui.system_manager.controller.alarm.vo.AlarmConstants.ALARM_KEY_DEFAULT_VALUE
import kr.open.library.simple_ui.system_manager.controller.alarm.vo.AlarmConstants.WAKELOCK_TAG
import kr.open.library.simple_ui.system_manager.controller.alarm.vo.AlarmConstants.WAKELOCK_TIMEOUT_MS
import kr.open.library.simple_ui.system_manager.controller.alarm.vo.AlarmVo
import kr.open.library.simple_ui.system_manager.controller.notification.SimpleNotificationController
import kr.open.library.simple_ui.system_manager.extensions.getAlarmController
import kr.open.library.simple_ui.system_manager.extensions.getPowerManager


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

    protected lateinit var notificationController: SimpleNotificationController

    protected abstract val registerType: RegisterType
    protected abstract val classType: Class<*>

    protected abstract fun createNotificationChannel(context: Context, alarmVo: AlarmVo)

    protected abstract fun showNotification(context: Context, alarmVo: AlarmVo)

    protected abstract fun loadAllalarmVoList(context: Context): List<AlarmVo>
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

    public enum class RegisterType {
        ALARM_CLOCK,
        ALARM_AND_ALLOW_WHILE_IDLE,
        ALARM_EXACT_AND_ALLOW_WHILE_IDLE
    }
}
