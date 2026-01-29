# SoftKeyboardController vs Plain Android - Complete Comparison Guide
> **SoftKeyboardController vs 순수 Android - 완벽 비교 가이드**

## 📦 Module Information (모듈 정보)
- **Module**: `simple_xml` (UI-dependent module / UI 의존 모듈)
- **Package**: `kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard`

<br></br>

## 개요
키보드 표시/숨김/지연 처리와 SDK 버전 분기를 단순화합니다.

<br></br>

## 🔎 At a Glance (한눈 비교)
| Item (항목) | Plain Android (기본 방식) | Simple UI (Simple UI) | Notes (비고) |
|---|---|---|---|
| Service acquisition<br>서비스 획득 | Manual `getSystemService()`<br>`getSystemService()` 수동 | Simple call via extensions<br>확장 함수로 간단 호출 | Less boilerplate<br>코드 간소화 |
| Focus/Token handling<br>Focus/Token 처리 | Manual handling<br>수동 처리 | Handled internally<br>내부 처리 | Improved stability<br>안정성 개선 |
| Delayed show<br>지연 표시 | Manual implementation<br>수동 구현 | `showDelay()` provided<br>`showDelay()` 제공 | Job cancellation supported<br>Job 취소 지원 |
| SDK branching<br>SDK 분기 | Manual branching<br>직접 분기 | Internal branching<br>내부 분기 | Includes resize handling<br>Resize 처리 포함 |

<br></br>

## 💡 Why It Matters (왜 중요한가)
**문제점:**
- `getSystemService()` 반복 호출과 캐스팅
- Null 처리, Focus 처리 수동 반복
- 지연 표시 기능 수동 구현
- SDK 버전 분기 복잡

**장점:**
- 코드 간소화(한 줄 호출)
- Null/Focus 처리 자동
- 지연 표시 제공
- SDK 버전 분기 자동 처리
<br></br>

## 순수 Android 방식 (Plain Android)
```kotlin
// Traditional SoftKeyboard display method (기존의 SoftKeyboard 표시 방법)
private fun showKeyboard(editText: EditText) {
    // 1. Acquire InputMethodManager (InputMethodManager 획득)
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

    if (imm != null) {
        // 2. Handle Focus (Focus 처리)
        if (editText.requestFocus()) {
            // 3. Show keyboard (키보드 표시)
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        } else {
            Log.e("Keyboard", "Failed to request focus")
        }
    } else {
        Log.e("Keyboard", "InputMethodManager is null")
    }
}

// Delayed display - Separate implementation (지연 표시 - 별도 구현)
private fun showKeyboardWithDelay(editText: EditText, delayMillis: Long) {
    editText.postDelayed({
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (imm != null && editText.requestFocus()) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }, delayMillis)
}

// Window Input Mode setup - Adjust Pan (Window Input Mode 설정 - Adjust Pan)
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    // ...
}

// Window Input Mode setup - Adjust Resize (SDK version branching required)
// (Window Input Mode 설정 - Adjust Resize (SDK 버전 분기 필수))
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11+ (API 30+): ADJUST_RESIZE deprecated
        val controller = window.insetsController
        if (controller != null) {
            // Use WindowInsetsController (WindowInsetsController 사용)
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // Fallback: Use WindowCompat (Fallback: WindowCompat 사용)
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
    } else {
        // Android 10 and below: Traditional method (deprecated)
        // (Android 10 이하: 기존 방식 (deprecated))
        @Suppress("DEPRECATION")
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }
}
```

<br></br>

## Simple UI 방식
```kotlin
// Simple keyboard display - One line (간단한 키보드 표시 - 한 줄)
private fun showKeyboard(editText: EditText) {
    getSoftKeyboardController().show(editText) // Done! (끝!)
}

// Hide keyboard - Safe windowToken handling (키보드 숨김 - 안전한 windowToken 처리)
private fun hideKeyboard(editText: EditText) {
    getSoftKeyboardController().hide(editText) // Auto fallback to applicationWindowToken!
}

// Delayed display - Runnable version (지연 표시 - Runnable 버전)
private fun showKeyboardWithDelay(editText: EditText, delayMillis: Long) {
    getSoftKeyboardController().showDelay(editText, delayMillis) // Returns Boolean (Boolean 반환)
}

// ⭐ NEW: Delayed display with Job (cancellable)
private var showDelayJob: Job? = null

private fun showKeyboardWithJobControl(editText: EditText) {
    showDelayJob?.cancel()
    showDelayJob = getSoftKeyboardController().showDelay(editText, 300, coroutineScope = lifecycleScope)
}

// Window Input Mode setup (Window Input Mode 설정)
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    getSoftKeyboardController().setAdjustPan(window)
}

// Window Input Mode - Adjust Resize setup
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    getSoftKeyboardController().setAdjustResize(window) // SDK 버전 자동 분기
}
```

<br></br>

## 관련 확장 함수
- `getSoftKeyboardController()`  
  자세한 목록: [README_SYSTEM_MANAGER_EXTENSIONS.md](README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>
