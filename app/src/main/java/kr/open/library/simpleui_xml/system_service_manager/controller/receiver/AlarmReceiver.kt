package kr.open.library.simpleui_xml.system_service_manager.controller.receiver

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import kr.open.library.simple_ui.core.extensions.date.toLocalDateTime
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.controller.alarm.receiver.BaseAlarmReceiver
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmConstants
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVo
import kr.open.library.simple_ui.core.system_manager.controller.notification.SimpleNotificationType
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.DefaultNotificationOption
import kr.open.library.simple_ui.core.system_manager.extensions.getNotificationController
import kr.open.library.simpleui_xml.R

/**
 * AndroidManifest.xml
 * <receiver android:name=".system_service_manager.controller.receiver.AlarmReceiver"
 *     android:enabled="true"
 *     android:exported="true">
 *     <intent-filter>
 *         <action android:name="android.intent.action.BOOT_COMPLETED"/>
 *     </intent-filter>
 *
 * </receiver>
 */
public class AlarmReceiver : BaseAlarmReceiver() {
    //    override val registerType = RegisterType.ALARM_EXACT_AND_ALLOW_WHILE_IDLE
    override val registerType = RegisterType.ALARM_CLOCK

    override val classType: Class<*> = this::class.java

    override val powerManagerAcquireTime: Long get() = 5000L

    override fun loadAllalarmVoList(context: Context): List<AlarmVo> {
        // data load from realm or room or sharedpreference or other
        return emptyList<AlarmVo>()
    }

    override fun loadalarmVoList(
        context: Context,
        intent: Intent,
        key: Int,
    ): AlarmVo? {
        Logx.d("alarmKey is " + key)
        if (key == AlarmConstants.ALARM_KEY_DEFAULT_VALUE) {
            Logx.e("Error Alarm Key $key")
            return null
        }

        val date = (System.currentTimeMillis() + 30000).toLocalDateTime()

        return AlarmVo(
            key = 1,
            title = "Dump Title",
            message = "Dump Message",
            soundUri = null,
            hour = date.hour,
            minute = date.minute,
            second = date.second,
        )
        // data load from realm or room or  other
//        return AlarmSharedPreference(context).loadAlarm()
    }

    override fun createNotificationChannel(
        context: Context,
        alarmVo: AlarmVo,
    ) {
        Logx.d()
        notificationController =
            context
                .getNotificationController(
                    showType = SimpleNotificationType.BROADCAST,
                    notificationChannel = null,
                ).apply {
                    createChannel(
                        NotificationChannel("Alarm_ID", "Alarm_Name", NotificationManager.IMPORTANCE_HIGH).apply {
//            setShowBadge(true)
                            alarmVo.vibrationPattern?.let {
                                enableVibration(true)
                                vibrationPattern = it.toLongArray()
                            }
                            alarmVo.soundUri?.let {
                                setSound(
                                    it,
                                    AudioAttributes
                                        .Builder()
                                        .setUsage(AudioAttributes.USAGE_ALARM)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .build(),
                                )
                            }
                        },
                    )
                }
    }

    @SuppressLint("MissingPermission")
    override fun showNotification(
        context: Context,
        alarmVo: AlarmVo,
    ) {
        Logx.d()
        notificationController.showNotification(
            DefaultNotificationOption(
                notificationId = alarmVo.key,
                smallIcon = R.drawable.ic_launcher_foreground,
                title = alarmVo.title,
                content = alarmVo.message,
                isAutoCancel = false,
            ),
            showType = SimpleNotificationType.BROADCAST,
        )
    }
}
