# FloatingViewController vs Plain Android - Complete Comparison Guide
> **FloatingViewController vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_xml` (UI-dependent module / UI 의존 모듈)
- **Package**: `kr.open.library.simple_ui.xml.system_manager.controller.window`

<br></br>

## Overview (개요)
Provides simple APIs for floating view add/move/remove.  
> 플로팅 뷰 추가/이동/제거를 간단한 API로 제공합니다.

<br></br>

## At a Glance (한눈 비교)
| Item (항목)           | Plain Android (기본 방식)      | Simple UI (Simple UI) | Notes (비고) |
|---------------------|----------------------------|-----------------------|---|
| WindowManager setup | Manual LayoutParams config | Handled internally    | Includes SDK branching<br>SDK 분기 포함 |
| Touch/drag          | Manual implementation      | Provided internally   | Less implementation burden<br>구현 부담 감소 |
| Collision handling  | Manual implementation      | Provided internally   | Improved stability<br>안정성 향상 |
| Permission          | Handled by caller          | Same                  | `SYSTEM_ALERT_WINDOW` required<br>`SYSTEM_ALERT_WINDOW` 권한 필요 |

<br></br>

## Why It Matters (중요한 이유)
**Issues**
- Complex LayoutParams setup
- SDK version branching required
- Manual touch event and collision handling
> LayoutParams 설정이 복잡
> SDK 버전 분기 필요
> 터치 이벤트/충돌 처리 수동 구현

**Advantages**
- Internal handling of complex touch/collision logic
- Automated SDK branching and WindowManager setup
- Simplified code
> 복잡한 터치 처리/충돌 처리 내부 관리
> SDK 분기 및 WindowManager 설정 자동화
> 코드 간소화

<br></br>

## Plain Android (순수 Android 방식)
```kotlin
// Traditional Floating View addition (기존의 Floating View 추가)
@RequiresPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
private fun addFloatingView() {
    // 1. Acquire WindowManager (WindowManager 획득)
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

    // 2. LayoutParams setup - Complex options (LayoutParams 설정 - 복잡한 옵션)
    val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        },
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 100
        y = 100
    }

    // 3. Create View (View 생성)
    val floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null)

    // 4. Manually add Touch event - Very complex (Touch 이벤트 수동 추가 - 매우 복잡)
    var initialX = 0
    var initialY = 0
    var initialTouchX = 0f
    var initialTouchY = 0f
    var isDragging = false

    floatingView.setOnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                true
            }
            // ... Dozens of lines of Touch handling code (터치 처리 코드 다수)
        }
    }
}
```

<br></br>

## Simple UI Approach (Simple UI 방식)
```kotlin
// Simple Floating View addition - Few lines (간단한 Floating View 추가 - 몇 줄)
private fun addFloatingView() {
    val icon = ImageView(this).apply {
        setImageResource(R.drawable.ic_launcher_foreground)
    }

    val dragView = FloatingDragView(icon, 100, 100).apply {
        lifecycleScope.launch {
            sfCollisionStateFlow.collect { (touchType, collisionType) ->
                when (touchType) {
                    FloatingViewTouchType.TOUCH_DOWN -> { /* Handle (처리) */ }
                    FloatingViewTouchType.TOUCH_MOVE -> { /* Handle (처리) */ }
                    FloatingViewTouchType.TOUCH_UP -> { /* Handle (처리) */ }
                }
            }
        }
    }

    floatingViewController.addFloatingDragView(dragView)
}

// Fixed View setup (Fixed View 설정)
private fun setFixedView() {
    val icon = ImageView(this).apply { setBackgroundColor(Color.GREEN) }
    val fixedView = FloatingFixedView(icon, 200, 300)
    floatingViewController.setFloatingFixedView(fixedView)
}

// Remove all Views (모든 View 제거)
private fun removeAll() {
    floatingViewController.removeAllFloatingView()
}
```

<br></br>

## Return Contract (반환 계약)
- `Boolean` 반환은 **실제 WindowManager 반영 성공 여부**를 기준으로 합니다.
- 하위 `addView/removeView`가 실패하면 상위 API도 `false`를 반환합니다.
- 실패 시 내부 상태(list/reference)를 변경하지 않는 방향으로 동작합니다.
> `Boolean` 반환은 **실제 WindowManager 반영 성공 여부**를 의미합니다.
> 하위 `addView/removeView` 실패 시 상위 API도 `false`를 반환합니다.
> 실패 경로에서는 내부 상태(list/reference) 변경을 최소화합니다.

### `removeAllFloatingView()` 전략
- 현재 구현은 `first-failure-stop` 전략입니다.
- 중간 실패가 발생하면 즉시 `false`를 반환하며, 이미 제거된 항목은 제거된 상태로 유지됩니다.
- 즉, 일반 호출 경로에서는 부분 정리(partial cleanup) 상태가 남을 수 있습니다.
- `onDestroy()` 경로에서는 추가 정리 로직이 수행되어 종료 시점 일관성을 보강합니다.
> 현재 `removeAllFloatingView()`는 `first-failure-stop` 전략입니다.
> 중간 실패 시 즉시 `false`를 반환하며, 일반 호출에서는 부분 정리 상태가 남을 수 있습니다.

<br></br>

## Related Extensions (관련 확장 함수)
- `getFloatingViewController()`  
  See full list / 전체 목록: [README_SYSTEM_MANAGER_EXTENSIONS.md](../../README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>


