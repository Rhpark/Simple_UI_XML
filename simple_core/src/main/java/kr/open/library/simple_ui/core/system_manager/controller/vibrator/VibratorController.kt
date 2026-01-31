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
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.extensions.trycatch.throwMinSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getVibrator
import kr.open.library.simple_ui.core.system_manager.extensions.getVibratorManager

/**
 * Controller for managing device vibration operations with backward compatibility.<br>
 * Handles both legacy Vibrator API (SDK < 31) and modern VibratorManager API (SDK >= 31).<br><br>
 * 기기 진동 작업을 관리하는 컨트롤러로 하위 호환성을 제공합니다.<br>
 * 레거시 Vibrator API (SDK < 31)와 최신 VibratorManager API (SDK >= 31)를 모두 처리합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android vibration APIs changed significantly in SDK 31 (VibratorManager introduction).<br>
 * - Direct usage of system vibration APIs requires extensive SDK version checking.<br>
 * - Permission handling and error management need to be consistent across all methods.<br><br>
 * - Android 진동 API가 SDK 31에서 크게 변경되었습니다 (VibratorManager 도입).<br>
 * - 시스템 진동 API 직접 사용 시 광범위한 SDK 버전 체크가 필요합니다.<br>
 * - 모든 메서드에서 일관된 권한 처리와 에러 관리가 필요합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - **Automatic SDK branching**: Internally handles SDK version differences, exposing unified API.<br>
 * - **Input validation**: All methods validate parameters before system calls to prevent exceptions.<br>
 * - **Boolean return pattern**: Simple true/false return instead of exceptions for easier usage.<br><br>
 * - **자동 SDK 분기**: 내부적으로 SDK 버전 차이를 처리하여 통일된 API를 제공합니다.<br>
 * - **입력 검증**: 모든 메서드가 시스템 호출 전 파라미터를 검증하여 예외를 방지합니다.<br>
 * - **Boolean 반환 패턴**: 예외 대신 간단한 true/false 반환으로 사용성을 높였습니다.<br>
 *
 * **Important notes / 주의사항:**<br>
 * - Requires VIBRATE permission in AndroidManifest.xml.<br>
 * - Amplitude control may be ignored on devices without hardware support.<br>
 * - SDK O(26) or higher required for amplitude-based vibration effects.<br><br>
 * - AndroidManifest.xml에 VIBRATE 권한이 필요합니다.<br>
 * - 하드웨어 지원이 없는 기기에서는 강도 조절이 무시될 수 있습니다.<br>
 * - 강도 기반 진동 효과는 SDK O(26) 이상이 필요합니다.<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Get controller instance: `context.getVibratorController()`<br>
 * 2. Check vibration support: `controller.hasVibrator()`<br>
 * 3. Trigger vibration: `controller.vibrate(200L)` or `controller.createOneShot(200L, 128)`<br>
 * 4. Cancel if needed: `controller.cancel()`<br><br>
 * 1. 컨트롤러 인스턴스 획득: `context.getVibratorController()`<br>
 * 2. 진동 지원 확인: `controller.hasVibrator()`<br>
 * 3. 진동 실행: `controller.vibrate(200L)` 또는 `controller.createOneShot(200L, 128)`<br>
 * 4. 필요시 취소: `controller.cancel()`<br>
 *
 * Required manifest permission:<br>
 * `<uses-permission android:name="android.permission.VIBRATE"/>`<br><br>
 * 필수 매니페스트 권한:<br>
 * `<uses-permission android:name="android.permission.VIBRATE"/>`<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.<br>
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
        checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = { context.getVibratorManager() },
            negativeWork = { throwMinSdkVersion(it) }
        )
    }

    /**
     * Creates a one-shot vibration with specified duration and amplitude.<br>
     * Notes.<br>
     * - `amplitude` is an amplitude value (DEFAULT_AMPLITUDE(-1) or 1..255).<br>
     * - Available since SDK O (26) (this library minSdk=28).<br>
     * - Devices without amplitude control may ignore the requested amplitude.<br><br>
     *
     * 지정된 지속 시간과 강도로 단발성 진동을 생성합니다.<br>
     * 참고.<br>
     * - `amplitude`는 강도(amplitude) 값이며 DEFAULT_AMPLITUDE(-1) 또는 1..255를 사용합니다.<br>
     * - SDK O(26)부터 지원되며, 본 라이브러리 minSdk=28에서는 런타임에서 항상 사용 가능합니다.<br>
     * - 기기가 amplitude control을 지원하지 않으면 지정한 강도가 무시될 수 있습니다.<br>
     *
     * @param timer Duration in milliseconds.<br><br>
     *              진동 지속 시간 (밀리초).
     *
     * @param amplitude Amplitude value (DEFAULT_AMPLITUDE(-1) or 1..255).<br><br>
     *               강도 값 (DEFAULT_AMPLITUDE(-1) 또는 1..255).
     *
     * @return `true` if vibration was triggered successfully, `false` otherwise.<br><br>
     *         진동 실행 성공 시 `true`, 그렇지 않으면 `false`.
     */
    @RequiresPermission(VIBRATE)
    public fun createOneShot(timer: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE): Boolean = tryCatchSystemManager(false) {
        if (timer <= 0L) {
            Logx.e("timer > 0L, timer $timer")
            return false
        }
        if (amplitude != VibrationEffect.DEFAULT_AMPLITUDE && amplitude !in 1..255) {
            Logx.e("amplitude must be 1..255 or DEFAULT_AMPLITUDE(-1). amplitude=$amplitude")
            return false
        }

        val oneShot = VibrationEffect.createOneShot(timer, amplitude)
        checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = { vibratorManager.vibrate(CombinedVibration.createParallel(oneShot)) },
            negativeWork = { vibrator.vibrate(oneShot) },
        )
        return true
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
    public fun vibrate(milliseconds: Long): Boolean {
        if (milliseconds <= 0L) {
            Logx.e("milliseconds > 0L, milliseconds $milliseconds")
            return false
        }

        return createOneShot(milliseconds)
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
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = {
                val predefinedEffect = VibrationEffect.createPredefined(vibrationEffectClick)
                checkSdkVersion(Build.VERSION_CODES.S,
                    positiveWork = { vibratorManager.vibrate(CombinedVibration.createParallel(predefinedEffect)) },
                    negativeWork = { vibrator.vibrate(predefinedEffect) },
                )
                true
            },
            negativeWork = {
                Logx.e("createPredefined는 Android Q(29) 이상에서만 지원됩니다. currentSdk=${Build.VERSION.SDK_INT}")
                false
            },
        )
    }

    /**
     * Creates a waveform vibration pattern with custom timings and amplitudes.<br>
     * Notes.<br>
     * - `times` and `amplitudes` must be non-empty and have the same length.<br>
     * - Every value in `times` must be >= 0.<br>
     * - `amplitudes` supports DEFAULT_AMPLITUDE(-1) or 0..255 (0 = motor off for that segment).<br>
     * - `repeat` must be -1 or a valid index; when >= 0, it repeats until `cancel()` is called.<br>
     * - Amplitude control may be ignored on devices without hardware support.<br><br>
     *
     * 사용자 정의 타이밍과 강도로 웨이브폼 진동을 생성합니다.<br>
     * 참고.<br>
     * - `times`와 `amplitudes`는 비어있으면 안 되며, 길이가 동일해야 합니다.<br>
     * - `times`의 모든 값은 0 이상이어야 합니다.<br>
     * - `amplitudes`는 DEFAULT_AMPLITUDE(-1) 또는 0..255를 허용합니다(0은 해당 구간 진동 끔).<br>
     * - `repeat`는 -1 또는 유효한 인덱스여야 하며, 0 이상이면 `cancel()` 호출 전까지 반복됩니다.<br>
     * - 기기가 amplitude control을 지원하지 않으면 강도 값이 무시될 수 있습니다.<br>
     *
     * @param times Timing values in milliseconds.<br><br>
     *              타이밍 값 (밀리초 단위).
     *
     * @param amplitudes Amplitude values (DEFAULT_AMPLITUDE(-1) or 0..255).<br><br>
     *                   강도 값 (DEFAULT_AMPLITUDE(-1) 또는 0..255).
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
        // 배열 검증 추가
        if (times.isEmpty() || amplitudes.isEmpty()) {
            Logx.e("times or amplitudes array is empty, times: ${times.size}, amplitudes: ${amplitudes.size}")
            return false
        }
        if (times.size != amplitudes.size) {
            Logx.e("times and amplitudes array size must be same, times: ${times.size}, amplitudes: ${amplitudes.size}")
            return false
        }
        if (times.any { it < 0L }) {
            Logx.e("times must be >= 0. times=${times.contentToString()}")
            return false
        }
        if (amplitudes.any { it != VibrationEffect.DEFAULT_AMPLITUDE && it !in 0..255 }) {
            Logx.e("amplitudes must be 0..255 or DEFAULT_AMPLITUDE(-1). amplitudes=${amplitudes.contentToString()}")
            return false
        }
        if (repeat != -1 && repeat !in times.indices) {
            Logx.e("repeat must be between 0 and ${times.size - 1}, repeat: $repeat")
            return false
        }

        val waveformEffect = VibrationEffect.createWaveform(times, amplitudes, repeat)
        checkSdkVersion(Build.VERSION_CODES.S,
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
        // 배열 검증 추가
        if (pattern.isEmpty()) {
            Logx.e("vibratePattern: pattern is empty.")
            return false
        }
        if (pattern.any { it < 0L }) {
            Logx.e("vibratePattern: pattern must be >= 0. pattern=${pattern.contentToString()}")
            return false
        }
        if (repeat != -1 && repeat !in pattern.indices) {
            Logx.e("vibratePattern: repeat must be -1 or within 0..${pattern.size - 1}. repeat=$repeat")
            return false
        }

        val waveformEffect = VibrationEffect.createWaveform(pattern, repeat)
        checkSdkVersion(Build.VERSION_CODES.S,
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
        checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = { vibratorManager.cancel() },
            negativeWork = { vibrator.cancel() },
        )
        return true
    }

    /**
     * Checks if the device supports amplitude control for vibration.<br>
     * When not supported, amplitude values passed to vibration methods will be ignored
     * and the device will vibrate at its default intensity.<br><br>
     * 기기가 진동 강도 조절을 지원하는지 확인합니다.<br>
     * 지원하지 않는 경우, 진동 메서드에 전달된 강도 값은 무시되고
     * 기기의 기본 강도로 진동합니다.<br>
     *
     * @return true if amplitude control is supported, false otherwise.<br><br>
     *         강도 조절이 지원되면 true, 그렇지 않으면 false.<br>
     */
    public fun hasAmplitudeControl(): Boolean = safeCatch(false) {
        return checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = { vibratorManager.defaultVibrator.hasAmplitudeControl() },
            negativeWork = { vibrator.hasAmplitudeControl() },
        )
    }

    /**
     * Check if vibration is supported on this device.<br><br>
     * 진동 지원 여부를 확인합니다.<br>
     *
     * @return `true` if device supports vibration, `false` otherwise.<br><br>
     *         기기가 진동을 지원하면 `true`, 그렇지 않으면 `false`.<br>
     */
    public fun hasVibrator(): Boolean = safeCatch(false) {
        return checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = { vibratorManager.defaultVibrator.hasVibrator() },
            negativeWork = { vibrator.hasVibrator() },
        )
    }
}
