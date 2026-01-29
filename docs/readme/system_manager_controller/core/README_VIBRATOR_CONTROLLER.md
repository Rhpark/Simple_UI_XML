# VibratorController vs Plain Android - Complete Comparison Guide
> **VibratorController vs 순수 Android - 완벽 비교 가이드**

## 📦 Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.controller.vibrator`

<br></br>

## 개요
진동 실행, 패턴/프리셋 진동, SDK 버전 분기를 단순화합니다.

<br></br>

## 🔎 At a Glance (한눈 비교)
| Item (항목) | Plain Android (기본 방식) | Simple UI (Simple UI) | Notes (비고) |
|---|---|---|---|
| SDK branching<br>SDK 분기 | Version-specific branching required<br>버전별 코드 분기 필수 | Automatic branching internally<br>내부에서 자동 분기 | Supports 7/8/12+<br>7/8/12+ 대응 |
| Deprecated API handling<br>Deprecated API | Handled by caller<br>호출부에서 직접 처리 | Handled internally<br>내부 처리 | Less boilerplate<br>코드 단순화 |
| Patterns / presets<br>패턴/프리셋 | Manual creation/call<br>직접 생성/호출 | `vibratePattern` / `createPredefined`<br>`vibratePattern`/`createPredefined` | Better usability<br>사용성 개선 |
| Permission<br>권한 | Handled by caller<br>호출부에서 직접 처리 | Same<br>동일 | `VIBRATE` permission required<br>`VIBRATE` 권한 필요 |

<br></br>

## 💡 Why It Matters (왜 중요한가)
**문제점:**
- SDK 버전별 분기 처리 필요
- `getSystemService()` 반복 호출 및 캐스팅
- Deprecated API 수동 처리

**장점:**
- 코드 간소화(복잡한 분기 제거, 단일 호출)
- SDK 버전 분기 자동 처리
- Deprecated API 내부 처리
<br></br>

## 순수 Android 방식 (Plain Android)
```kotlin
// Traditional SDK version branching (기존의 SDK 버전 분기 처리)
@RequiresPermission(Manifest.permission.VIBRATE)
private fun vibrate(milliseconds: Long) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ (API 31+) - Use VibratorManager (VibratorManager 사용)
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator

        val effect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
        vibratorManager.vibrate(CombinedVibration.createParallel(effect))

    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Android 8+ (API 26+) - Use VibrationEffect (VibrationEffect 사용)
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val effect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)

    } else {
        // Android 7 and below - Use Deprecated API (Android 7 이하 - Deprecated API 사용)
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        @Suppress("DEPRECATION")
        vibrator.vibrate(milliseconds)
    }
}

// Pattern vibration - Complex branching (패턴 진동 - 복잡한 분기)
@RequiresPermission(Manifest.permission.VIBRATE)
private fun vibratePattern(pattern: LongArray, repeat: Int = -1) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val effect = VibrationEffect.createWaveform(pattern, repeat)
        vibratorManager.vibrate(CombinedVibration.createParallel(effect))

    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val effect = VibrationEffect.createWaveform(pattern, repeat)
        vibrator.vibrate(effect)

    } else {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        @Suppress("DEPRECATION")
        vibrator.vibrate(pattern, repeat)
    }
}
```

<br></br>

## Simple UI 방식
```kotlin
// Simple vibration - One line (단순 진동 - 한 줄)
private fun vibrate(milliseconds: Long) {
    getVibratorController().vibrate(milliseconds) // Auto SDK handling! (SDK 자동 처리!)
}

// Pattern vibration - One line (패턴 진동 - 한 줄)
private fun vibratePattern(pattern: LongArray, repeat: Int = -1) {
    getVibratorController().vibratePattern(pattern, repeat) // Done! (끝!)
}

// Waveform vibration (Custom pattern) (웨이브폼 진동 (커스텀 패턴))
private fun vibrateWaveform() {
    val times = longArrayOf(0, 100, 50, 200, 50, 100)
    val amplitudes = intArrayOf(0, 128, 0, 255, 0, 128)
    getVibratorController().createWaveform(times, amplitudes, -1)
}

// System-defined vibration (시스템 정의 진동)
private fun vibrateClick() {
    getVibratorController().createPredefined(VibrationEffect.EFFECT_CLICK)
}

// Cancel vibration (진동 취소)
private fun cancelVibrate() {
    getVibratorController().cancel()
}
```

<br></br>

## 관련 확장 함수
- `getVibratorController()`  
  자세한 목록: [../xml/README_SYSTEM_MANAGER_EXTENSIONS.md](../xml/README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>
