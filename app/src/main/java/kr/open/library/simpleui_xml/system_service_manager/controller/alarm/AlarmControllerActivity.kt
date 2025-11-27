package kr.open.library.simpleui_xml.system_service_manager.controller.alarm

import android.annotation.SuppressLint
import android.os.Bundle
import kr.open.library.simple_ui.xml.extensions.view.toastShort
import kr.open.library.simple_ui.xml.ui.activity.BaseActivity
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVo
import kr.open.library.simple_ui.core.system_manager.extensions.getAlarmController
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityAlarmControllerBinding
import kr.open.library.simpleui_xml.system_service_manager.controller.receiver.AlarmReceiver
import java.util.Calendar

class AlarmControllerActivity : BaseActivity(R.layout.activity_alarm_controller) {

    private lateinit var binding: ActivityAlarmControllerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    @SuppressLint("MissingPermission")
    private fun initListener() {
        binding.run {
            btnRegisterAlarmClock.setOnClickListener {
                val hour = timePicker.hour
                val minute = timePicker.minute

                val alarmVo = AlarmVo(
                    key = 1,
                    title = "AlarmClock Title",
                    message = "AlarmClock Message",
                    soundUri = null,
                    hour = hour,
                    minute = minute,
                    second = 0
                )

                val result = getAlarmController().registerAlarmClock(AlarmReceiver::class.java, alarmVo)
                if (result) {
                    tvResult.text = "AlarmClock registered at $hour:$minute"
                    toastShort("AlarmClock registered")
                } else {
                    tvResult.text = "Failed to register AlarmClock"
                    toastShort("Failed to register")
                }
            }

            btnRegisterExactAlarm.setOnClickListener {
                val hour = timePicker.hour
                val minute = timePicker.minute

                val alarmVo = AlarmVo(
                    key = 2,
                    title = "Exact Alarm Title",
                    message = "Exact Alarm (Allow While Idle)",
                    soundUri = null,
                    hour = hour,
                    minute = minute,
                    second = 0
                )

                val result = getAlarmController().registerAlarmExactAndAllowWhileIdle(AlarmReceiver::class.java, alarmVo)
                if (result) {
                    tvResult.text = "Exact Alarm registered at $hour:$minute"
                    toastShort("Exact Alarm registered")
                } else {
                    tvResult.text = "Failed to register Exact Alarm"
                    toastShort("Failed to register")
                }
            }

            btnRegisterAllowWhileIdle.setOnClickListener {
                val hour = timePicker.hour
                val minute = timePicker.minute

                val alarmVo = AlarmVo(
                    key = 3,
                    title = "Allow While Idle Alarm",
                    message = "This alarm allows idle mode",
                    soundUri = null,
                    hour = hour,
                    minute = minute,
                    second = 0
                )

                val result = getAlarmController().registerAlarmAndAllowWhileIdle(AlarmReceiver::class.java, alarmVo)
                if (result) {
                    tvResult.text = "Allow While Idle Alarm registered at $hour:$minute"
                    toastShort("Alarm registered")
                } else {
                    tvResult.text = "Failed to register Alarm"
                    toastShort("Failed to register")
                }
            }

            btnRemoveAlarm.setOnClickListener {
                val key = edtAlarmKey.text.toString().toIntOrNull() ?: 1
                val result = getAlarmController().remove(key, AlarmReceiver::class.java)

                if (result) {
                    tvResult.text = "Alarm with key $key removed"
                    toastShort("Alarm removed")
                } else {
                    tvResult.text = "Failed to remove alarm with key $key"
                    toastShort("Failed to remove")
                }
            }

            btnCheckExists.setOnClickListener {
                val key = edtAlarmKey.text.toString().toIntOrNull() ?: 1
                val exists = getAlarmController().exists(key, AlarmReceiver::class.java)

                tvResult.text = "Alarm with key $key exists: $exists"
                toastShort("Exists: $exists")
            }
        }
    }
}
