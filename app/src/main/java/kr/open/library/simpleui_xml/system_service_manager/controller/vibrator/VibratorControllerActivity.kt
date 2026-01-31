package kr.open.library.simpleui_xml.system_service_manager.controller.vibrator

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.system_manager.extensions.getVibratorController
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import kr.open.library.simple_ui.xml.ui.components.activity.normal.BaseActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityVibratorControllerBinding

class VibratorControllerActivity : BaseActivity(R.layout.activity_vibrator_controller) {
    private lateinit var binding: ActivityVibratorControllerBinding

    private val vibratorController by lazy { getVibratorController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVibratorControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    @SuppressLint("MissingPermission")
    private fun initListener() {
        binding.run {
            btnVibrate.setOnClickListener {
                vibratorController.vibrate(1000)
                toastShowShort("Vibrate 1000ms")
            }

            btnPattern1.setOnClickListener {
                // Pattern: Short-Long-Short (100ms-500ms-100ms with 50ms gaps)
                val pattern = longArrayOf(0, 100, 50, 500, 50, 100)
                vibratorController.vibratePattern(pattern, -1)
                toastShowShort("Pattern 1: Short-Long-Short")
            }

            btnPattern2.setOnClickListener {
                // Pattern: SOS (... --- ...)
                val pattern =
                    longArrayOf(0, 100, 100, 100, 100, 100, 300, 300, 300, 300, 300, 300, 300, 100, 100, 100, 100, 100)
                vibratorController.vibratePattern(pattern, -1)
                toastShowShort("Pattern 2: SOS")
            }

            btnPattern3.setOnClickListener {
                // Pattern: Wave (increasing intensity)
                val times = longArrayOf(0, 100, 50, 200, 50, 300, 50, 200, 50, 100)
                val amplitudes = intArrayOf(0, 64, 0, 128, 0, 255, 0, 128, 0, 64)
                vibratorController.createWaveform(times, amplitudes, -1)
                toastShowShort("Pattern 3: Wave")
            }

            btnClickEffect.setOnClickListener {
                checkSdkVersion(Build.VERSION_CODES.Q,
                    positiveWork = {
                        vibratorController.createPredefined(VibrationEffect.EFFECT_CLICK)
                        toastShowShort("Click Effect")
                    },
                    negativeWork = { toastShowShort("Requires Android 10+") },
                )
            }

            btnDoubleClickEffect.setOnClickListener {
                checkSdkVersion(Build.VERSION_CODES.Q,
                    positiveWork = {
                        vibratorController.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                        toastShowShort("Double Click Effect")
                    },
                    negativeWork = { toastShowShort("Requires Android 10+") },
                )
            }

            btnHeavyClickEffect.setOnClickListener {
                checkSdkVersion(Build.VERSION_CODES.Q,
                    positiveWork = {
                        vibratorController.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                        toastShowShort("Heavy Click Effect")
                    },
                    negativeWork = { toastShowShort("Requires Android 10+") },
                )
            }

            btnCancel.setOnClickListener {
                vibratorController.cancel()
                toastShowShort("Vibration cancelled")
            }

            btnCheckHasVibrator.setOnClickListener {
                val hasVibrator = vibratorController.hasVibrator()
                toastShowShort("Has Vibrator: $hasVibrator")
            }

            btnCheckAmplitudeControl.setOnClickListener {
                val hasAmplitudeControl = vibratorController.hasAmplitudeControl()
                toastShowShort("강도 지원: $hasAmplitudeControl")
            }
        }
    }
}
