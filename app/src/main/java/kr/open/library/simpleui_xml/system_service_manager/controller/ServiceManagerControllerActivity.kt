package kr.open.library.simpleui_xml.system_service_manager.controller

import android.content.Intent
import android.os.Bundle
import kr.open.library.simple_ui.xml.ui.activity.BaseActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityServiceManagerControllerBinding
import kr.open.library.simpleui_xml.system_service_manager.controller.alarm.AlarmControllerActivity
import kr.open.library.simpleui_xml.system_service_manager.controller.floating.FloatingViewControllerActivity
import kr.open.library.simpleui_xml.system_service_manager.controller.notification.NotificationControllerActivity
import kr.open.library.simpleui_xml.system_service_manager.controller.softkeyboard.SoftKeyboardControllerActivity
import kr.open.library.simpleui_xml.system_service_manager.controller.vibrator.VibratorControllerActivity
import kr.open.library.simpleui_xml.system_service_manager.controller.wifi.WifiControllerActivity

class ServiceManagerControllerActivity : BaseActivity(R.layout.activity_service_manager_controller) {

    private lateinit var binding: ActivityServiceManagerControllerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceManagerControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        binding.run {
            btnSoftKeyboardController.setOnClickListener {
                startActivity(Intent(this@ServiceManagerControllerActivity, SoftKeyboardControllerActivity::class.java))
            }

            btnVibratorController.setOnClickListener {
                startActivity(Intent(this@ServiceManagerControllerActivity, VibratorControllerActivity::class.java))
            }

            btnAlarmController.setOnClickListener {
                startActivity(Intent(this@ServiceManagerControllerActivity, AlarmControllerActivity::class.java))
            }

            btnNotificationController.setOnClickListener {
                startActivity(Intent(this@ServiceManagerControllerActivity, NotificationControllerActivity::class.java))
            }

            btnWifiController.setOnClickListener {
                startActivity(Intent(this@ServiceManagerControllerActivity, WifiControllerActivity::class.java))
            }

            btnFloatingViewController.setOnClickListener {
                startActivity(Intent(this@ServiceManagerControllerActivity, FloatingViewControllerActivity::class.java))
            }
        }
    }
}
