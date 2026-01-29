# FloatingViewController vs Plain Android - Complete Comparison Guide
> **FloatingViewController vs 순수 Android - 완벽 비교 가이드**

## 📦 Module Information (모듈 정보)
- **Module**: `simple_xml` (UI-dependent module / UI 의존 모듈)
- **Package**: `kr.open.library.simple_ui.xml.system_manager.controller.window`

<br></br>

## 개요
플로팅 뷰 추가/이동/제거를 간단한 API로 제공합니다.

<br></br>

## 🔎 At a Glance (한눈 비교)
| Item (항목) | Plain Android (기본 방식) | Simple UI (Simple UI) | Notes (비고) |
|---|---|---|---|
| WindowManager setup<br>WindowManager 설정 | Manual LayoutParams config<br>LayoutParams 직접 구성 | Handled internally<br>내부 처리 | Includes SDK branching<br>SDK 분기 포함 |
| Touch/drag<br>터치/드래그 | Manual implementation<br>수동 구현 | Provided internally<br>내부 제공 | Less implementation burden<br>구현 부담 감소 |
| Collision handling<br>충돌 처리 | Manual implementation<br>직접 구현 | Provided internally<br>내부 제공 | Improved stability<br>안정성 향상 |
| Permission<br>권한 | Handled by caller<br>호출부에서 처리 | Same<br>동일 | `SYSTEM_ALERT_WINDOW` |

<br></br>

## 💡 Why It Matters (왜 중요한가)
**문제점:**
- LayoutParams 설정이 복잡
- SDK 버전 분기 필요
- 터치 이벤트/충돌 처리 수동 구현

**장점:**
- 복잡한 터치 처리/충돌 처리 내부 관리
- SDK 분기 및 WindowManager 설정 자동화
- 코드 간소화
<br></br>

## 순수 Android 방식 (Plain Android)
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
            // ... Dozens of lines of Touch handling code
        }
    }
}
```

<br></br>

## Simple UI 방식
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
                    FloatingViewTouchType.TOUCH_DOWN -> { /* Handle */ }
                    FloatingViewTouchType.TOUCH_MOVE -> { /* Handle */ }
                    FloatingViewTouchType.TOUCH_UP -> { /* Handle */ }
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

## 관련 확장 함수
- `getFloatingViewController()`  
  자세한 목록: [README_SYSTEM_MANAGER_EXTENSIONS.md](README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>
