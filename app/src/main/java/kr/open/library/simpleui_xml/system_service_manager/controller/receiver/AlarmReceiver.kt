package kr.open.library.simpleui_xml.system_service_manager.controller.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.controller.alarm.AlarmConstants
import kr.open.library.simple_ui.core.system_manager.controller.alarm.receiver.BaseAlarmReceiver
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmNotificationVO
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVO
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.DefaultNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.SimpleNotificationOptionBase
import kr.open.library.simple_ui.core.system_manager.extensions.getNotificationController
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.system_service_manager.controller.alarm.AlarmSampleStore

/**
 * AndroidManifest.xml 등록 예시
 * <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
 * <receiver android:name=".system_service_manager.controller.receiver.AlarmReceiver"
 *     android:enabled="true"
 *     android:exported="true">
 *     <intent-filter>
 *         <action android:name="android.intent.action.BOOT_COMPLETED"/>
 *         <action android:name="android.intent.action.TIME_CHANGED"/>
 *         <action android:name="android.intent.action.TIMEZONE_CHANGED"/>
 *         <action android:name="android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"/>
 *     </intent-filter>
 * </receiver>
 *
 * 부팅/시간/타임존/정확 알람 권한 변경 브로드캐스트를 수신해 재등록합니다.<br>
 */
public class AlarmReceiver : BaseAlarmReceiver() {
    override val classType: Class<*> = this::class.java

    override val powerManagerAcquireTime: Long get() = 5000L

    override fun loadAllAlarmVoList(context: Context): List<AlarmVO> = AlarmSampleStore.getAll()

    override fun loadAlarmVoList(
        context: Context,
        intent: Intent,
        key: Int,
    ): AlarmVO? {
        Logx.d("알람 키: $key")
        if (key == AlarmConstants.ALARM_KEY_DEFAULT_VALUE) {
            Logx.e("잘못된 알람 키입니다. key=$key")
            return null
        }

        val alarmVo = AlarmSampleStore.get(key)
        if (alarmVo == null) {
            Logx.w("저장소에 알람이 없습니다. key=$key")
        }

        return alarmVo
    }

    override fun createNotificationChannel(
        context: Context,
        notification: AlarmNotificationVO,
    ) {
        Logx.d()
        notificationController = context.getNotificationController(
            notificationChannel = NotificationChannel("Alarm_ID", "Alarm_Name", NotificationManager.IMPORTANCE_HIGH).apply {
//            setShowBadge(true)
                notification.vibrationPattern?.let {
                    enableVibration(true)
                    vibrationPattern = it.toLongArray()
                }
                notification.soundUri?.let {
                    setSound(
                        it,
                        AudioAttributes
                            .Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                }
            }
        )
    }

    override fun buildNotificationOption(
        context: Context,
        alarmVo: AlarmVO,
    ): SimpleNotificationOptionBase {
        Logx.d()
        return DefaultNotificationOption(
            notificationId = alarmVo.key,
            title = alarmVo.notification.title,
            content = alarmVo.notification.message,
            isAutoCancel = false,
            smallIcon = R.drawable.ic_launcher_foreground,
        )
    }
}
