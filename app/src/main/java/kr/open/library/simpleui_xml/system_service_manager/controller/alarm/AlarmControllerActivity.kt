package kr.open.library.simpleui_xml.system_service_manager.controller.alarm

import android.Manifest.permission.SCHEDULE_EXACT_ALARM
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVO
import kr.open.library.simple_ui.core.system_manager.extensions.getAlarmController
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import kr.open.library.simple_ui.xml.ui.components.activity.normal.BaseActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityAlarmControllerBinding
import kr.open.library.simpleui_xml.system_service_manager.controller.receiver.AlarmReceiver

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
                checkSdkVersion(Build.VERSION_CODES.S,
                    positiveWork = {
                        requestPermissions(listOf(SCHEDULE_EXACT_ALARM)) {
                            if (it.isEmpty()) registerAlarmClock()
                            else toastShowShort("denied permissions $it")
                        }
                    },
                    negativeWork = { registerAlarmClock() }
                )
            }

            btnRegisterExactAlarm.setOnClickListener {
                checkSdkVersion(Build.VERSION_CODES.S,
                    positiveWork = {
                        requestPermissions(listOf(SCHEDULE_EXACT_ALARM)) {
                            if (it.isEmpty()) registerAlarmExactAndAllowWhileIdle()
                            else toastShowShort("denied permissions $it")
                        }
                    },
                    negativeWork = { registerAlarmExactAndAllowWhileIdle() }
                )
            }

            btnRegisterAllowWhileIdle.setOnClickListener {
                checkSdkVersion(Build.VERSION_CODES.S,
                    positiveWork = {
                        requestPermissions(listOf(SCHEDULE_EXACT_ALARM)) {
                            if (it.isEmpty()) registerAlarmAndAllowWhileIdle()
                            else toastShowShort("denied permissions $it")
                        }
                    },
                    negativeWork = { registerAlarmAndAllowWhileIdle() }
                )
            }

            btnRemoveAlarm.setOnClickListener {
                checkSdkVersion(Build.VERSION_CODES.S,
                    positiveWork = {
                        requestPermissions(listOf(SCHEDULE_EXACT_ALARM)) {
                            if (it.isEmpty()) remove()
                            else toastShowShort("denied permissions $it")
                        }
                    },
                    negativeWork = { remove() }
                )
            }

            btnCheckExists.setOnClickListener {
                checkSdkVersion(Build.VERSION_CODES.S,
                    positiveWork = {
                        requestPermissions(listOf(SCHEDULE_EXACT_ALARM)) {
                            if (it.isEmpty()) exists()
                            else toastShowShort("denied permissions $it")
                        }
                    },
                    negativeWork = { exists() }
                )
            }
        }
    }

    private fun registerAlarmClock() {
        binding.run {
            val hour = timePicker.hour
            val minute = timePicker.minute

            val alarmVo =
                AlarmVO(
                    key = 1,
                    title = "AlarmClock Title",
                    message = "AlarmClock Message",
                    soundUri = null,
                    hour = hour,
                    minute = minute,
                    second = 0,
                )

            val result = getAlarmController().registerAlarmClock(AlarmReceiver::class.java, alarmVo)
            if (result) {
                tvResult.text = "AlarmClock registered at $hour:$minute"
                toastShowShort("AlarmClock registered")
            } else {
                tvResult.text = "Failed to register AlarmClock"
                toastShowShort("Failed to register")
            }
        }
    }

    private fun registerAlarmExactAndAllowWhileIdle() {
        binding.run {
            val hour = timePicker.hour
            val minute = timePicker.minute

            val alarmVo =
                AlarmVO(
                    key = 2,
                    title = "Exact Alarm Title",
                    message = "Exact Alarm (Allow While Idle)",
                    soundUri = null,
                    hour = hour,
                    minute = minute,
                    second = 0,
                )

            val result = getAlarmController().registerAlarmExactAndAllowWhileIdle(AlarmReceiver::class.java, alarmVo)
            if (result) {
                tvResult.text = "Exact Alarm registered at $hour:$minute"
                toastShowShort("Exact Alarm registered")
            } else {
                tvResult.text = "Failed to register Exact Alarm"
                toastShowShort("Failed to register")
            }
        }
    }

    private fun registerAlarmAndAllowWhileIdle() {
        binding.run {
            val hour = timePicker.hour
            val minute = timePicker.minute

            val alarmVo =
                AlarmVO(
                    key = 3,
                    title = "Allow While Idle Alarm",
                    message = "This alarm allows idle mode",
                    soundUri = null,
                    hour = hour,
                    minute = minute,
                    second = 0,
                )

            val result = getAlarmController().registerAlarmAndAllowWhileIdle(AlarmReceiver::class.java, alarmVo)
            if (result) {
                tvResult.text = "Allow While Idle Alarm registered at $hour:$minute"
                toastShowShort("Alarm registered")
            } else {
                tvResult.text = "Failed to register Alarm"
                toastShowShort("Failed to register")
            }
        }
    }

    private fun remove() {
        binding.run {
            val key = edtAlarmKey.text.toString().toIntOrNull() ?: 1
            val result = getAlarmController().remove(key, AlarmReceiver::class.java)

            if (result) {
                tvResult.text = "Alarm with key $key removed"
                toastShowShort("Alarm removed")
            } else {
                tvResult.text = "Failed to remove alarm with key $key"
                toastShowShort("Failed to remove")
            }
        }
    }

    private fun exists() {
        binding.run {
            val key = edtAlarmKey.text.toString().toIntOrNull() ?: 1
            val exists = getAlarmController().exists(key, AlarmReceiver::class.java)

            tvResult.text = "Alarm with key $key exists: $exists"
            toastShowShort("Exists: $exists")
        }
    }
}
