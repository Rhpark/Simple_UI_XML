# System Service Manager Controller vs Plain Android - Complete Comparison Guide (System Service Manager Controller vs 순수 Android - 완벽 비교 가이드)

**"Simplify complex System Services into a single line!"** See the immediate difference Simple UI Controller makes compared to traditional Android System Service usage.
> **"복잡한 System Service를 한 줄로 끝내자!"** 기존 Android System Service 사용 대비 Simple UI Controller가 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 At a Glance (한눈 비교)

| Category                                  |                    Plain Android                    |                Simple UI Controller                 |                   Impact\                    |
|:------------------------------------------|:---------------------------------------------------:|:---------------------------------------------------:|:--------------------------------------------:|
| **SoftKeyboard Control**                  |   `getSystemService()`<br>`showSoftInput()` calls   |        `getSoftKeyboardController().show()`         |        **Dozens of lines → One line**        |
| **Vibrator Control**                      |           SDK version branching + 3 APIs            |         `getVibratorController().vibrate()`         |            **Auto SDK handling**             |
| **Alarm Registration**                    | `AlarmManager` + `PendingIntent` + `Calendar` setup |     `getAlarmController().registerAlarmClock()`     |       **Complex setup → Simple call**        |
| **Notification Display**                  |   `NotificationManager` + Channel + Builder setup   |  `getNotificationController().showNotification()`   |         **Auto channel management**          |
| **WiFi Info Query**                       | `WifiManager` + SDK branching + Permission handling |      `getWifiController().getConnectionInfo()`      |            **Auto SDK handling**             |
| **Floating View Management**              | `WindowManager` + `LayoutParams` + Touch handling   | `getFloatingViewController().addFloatingDragView()` |           **Auto Touch handling**            |

**Key takeaway:** System Service Manager Controller simplifies complex system service calls with **Extension functions**.

> **핵심:** System Service Manager Controller는 복잡한 시스템 서비스 호출을 **Extension 함수**로 단순화합니다.

<br></br>

## 💡 Why It Matters

### Eliminate Repetitive Code (반복 코드 제거)
- **System Service Acquisition:** Simplify `getSystemService()` calls with Extension functions
- **Auto SDK Version Handling:** Automatically handle Vibrator/VibratorManager version branching internally
- **Hide Complex Configuration:** Encapsulate Alarm Calendar calculations, Floating View Touch handling, etc.
> - **시스템 서비스 획득**: `getSystemService()` 호출과 Extension 함수로 간단하게
> - **SDK 버전 처리 자동화**: Vibrator/VibratorManager 버전 분기를 내부에서 자동 처리
> - **복잡한 설정 숨김**: Alarm Calendar 계산, Floating View Touch 처리 등을 캡슐화

<br></br>

### Safe Error Handling (안전한 에러 처리)
- **Automatic Exception Handling:** Controller automatically handles exceptions and returns Runtime results
- **Return Result Values:** Return safe Boolean via `tryCatchSystemManager()`
- **Lifecycle Integration:** Automatically cleanup all resources on `onDestroy()`
> - **자동 예외 처리**: Controller 내부에서 자동 예외 처리 후 Runtime 결과 반환
> - **결과 값 리턴**: `tryCatchSystemManager()` 통해 안전한 Boolean 반환
> - **Lifecycle 연동**: `onDestroy()` 시 모든 리소스 자동 정리

<br></br>

### Developer-Friendly Interface (개발자 친화적 인터페이스)
- **Unified API:** Intuitive methods like `show()`, `vibrate()`, `registerAlarmClock()`
- **Consistent Code Style:** Unify all services with Controller pattern
- **Type Safety:** Compile-time error checking support
> - **통합 API 제공**: `show()`, `vibrate()`, `registerAlarmClock()` 등 직관적 메서드
> - **일관된 코드 스타일**: Controller 패턴으로 모든 서비스 통일
> - **타입 안전성**: Compile-time 오류 체크 지원

<br></br>

## SystemManager VS Controller 


### #1: SoftKeyboardController

<details>
<summary><strong>Plain Android - SoftKeyboard Display (순수 Android - SoftKeyboard 표시)</strong></summary>

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
**Issues:**
- Multiple `getSystemService()` calls and type casting
- Manual null handling and focus handling repeated
- Must implement delayed execution manually
- **Complex SDK version branching** (ADJUST_RESIZE deprecated in Android 11+)
- WindowInsetsController null check and fallback handling required
- Additional learning needed for WindowCompat and WindowInsets API
- Boilerplate-heavy structure

> **문제점:**
> - 여러 단계의 `getSystemService()` 호출과 타입 캐스팅
> - Null 처리, Focus 처리 수동으로 반복
> - 지연 실행 기능을 직접 구현해야 함
> - **SDK 버전 분기 처리 복잡** (Android 11+에서 ADJUST_RESIZE deprecated)
> - WindowInsetsController null 체크 및 fallback 처리 필요
> - WindowCompat, WindowInsets API 추가 학습 필요
> - 보일러플레이트 코드가 많은 구조

<br></br>
</details>

<details>
<summary><strong>Simple UI - SoftKeyboard Controller</strong></summary>

```kotlin
// Simple keyboard display - One line (간단한 키보드 표시 - 한 줄)
private fun showKeyboard(editText: EditText) {
    getSoftKeyboardController().show(editText) // Done! (끝!)
}

// Delayed display - One line (지연 표시 - 한 줄)
private fun showKeyboardWithDelay(editText: EditText, delayMillis: Long) {
    getSoftKeyboardController().showDelay(editText, delayMillis) // Done! (끝!)
}

// Coroutine support (Coroutine 지원)
private fun showKeyboardWithCoroutine(editText: EditText) {
    getSoftKeyboardController().showDelay(editText, 300, lifecycleScope)
}

// Window Input Mode setup (Window Input Mode 설정)
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    getSoftKeyboardController().setAdjustPan(window)
}

// Window Input Mode - Adjust Resize setup (Window Input Mode - Adjust Resize 설정)
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    getSoftKeyboardController().setAdjustResize(window) // Auto SDK version branching! (SDK 버전 자동 분기!)
}

// ⭐ setAdjustResize() internal implementation (Library code)
// (⭐ setAdjustResize() 내부 구현 (라이브러리 코드))
public fun setAdjustResize(window: Window) {
    checkSdkVersion(Build.VERSION_CODES.R,
        positiveWork = {
            // Android 11+: Use WindowInsetsController (WindowInsetsController 사용)
            val controller = window.insetsController
            if (controller != null) {
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                // Fallback: Use WindowCompat (Fallback: WindowCompat 사용)
                WindowCompat.setDecorFitsSystemWindows(window, true)
            }
        },
        negativeWork = {
            // Android 10 and below: Traditional method (Android 10 이하: 기존 방식)
            @Suppress("DEPRECATION")
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    )
}
```
**Advantages:**
- **Dramatically simplified** (Dozens of lines → One line)
- Automated null handling and focus handling
- Built-in delayed execution (Runnable/Coroutine)
- **Clean SDK version branching with checkSdkVersion() helper**
- **Automatic SDK version branching** (Android 11+ WindowInsetsController auto-used)
- Automated WindowInsetsController null handling and WindowCompat fallback
- Safe exception handling, Boolean return
> **장점:**
> - **극적인 코드 간소화** (수십 줄 → 한 줄)
> - Null 처리, Focus 처리 자동화
> - 지연 실행 (Runnable/Coroutine) 기본 제공
> - **checkSdkVersion() 헬퍼로 깔끔한 SDK 버전 분기**
> - **SDK 버전 자동 분기 처리** (Android 11+ WindowInsetsController 자동 사용)
> - WindowInsetsController null 처리 및 WindowCompat fallback 자동화
> - 안전한 예외 처리, Boolean 반환
</details>

<br>
</br>

### #2: VibratorController

<details>
<summary><strong>Plain Android - Vibrator Control (순수 Android - Vibrator 제어)</strong></summary>

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
**Issues:**
- Required branching for 3 SDK versions
- Repeated `getSystemService()` calls and type casting
- Manual suppression of Deprecated API
- Difficult to maintain with complex structure
> **문제점:**
> - 3가지 SDK 버전별 분기 처리 필수
> - `getSystemService()` 반복 호출과 타입 캐스팅
> - Deprecated API 수동 Suppress 처리
> - 복잡한 구조로 유지보수 어려움

<br></br>
</details>

<details>
<summary><strong>Simple UI - Vibrator Controller</strong></summary>

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
**Advantages (장점):**
- **Dramatically simplified** (Complex branching → Single call)
- Complete automatic SDK version branching (Vibrator/VibratorManager)
- Automatic exception handling
- Deprecated API handled internally
- Safe exception handling, Boolean return
> **장점:**
> - **대폭 간소화** (복잡한 분기 → 단일 호출)
> - SDK 버전 분기 완전 자동 (Vibrator/VibratorManager)
> - 자동 예외 처리
> - Deprecated API 내부에서 처리
> - 안전한 예외 처리, Boolean 반환
</details>

<br>
</br>

### #3: AlarmController

<details>
<summary><strong>Plain Android - Alarm Registration (순수 Android - Alarm 등록)</strong></summary>

```kotlin
// Traditional Alarm registration method (기존의 Alarm 등록 방법)
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
private fun registerAlarm(hour: Int, minute: Int) {
    // 1. Acquire AlarmManager (AlarmManager 획득)
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // 2. Calendar setup - Time calculation (Calendar 설정 - 시간 계산)
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        // If time passed today, set to tomorrow (Manual handling)
        // (오늘 시간 지났으면 내일로 설정 (수동 처리))
        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }
    }

    // 3. Create PendingIntent - Complex flags (PendingIntent 생성 - 복잡한 플래그)
    val intent = Intent(this, AlarmReceiver::class.java).apply {
        putExtra("ALARM_KEY", 1)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        this,
        1,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 4. Create and register AlarmClockInfo (AlarmClockInfo 생성 및 등록)
    val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
}

// Alarm removal - Complex handling (Alarm 삭제 - 복잡한 처리)
private fun removeAlarm(key: Int) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(this, AlarmReceiver::class.java).apply {
        putExtra("ALARM_KEY", key)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        this,
        key,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )

    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
```
**Issues:**
- Manual Calendar setup and today/tomorrow calculation
- PendingIntent flags must be set manually
- Must create AlarmClockInfo directly
- Need to manage Receiver Class
- Repeated manual null handling
> **문제점:**
> - Calendar 설정 및 오늘/내일 계산 수동 처리
> - PendingIntent 플래그 수동 설정 필수
> - AlarmClockInfo 직접 생성해야 함
> - Receiver Class 관리 필요
> - Null 처리 수동으로 반복
</details>

<details>
<summary><strong>Simple UI - Alarm Controller</strong></summary>

```kotlin
// Simple Alarm registration - One line (간단한 Alarm 등록 - 한 줄)
private fun registerAlarm(hour: Int, minute: Int) {
    val alarmVo = AlarmVo(
        key = 1,
        title = "Alarm Title", // (알람 제목)
        message = "Alarm Message", // (알람 메시지)
        hour = hour,
        minute = minute,
        second = 0
    )
    getAlarmController().registerAlarmClock(AlarmReceiver::class.java, alarmVo) // Done! (끝!)
}

// Exact Alarm (Runs even in Idle mode) (정확한 Alarm (Idle 모드에서도 실행))
private fun registerExactAlarm(hour: Int, minute: Int) {
    val alarmVo = AlarmVo(
        key = 2,
        title = "Exact Alarm", // (정확한 알람)
        message = "Exact Alarm Message",
        hour = hour,
        minute = minute
    )
    getAlarmController().registerAlarmExactAndAllowWhileIdle(AlarmReceiver::class.java, alarmVo)
}

// Alarm removal - One line (Alarm 삭제 - 한 줄)
private fun removeAlarm(key: Int) {
    getAlarmController().remove(key, AlarmReceiver::class.java)
}

// Check Alarm exists - One line (Alarm 존재 확인 - 한 줄)
private fun checkAlarmExists(key: Int): Boolean {
    return getAlarmController().exists(key, AlarmReceiver::class.java)
}
```
**Advantages**
- **Dramatically simplified** (Complex setup → VO object)
- Automatic Calendar calculation (Auto today/tomorrow determination)
- Automatic PendingIntent creation (Built-in flags)
- Automatic AlarmClockInfo creation
- Automatic exception handling, automatic SDK version handling
- Safe exception handling, Boolean return
> **장점:**
> - **대폭 간소화** (복잡한 설정 → VO 객체)
> - Calendar 자동 계산 (오늘/내일 자동 판단)
> - PendingIntent 자동 생성 (플래그 내장)
> - AlarmClockInfo 자동 생성
> - 자동 예외 처리, SDK 버전 자동 처리
> - 안전한 예외 처리, Boolean 반환
</details>

<br>
</br>

### #4: FloatingViewController

<details>
<summary><strong>Plain Android - Floating View Management (순수 Android - Floating View 관리)</strong></summary>

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
            // ... Dozens of lines of Touch handling code (수십 줄의 Touch 처리 코드)
        }
    }
}
```
**Issues:**
- Complex WindowManager and LayoutParams setup
- SDK version-specific TYPE branching required
- Must implement Touch events directly (dozens of lines)
- Must implement Collision Detection directly
- Memory leak risk
> **문제점:**
> - WindowManager, LayoutParams 복잡한 설정
> - SDK 버전별 TYPE 분기 필요
> - Touch 이벤트 직접 구현 필요 (수십 줄)
> - Collision Detection 직접 구현 필요
> - 메모리 누수 위험

<br></br>
</details>

<details>
<summary><strong>Simple UI - Floating View Controller</strong></summary>

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

    floatingViewController.addFloatingDragView(dragView) // Done! (끝!)
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
**Advantages):**
- **Dramatically simplified** (Dozens of lines → Few lines)
- Automatic WindowManager and LayoutParams handling
- Automatic Touch event handling (Flow-based)
- Automatic Collision Detection provided
- Memory leak prevention (Automatic Lifecycle management)
- Automatic SDK version branching
> **장점:**
> - **큰 폭으로 간소화** (수십 줄 → 몇 줄)
> - WindowManager, LayoutParams 자동 처리
> - Touch 이벤트 자동 처리 (Flow 기반)
> - Collision Detection 자동 제공
> - 메모리 누수 방지 (Lifecycle 자동 관리)
> - SDK 버전 자동 분기
</details>

<br>
</br>

### #5: NotificationController

<details>
<summary><strong>Plain Android - Notification Display (순수 Android - Notification 표시)</strong></summary>

```kotlin
// Traditional Notification display method (기존의 Notification 표시 방법)
@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
private fun showNotification() {
    // 1. Acquire NotificationManager (NotificationManager 획득)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 2. Create Notification Channel (Android 8.0+) (Notification Channel 생성 (Android 8.0+))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "channel_id",
            "Channel Name",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel Description"
        }
        notificationManager.createNotificationChannel(channel)
    }

    // 3. Create PendingIntent (PendingIntent 생성)
    val intent = Intent(this, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 4. Create Notification Builder (Notification Builder 생성)
    val builder = NotificationCompat.Builder(this, "channel_id").apply {
        setContentTitle("Title") // (제목)
        setContentText("Content") // (내용)
        setSmallIcon(R.drawable.ic_notification)
        setAutoCancel(true)
        setContentIntent(pendingIntent)
    }

    // 5. Display Notification (Notification 표시)
    notificationManager.notify(1, builder.build())
}

// Progress notification - Complex implementation (진행률 알림 - 복잡한 구현)
private var progressBuilder: NotificationCompat.Builder? = null

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
private fun showProgressNotification(progress: Int) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (progressBuilder == null) {
        progressBuilder = NotificationCompat.Builder(this, "channel_id").apply {
            setContentTitle("Downloading") // (다운로드 중)
            setContentText("File Download") // (파일 다운로드)
            setSmallIcon(R.drawable.ic_download)
            setOngoing(true)
            setPriority(NotificationCompat.PRIORITY_LOW)
        }
    }

    progressBuilder?.setProgress(100, progress, false)
    notificationManager.notify(2, progressBuilder!!.build())
}
```
**Issues:**
- Manual Channel creation, Builder setup, and PendingIntent
- SDK version branching required (Android 8.0+)
- Must manage Builder reference directly for progress notifications
- Memory leak risk (Storing Builder reference)
> **문제점:**
> - Channel 생성, Builder 설정, PendingIntent 모두 수동
> - SDK 버전 분기 필요 (Android 8.0+)
> - 진행률 알림용 Builder 참조 직접 관리
> - 메모리 누수 위험 (Builder 참조 보관)

<br></br>
</details>

<details>
<summary><strong>Simple UI - Notification Controller</strong></summary>

```kotlin
// Simple Notification display - One line (간단한 Notification 표시 - 한 줄)
private fun showNotification() {
    val notificationOption = SimpleNotificationOptionVo(
        notificationId = 1,
        title = "Title", // (제목)
        content = "Content", // (내용)
        smallIcon = R.drawable.ic_notification,
        isAutoCancel = true,
        clickIntent = Intent(this, MainActivity::class.java)
    )
    getNotificationController(SimpleNotificationType.ACTIVITY).showNotification(notificationOption) // Done! (끝!)
}

// Progress notification - Simple creation and update (진행률 알림 - 간단한 생성 및 업데이트)
private fun showProgressNotification() {
    val progressOption = SimpleProgressNotificationOptionVo(
        notificationId = 2,
        title = "Downloading", // (다운로드 중)
        content = "File Download", // (파일 다운로드)
        smallIcon = R.drawable.ic_download,
        progressPercent = 0,
        onGoing = true
    )
    getNotificationController(SimpleNotificationType.ACTIVITY).showProgressNotification(progressOption)
}

// Progress update - One line (진행률 업데이트 - 한 줄)
private fun updateProgress(progress: Int) {
    getNotificationController(SimpleNotificationType.ACTIVITY).updateProgress(2, progress)
}

// Progress completion - One line (진행률 완료 - 한 줄)
private fun completeProgress() {
    getNotificationController(SimpleNotificationType.ACTIVITY).completeProgress(2, "Download Complete") // (다운로드 완료)
}

// BigText style notification (BigText 스타일 알림)
private fun showBigTextNotification() {
    val option = SimpleNotificationOptionVo(
        notificationId = 3,
        title = "Long Text Notification", // (긴 텍스트 알림)
        content = "Summary", // (요약 내용)
        snippet = "Very long text will be displayed here. " + // (매우 긴 텍스트가 여기에 표시됩니다.)
                 "You can see the full content when expanded.", // (확장하면 전체 내용을 볼 수 있습니다.)
        smallIcon = R.drawable.ic_notification,
        style = NotificationStyle.BIG_TEXT
    )
    getNotificationController(SimpleNotificationType.ACTIVITY).showNotification(option)
}
```
**Advantages:**
- **Dramatically simplified** (Complex setup → VO object)
- Automatic Channel creation and management
- Automatic PendingIntent creation (Type-specific)
- Automatic Builder reference management (Memory leak prevention)
- Automatic progress notification cleanup (After 30 minutes)
- Easy support for various styles (DEFAULT, BIG_TEXT, BIG_PICTURE, PROGRESS)
> **장점:**
> - **대폭 간소화** (복잡한 설정 → VO 객체)
> - Channel 자동 생성 및 관리
> - PendingIntent 자동 생성 (타입별 구분)
> - Builder 참조 자동 관리 (메모리 누수 방지)
> - 진행률 알림 자동 정리 (30분 후)
> - 다양한 스타일 간편 지원 (DEFAULT, BIG_TEXT, BIG_PICTURE, PROGRESS)
</details>

<br>
</br>

### #6: WiFiController

<details>
<summary><strong>Plain Android - WiFi Information Query (순수 Android - WiFi 정보 조회)</strong></summary>

```kotlin
// Traditional WiFi information query method (기존의 WiFi 정보 조회 방법)
@RequiresPermission(allOf = [Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE])
private fun getWifiInfo() {
    // 1. Acquire WifiManager (WifiManager 획득)
    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // 2. SDK version-specific branching (SDK 버전별 분기 처리)
    val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ - Use NetworkCapabilities (NetworkCapabilities 사용)
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            (capabilities.transportInfo as? WifiInfo)
        } else {
            null
        }
    } else {
        // Android 11 and below - Legacy API (Android 11 이하 - 구형 API)
        @Suppress("DEPRECATION")
        wifiManager.connectionInfo
    }

    // 3. Extract information (정보 추출)
    wifiInfo?.let { info ->
        val ssid = info.ssid.removeSurrounding("\"")
        val bssid = info.bssid
        val rssi = info.rssi
        val linkSpeed = info.linkSpeed

        Log.d("WiFi", "SSID: $ssid, RSSI: $rssi, Speed: $linkSpeed Mbps")
    }
}

// WiFi scan - Complex permissions and handling (WiFi 스캔 - 복잡한 권한 및 처리)
@RequiresPermission(allOf = [
    Manifest.permission.CHANGE_WIFI_STATE,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.ACCESS_FINE_LOCATION
])
private fun scanWifi() {
    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // Start scan (스캔 시작)
    @Suppress("DEPRECATION")
    val success = wifiManager.startScan()

    if (success) {
        // Query scan results (스캔 결과 조회)
        val results = wifiManager.scanResults
        results.forEach { result ->
            Log.d("WiFi", "SSID: ${result.SSID}, Level: ${result.level}")
        }
    }
}

// Calculate signal strength level (신호 강도 레벨 계산)
private fun calculateSignalLevel(rssi: Int): Int {
    return WifiManager.calculateSignalLevel(rssi, 5)
}
```
**Issues:**
- Complex SDK version-specific branching
- Need to use both ConnectivityManager and WifiManager
- Manual SSID quote removal
- Manual Deprecated API suppression
- Complex permission handling (Multiple permission combinations)
> **문제점:**
> - SDK 버전별 분기 처리 복잡
> - ConnectivityManager, WifiManager 모두 사용 필요
> - SSID 따옴표 제거 수동 처리
> - Deprecated API 수동 Suppress
> - 권한 처리 복잡 (여러 권한 조합)

<br></br>
</details>

<details>
<summary><strong>Simple UI - WiFi Controller</strong></summary>

```kotlin
// Simple WiFi information query - One line (간단한 WiFi 정보 조회 - 한 줄)
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
private fun getWifiInfo() {
    val wifiInfo = getWifiController().getConnectionInfo() // Auto SDK branching! (SDK 자동 분기!)

    wifiInfo?.let {
        val ssid = getWifiController().getCurrentSsid() // Auto quote removal (따옴표 자동 제거)
        val rssi = getWifiController().getCurrentRssi()
        val linkSpeed = getWifiController().getCurrentLinkSpeed()

        Log.d("WiFi", "SSID: $ssid, RSSI: $rssi, Speed: $linkSpeed Mbps")
    }
}

// WiFi scan - Simple call (WiFi 스캔 - 간단한 호출)
@RequiresPermission(allOf = [
    Manifest.permission.CHANGE_WIFI_STATE,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.ACCESS_FINE_LOCATION
])
private fun scanWifi() {
    getWifiController().startScan()
    val results = getWifiController().getScanResults()

    results.forEach { result ->
        Log.d("WiFi", "SSID: ${result.SSID}, Level: ${result.level}")
    }
}

// Check signal strength and connection status (신호 강도 및 연결 상태 확인)
private fun checkWifiStatus() {
    val isConnected = getWifiController().isConnectedWifi()
    val isEnabled = getWifiController().isWifiEnabled()
    val rssi = getWifiController().getCurrentRssi()
    val signalLevel = getWifiController().calculateSignalLevel(rssi, 5)

    Log.d("WiFi", "Connected: $isConnected, Enabled: $isEnabled, Signal: $signalLevel/5")
}

// Modern network detailed information (API 29+) (현대적 네트워크 상세 정보 (API 29+))
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
private fun getNetworkDetails() {
    val details = getWifiController().getModernNetworkDetails()

    details?.let {
        Log.d("WiFi", """
            Connected (연결): ${it.isConnected}
            Internet (인터넷): ${it.hasInternet}
            Validated (검증됨): ${it.isValidated}
            Download Speed (다운로드 속도): ${it.linkDownstreamBandwidthKbps} Kbps
            Upload Speed (업로드 속도): ${it.linkUpstreamBandwidthKbps} Kbps
        """.trimIndent())
    }
}

// Check WiFi band support (WiFi 대역 지원 확인)
private fun checkWifiBands() {
    val is5GHz = getWifiController().is5GHzBandSupported()
    val is6GHz = getWifiController().is6GHzBandSupported() // API 30+

    Log.d("WiFi", "5GHz: $is5GHz, 6GHz: $is6GHz")
}
```
**Advantages:**
- **Dramatically simplified** (Complex branching → Single call)
- Automatic SDK version branching (Android 12+ / 11 and below)
- Automatic SSID quote removal
- Deprecated API handled internally
- Convenient helper methods (getCurrentSsid, getCurrentRssi, etc.)
- Automatic modern API support (NetworkCapabilities)
> **장점:**
> - **대폭 간소화** (복잡한 분기 → 단일 호출)
> - SDK 버전 자동 분기 (Android 12+ / 11 이하)
> - SSID 따옴표 자동 제거
> - Deprecated API 내부 처리
> - 편리한 헬퍼 메서드 (getCurrentSsid, getCurrentRssi 등)
> - 현대적 API 자동 지원 (NetworkCapabilities)
</details>

<br>
</br>
