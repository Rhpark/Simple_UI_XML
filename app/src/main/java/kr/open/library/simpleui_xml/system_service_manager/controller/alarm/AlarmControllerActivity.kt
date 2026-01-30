package kr.open.library.simpleui_xml.system_service_manager.controller.alarm

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmIdleMode
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmNotificationVO
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmScheduleVO
import kr.open.library.simple_ui.core.system_manager.controller.alarm.vo.AlarmVO
import kr.open.library.simple_ui.core.system_manager.extensions.getAlarmController
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import kr.open.library.simple_ui.xml.ui.components.activity.normal.BaseActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityAlarmControllerBinding
import kr.open.library.simpleui_xml.system_service_manager.controller.receiver.AlarmReceiver
import java.util.Locale

class AlarmControllerActivity : BaseActivity(R.layout.activity_alarm_controller) {
    private lateinit var binding: ActivityAlarmControllerBinding
    private val alarmController by lazy { getAlarmController() }

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
                runExactAlarmPermissionOrOpenSettings { registerAlarmClock() }
            }

            btnRegisterExactAlarm.setOnClickListener {
                runExactAlarmPermissionOrOpenSettings { registerAlarmExactAndAllowWhileIdle() }
            }

            btnRegisterAllowWhileIdle.setOnClickListener {
                registerAlarmAndAllowWhileIdle()
            }

            btnRemoveAlarm.setOnClickListener {
                remove()
            }

            btnCheckExists.setOnClickListener {
                exists()
            }
        }
    }

    private fun registerAlarmClock() {
        val key = getAlarmKeyOrNull() ?: return
        val alarmVo = buildAlarmVo(
            key = key,
            idleMode = AlarmIdleMode.NONE,
            title = "알람 시계",
            message = "알람 시계 등록 테스트",
        )

        registerOrUpdate(
            key = key,
            alarmVo = alarmVo,
            updateAction = { alarmController.updateAlarmClock(AlarmReceiver::class.java, alarmVo) },
            successLabel = "알람 시계",
        )
    }

    private fun registerAlarmExactAndAllowWhileIdle() {
        val key = getAlarmKeyOrNull() ?: return
        val alarmVo = buildAlarmVo(
            key = key,
            idleMode = AlarmIdleMode.EXACT,
            title = "정확 알람",
            message = "정확 알람(유휴 허용) 테스트",
        )

        registerOrUpdate(
            key = key,
            alarmVo = alarmVo,
            updateAction = { alarmController.updateExactAndAllowWhileIdle(AlarmReceiver::class.java, alarmVo) },
            successLabel = "정확 알람",
        )
    }

    private fun registerAlarmAndAllowWhileIdle() {
        val key = getAlarmKeyOrNull() ?: return
        val alarmVo = buildAlarmVo(
            key = key,
            idleMode = AlarmIdleMode.INEXACT,
            title = "유휴 허용 알람",
            message = "유휴 허용(부정확) 테스트",
        )

        registerOrUpdate(
            key = key,
            alarmVo = alarmVo,
            updateAction = { alarmController.updateAllowWhileIdle(AlarmReceiver::class.java, alarmVo) },
            successLabel = "유휴 허용 알람",
        )
    }

    private fun remove() {
        val key = getAlarmKeyOrNull() ?: return
        val removed = alarmController.remove(key, AlarmReceiver::class.java)
        val storeRemoved = AlarmSampleStore.remove(key)

        showResult(
            title = "알람 삭제",
            detail = "컨트롤러 삭제=$removed, 저장소 삭제=$storeRemoved, key=$key",
        )
    }

    private fun exists() {
        val key = getAlarmKeyOrNull() ?: return
        val controllerExists = alarmController.exists(key, AlarmReceiver::class.java)
        val storeExists = AlarmSampleStore.exists(key)

        showResult(
            title = "알람 존재 확인",
            detail = "컨트롤러=$controllerExists, 저장소=$storeExists, key=$key",
        )
    }

    private fun runExactAlarmPermissionOrOpenSettings(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            onGranted()
            return
        }

        if (alarmController.canScheduleExactAlarms()) {
            onGranted()
            return
        }

        val intent = alarmController.buildExactAlarmPermissionIntent()
        if (intent != null) {
            toastShowShort("정확 알람 권한 설정 화면으로 이동합니다.")
            startActivity(intent)
        } else {
            toastShowShort("정확 알람 권한이 필요합니다.")
        }
    }

    private fun registerOrUpdate(
        key: Int,
        alarmVo: AlarmVO,
        updateAction: () -> Boolean,
        successLabel: String,
    ) {
        val wasStored = AlarmSampleStore.exists(key)
        val result = updateAction()

        if (result) {
            AlarmSampleStore.put(alarmVo)
        } else if (wasStored) {
            AlarmSampleStore.remove(key)
        }

        val actionLabel = if (wasStored) "갱신" else "등록"
        val timeText = formatTime(alarmVo.schedule.hour, alarmVo.schedule.minute, alarmVo.schedule.second)
        showResult(
            title = "$successLabel $actionLabel ${if (result) "성공" else "실패"}",
            detail = "key=$key, 시간=$timeText, 모드=${alarmVo.schedule.idleMode}",
        )
    }

    private fun buildAlarmVo(
        key: Int,
        idleMode: AlarmIdleMode,
        title: String,
        message: String,
    ): AlarmVO {
        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute

        return AlarmVO(
            key = key,
            schedule = AlarmScheduleVO(
                hour = hour,
                minute = minute,
                second = 0,
                idleMode = idleMode,
            ),
            notification = AlarmNotificationVO(
                title = title,
                message = message,
                soundUri = null,
            ),
        )
    }

    private fun getAlarmKeyOrNull(): Int? {
        val keyText = binding.edtAlarmKey.text
            .toString()
            .trim()
        val key = keyText.toIntOrNull()
        return if (key == null || key <= 0) {
            toastShowShort("알람 키는 1 이상의 숫자여야 합니다.")
            null
        } else {
            key
        }
    }

    private fun formatTime(hour: Int, minute: Int, second: Int): String =
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second)

    private fun showResult(title: String, detail: String) {
        binding.tvResult.text = "$title\n$detail"
    }
}
