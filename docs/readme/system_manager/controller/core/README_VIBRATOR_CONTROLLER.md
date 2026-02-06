# VibratorController vs Plain Android - Complete Comparison Guide
> **VibratorController vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.controller.vibrator` 

<br></br>

## Overview (개요)
Simplifies vibration execution, pattern/preset vibration, and SDK branching, with amplitude support checks.  
> 진동 실행, 패턴/프리셋 진동, SDK 버전 분기를 단순화하고 강도 지원 여부 확인까지 제공합니다.

<br></br>

## At a Glance (한눈 비교)
| Item (항목)               | Plain Android (기본 방식)               | Simple UI (Simple UI)                                                        | Notes (비고) |
|-------------------------|-------------------------------------|------------------------------------------------------------------------------|---|
| SDK branching           | Version-specific branching required | Automatic branching internally                                               | API 28+ / 29+ / 31+ 분기<br>API 28+ / 29+ / 31+ 대응 |
| Deprecated API handling | Handled by caller                   | Handled internally                                                           | Less boilerplate<br>코드 단순화 |
| Patterns / presets      | Manual creation/call                | `vibratePattern` / `createPredefined`<br>`vibratePattern`/`createPredefined` | Better usability<br>사용성 개선 |
| Amplitude support       | Caller must check                   | `hasAmplitudeControl()`                                                     | Device-dependent<br>기기 의존 |
| Predefined(Q+) safety   | Caller must guard                   | pre-Q returns false + logs                                                   | Safe fallback<br>안전 실패 |
| Permission              | Handled by caller                   | Same                                                                         | `VIBRATE` permission required<br>매니페스트 선언 필요(일반 권한) |

<br></br>

## Why It Matters (중요한 이유)
**Issues / 문제점**
- SDK version branching required
- Repeated `getSystemService()` calls and casting
- Manual handling of deprecated APIs
> SDK 버전별 분기 처리 필요
> `getSystemService()` 반복 호출 및 캐스팅
> Deprecated API 수동 처리

**Advantages / 장점:**
- Simplified code (remove complex branches, single call)
- Automatic SDK branching
- Deprecated API handling internally
> 코드 간소화(복잡한 분기 제거, 단일 호출)
> SDK 버전 분기 자동 처리
> Deprecated API 내부 처리

<br></br>

## Plain Android (순수 Android 방식)
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

## Simple UI Approach (Simple UI 방식)
```kotlin
// Simple vibration - One line (단순 진동 - 한 줄)
private fun vibrate(milliseconds: Long) {
    getVibratorController().vibrate(milliseconds) // Auto SDK handling (SDK 자동 처리)
}

// Pattern vibration - One line (패턴 진동 - 한 줄)
private fun vibratePattern(pattern: LongArray, repeat: Int = -1) {
    getVibratorController().vibratePattern(pattern, repeat) // One-line call (한 줄 호출)
}

// Waveform vibration (Custom pattern) (웨이브폼 진동 (커스텀 패턴))
private fun vibrateWaveform() {
    val times = longArrayOf(0, 100, 50, 200, 50, 100)
    val amplitudes = intArrayOf(0, 128, 0, 255, 0, 128)
    getVibratorController().createWaveform(times, amplitudes, -1)
}

// System-defined vibration (시스템 정의 진동)
private fun vibrateClick() {
    val ok = getVibratorController().createPredefined(VibrationEffect.EFFECT_CLICK)
    if (!ok) {
        // pre-Q: returns false with log
    }
}

// Cancel vibration (진동 취소)
private fun cancelVibrate() {
    getVibratorController().cancel()
}

// Vibrator hardware check (진동 하드웨어 지원 확인)
private fun checkVibratorSupport() {
    val hasVibrator = getVibratorController().hasVibrator()
}

// Amplitude support check (강도 지원 여부 확인)
private fun checkAmplitudeSupport() {
    val supported = getVibratorController().hasAmplitudeControl()
}
```

<br></br>

## Notes (주의사항)
- `createPredefined()` is available from Android Q(29). pre-Q returns false and logs a warning.  
  `createPredefined()`는 Android Q(29)+에서만 지원되며, pre-Q에서는 false 반환 + 경고 로그를 출력합니다.
- `repeat` >= 0 repeats until `cancel()` is called.  
  `repeat`가 0 이상이면 `cancel()` 호출 전까지 반복됩니다.
- `VIBRATE` is a normal permission: declare in AndroidManifest.xml (no runtime prompt).  
  `VIBRATE`는 일반 권한이므로 매니페스트 선언이 필요하고 런타임 요청은 없습니다.

<br></br>

## Related Extensions (관련 확장 함수)
- `getVibratorController()`  
  See full list / 전체 목록: [README_SYSTEM_MANAGER_EXTENSIONS.md](../../README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>


