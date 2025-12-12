package kr.open.library.simple_ui.core.system_manager.controller.vibrator

import android.Manifest.permission.VIBRATE
import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.throwMinSdkVersion
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getVibrator
import kr.open.library.simple_ui.core.system_manager.extensions.getVibratorManager

/**
 * Controller for managing device vibration operations with backward compatibility.<br>
 * Handles both legacy Vibrator API (SDK < 31) and modern VibratorManager API (SDK >= 31).<br><br>
 * 기기 진동 작업을 관리하는 컨트롤러로 하위 호환성을 제공합니다.<br>
 * 레거시 Vibrator API (SDK < 31)와 최신 VibratorManager API (SDK >= 31)를 모두 처리합니다.<br>
 *
 * Required manifest permission:<br>
 * `<uses-permission android:name="android.permission.VIBRATE"/>`<br><br>
 * 필수 매니페스트 권한:<br>
 * `<uses-permission android:name="android.permission.VIBRATE"/>`<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.
 */
public open class VibratorController(
    context: Context,
) : BaseSystemService(context, listOf(VIBRATE)) {
    /**
     * Legacy vibrator instance for SDK versions below 31 (Android 12).<br><br>
     * SDK 31 미만 (안드로이드 12 이하) 버전용 레거시 진동 인스턴스입니다.<br>
     */
    private val vibrator: Vibrator by lazy { context.getVibrator() }

    /**
     * Modern vibrator manager for SDK 31+ (Android 12+) with enhanced capabilities.<br><br>
     * SDK 31+ (안드로이드 12+) 버전용 향상된 기능을 제공하는 최신 진동 매니저입니다.<br>
     */

    private val vibratorManager: VibratorManager by lazy {
        checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = { context.getVibratorManager() },
            negativeWork = { throwMinSdkVersion(it) }
        )
    }

    /**
     * Creates a one-shot vibration with specified duration and amplitude.<br>
     * Supports different APIs based on SDK version for backward compatibility.<br><br>
     * 지정된 지속 시간과 강도로 단발성 진동을 생성합니다.<br>
     * 하위 호환성을 위해 SDK 버전에 따라 다른 API를 지원합니다.<br>
     *
     * @param timer Duration in milliseconds.<br><br>
     *              진동 지속 시간 (밀리초).
     *
     * @param effect Amplitude from -1 to 255, where -1 is default.<br><br>
     *               강도 -1~255, -1은 기본값.
     *
     * @return `true` if vibration was triggered successfully, `false` otherwise.<br><br>
     *         진동 실행 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(VIBRATE)
    public fun createOneShot(timer: Long, effect: Int = VibrationEffect.DEFAULT_AMPLITUDE): Boolean = tryCatchSystemManager(false) {
        checkSdkVersion(
            Build.VERSION_CODES.Q,
            positiveWork = {
                val oneShot = VibrationEffect.createOneShot(timer, effect)
                checkSdkVersion(
                    Build.VERSION_CODES.S,
                    positiveWork = { vibratorManager.vibrate(CombinedVibration.createParallel(oneShot)) },
                    negativeWork = { vibrator.vibrate(oneShot) },
                )
                return true
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timer)
                return true
            },
        )
    }

    /**
     * Simple vibration execution (duration only).<br><br>
     * 단순 진동 실행 (지속시간만 지정).<br>
     *
     * @param milliseconds Vibration duration in milliseconds.<br><br>
     *                     진동 지속 시간 (밀리초).
     *
     * @return `true` if vibration was triggered successfully, `false` otherwise.<br><br>
     *         진동 실행 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(VIBRATE)
    public fun vibrate(milliseconds: Long): Boolean = tryCatchSystemManager(false) {
        val effect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
        checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = { vibratorManager.vibrate(CombinedVibration.createParallel(effect)) },
            negativeWork = { vibrator.vibrate(effect) },
        )
        return true
    }

    /**
     * Creates a predefined vibration effect using system-defined patterns.<br>
     * Available effects: EFFECT_CLICK, EFFECT_DOUBLE_CLICK, EFFECT_TICK, etc.<br><br>
     * 시스템에서 정의된 패턴을 사용하여 미리 정의된 진동 효과를 생성합니다.<br>
     * 사용 가능한 효과: EFFECT_CLICK, EFFECT_DOUBLE_CLICK, EFFECT_TICK 등.<br>
     *
     * @param vibrationEffectClick Predefined effect constant (e.g., VibrationEffect.EFFECT_CLICK).<br><br>
     *                             미리 정의된 효과 상수 (예: VibrationEffect.EFFECT_CLICK).
     *
     * @return `true` if vibration was triggered successfully, `false` otherwise.<br><br>
     *         진동 실행 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(VIBRATE)
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun createPredefined(vibrationEffectClick: Int): Boolean = tryCatchSystemManager(false) {
        val predefinedEffect = VibrationEffect.createPredefined(vibrationEffectClick)
        checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = { vibratorManager.vibrate(CombinedVibration.createParallel(predefinedEffect)) },
            negativeWork = { vibrator.vibrate(predefinedEffect) },
        )
        return true
    }

    /**
     * Creates a complex waveform vibration pattern with custom timing and amplitudes.<br>
     * Supports different APIs based on SDK version for backward compatibility.<br><br>
     * 사용자 정의 타이밍과 강도로 복잡한 웨이브폼 진동 패턴을 생성합니다.<br>
     * 하위 호환성을 위해 SDK 버전에 따라 다른 API를 지원합니다.<br>
     *
     * @param times Timing values in milliseconds.<br><br>
     *              타이밍 값 (밀리초 단위).
     *
     * @param amplitudes Amplitude values (0-255) where 0 = motor off.<br><br>
     *                   강도 값 (0-255), 0은 모터 끔.
     *
     * @param repeat Index to start repeating from, -1 for no repeat.<br><br>
     *               반복 시작 인덱스, -1은 반복 없음.
     *
     * @return `true` if vibration was triggered successfully, `false` otherwise.<br><br>
     *         진동 실행 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(VIBRATE)
    public fun createWaveform(
        times: LongArray,
        amplitudes: IntArray,
        repeat: Int = -1,
    ): Boolean = tryCatchSystemManager(false) {
        val waveformEffect = VibrationEffect.createWaveform(times, amplitudes, repeat)
        checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = { vibratorManager.vibrate(CombinedVibration.createParallel(waveformEffect)) },
            negativeWork = { vibrator.vibrate(waveformEffect) },
        )
        return true
    }

    /**
     * Pattern vibration execution (timing array only).<br><br>
     * 패턴 진동 실행 (타이밍 배열만 사용).<br>
     *
     * @param pattern Vibration pattern array.<br><br>
     *                진동 패턴 배열.
     *
     * @param repeat Repeat start index, -1 for no repeat.<br><br>
     *               반복 시작 인덱스, -1은 반복 없음.
     *
     * @return `true` if vibration was triggered successfully, `false` otherwise.<br><br>
     *         진동 실행 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(VIBRATE)
    public fun vibratePattern(pattern: LongArray, repeat: Int = -1): Boolean = tryCatchSystemManager(false) {
        val waveformEffect = VibrationEffect.createWaveform(pattern, repeat)
        checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = { vibratorManager.vibrate(CombinedVibration.createParallel(waveformEffect)) },
            negativeWork = { vibrator.vibrate(waveformEffect) },
        )
        return true
    }

    /**
     * Cancels any ongoing vibration immediately.<br>
     * Works with both legacy Vibrator and modern VibratorManager APIs.<br><br>
     * 진행 중인 모든 진동을 즉시 취소합니다.<br>
     * 레거시 Vibrator와 최신 VibratorManager API 모두에서 작동합니다.<br>
     *
     * @return `true` if cancellation was successful, `false` otherwise.<br><br>
     *         취소 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(VIBRATE)
    public fun cancel(): Boolean = tryCatchSystemManager(false) {
        checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = { vibratorManager.cancel() },
            negativeWork = { vibrator.cancel() },
        )
        return true
    }

    /**
     * Check if vibration is supported on this device.<br><br>
     * 진동 지원 여부를 확인합니다.<br>
     *
     * @return `true` if device supports vibration, `false` otherwise.<br><br>
     *         기기가 진동을 지원하면 `true`, 그렇지 않으면 `false`.<br>
     */
    public fun hasVibrator(): Boolean = tryCatchSystemManager(false) {
        return checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = { vibratorManager.defaultVibrator.hasVibrator() },
            negativeWork = { vibrator.hasVibrator() },
        )
    }
}
