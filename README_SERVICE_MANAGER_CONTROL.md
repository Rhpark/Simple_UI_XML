# System Service Manager Controller vs 순수 Android - 완벽 비교 가이드

> **"복잡한 System Service를 한 줄로 끝내자!"** 기존 Android System Service 사용 대비 Simple UI Controller가 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 한눈 비교 (At a glance)

| 항목 | 순수 Android | Simple UI Controller | 개선 효과 |
|:--|:--:|:--:|:--:|
| **키보드 SoftKeyboard 제어** | `getSystemService()`<br>`showSoftInput()` 호출 | `getSoftKeyboardController().show()` | **수십 줄 → 한 줄** |
| **진동 Vibrator 제어** | SDK 버전 분기 + 3가지 API | `getVibratorController().vibrate()` | **SDK 자동 처리** |
| **알람 Alarm 등록** | `AlarmManager` + `PendingIntent` + `Calendar` 설정 | `getAlarmController().registerAlarmClock()` | **복잡한 설정 → 간단 호출** |
| **알림 Notification 표시** | `NotificationManager` + Channel + Builder 설정 | `getNotificationController().showNotification()` | **채널 자동 관리** |
| **WiFi 정보 조회** | `WifiManager` + SDK 분기 + 권한 처리 | `getWifiController().getConnectionInfo()` | **SDK 자동 처리** |
| **플로팅 Floating View 관리** | `WindowManager` + `LayoutParams` + Touch 처리 | `getFloatingViewController().addFloatingDragView()` | **Touch 자동화** |

> **핵심:** System Service Manager Controller는 복잡한 시스템 서비스 호출을 **Extension 함수**로 단순화합니다.

<br>
</br>

## 💡 왜 중요한가:

### 반복 코드 제거
- **시스템 서비스 획득**: `getSystemService()` 호출과 Extension 함수로 간단하게
- **SDK 버전 처리 자동화**: Vibrator/VibratorManager 버전 분기를 내부에서 자동 처리
- **복잡한 설정 숨김**: Alarm Calendar 계산, Floating View Touch 처리 등을 캡슐화

<br>
</br>

### 안전한 에러 처리
- **자동 예외 처리**: Controller 내부에서 자동 예외 처리 후 Runtime 결과 반환
- **결과 값 리턴**: `tryCatchSystemManager()` 통해 안전한 Boolean 반환
- **Lifecycle 연동**: `onDestroy()` 시 모든 리소스 자동 정리

<br>
</br>

### 개발자 친화적 인터페이스
- **통합 API 제공**: `show()`, `vibrate()`, `registerAlarmClock()` 등 직관적 메서드
- **일관된 코드 스타일**: Controller 패턴으로 모든 서비스 통일
- **타입 안전성**: Compile-time 오류 체크 지원

<br>
</br>

## 실제 코드 비교

<br>
</br>

### 첫째: SoftKeyboard 제어 비교

<details>
<summary><strong>순수 Android - SoftKeyboard 표시</strong></summary>

```kotlin
// 기존의 SoftKeyboard 표시 방법
private fun showKeyboard(editText: EditText) {
    // 1. InputMethodManager 획득
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

    if (imm != null) {
        // 2. Focus 처리
        if (editText.requestFocus()) {
            // 3. 키보드 표시
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        } else {
            Log.e("Keyboard", "Failed to request focus")
        }
    } else {
        Log.e("Keyboard", "InputMethodManager is null")
    }
}

// 지연 표시 - 별도 구현
private fun showKeyboardWithDelay(editText: EditText, delayMillis: Long) {
    editText.postDelayed({
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (imm != null && editText.requestFocus()) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }, delayMillis)
}

// Window Input Mode 설정
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    // ...
}
```
**문제점:**
- 여러 단계의 `getSystemService()` 호출과 타입 캐스팅
- Null 처리, Focus 처리 수동으로 반복
- 지연 실행 기능을 직접 구현해야 함
- 보일러플레이트 코드가 많은 구조
</details>

<details>
<summary><strong>Simple UI - SoftKeyboard Controller</strong></summary>

```kotlin
// 간단한 키보드 표시 - 한 줄
private fun showKeyboard(editText: EditText) {
    getSoftKeyboardController().show(editText) // 끝!
}

// 지연 표시 - 한 줄
private fun showKeyboardWithDelay(editText: EditText, delayMillis: Long) {
    getSoftKeyboardController().showDelay(editText, delayMillis) // 끝!
}

// Coroutine 지원
private fun showKeyboardWithCoroutine(editText: EditText) {
    getSoftKeyboardController().showDelay(editText, 300, lifecycleScope)
}

// Window Input Mode 설정
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    getSoftKeyboardController().setAdjustPan(window)
}
```
**장점:**
- **극적인 코드 간소화** (수십 줄 → 한 줄)
- Null 처리, Focus 처리 자동화
- 지연 실행 (Runnable/Coroutine) 기본 제공
- 안전한 예외 처리, Boolean 반환
</details>

<br>
</br>

### 둘째: Vibrator 제어 비교

<details>
<summary><strong>순수 Android - Vibrator 제어</strong></summary>

```kotlin
// 기존의 SDK 버전 분기 처리
@RequiresPermission(Manifest.permission.VIBRATE)
private fun vibrate(milliseconds: Long) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ (API 31+) - VibratorManager 사용
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator

        val effect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
        vibratorManager.vibrate(CombinedVibration.createParallel(effect))

    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Android 8+ (API 26+) - VibrationEffect 사용
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val effect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)

    } else {
        // Android 7 이하 - Deprecated API 사용
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        @Suppress("DEPRECATION")
        vibrator.vibrate(milliseconds)
    }
}

// 패턴 진동 - 복잡한 분기
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
**문제점:**
- 3가지 SDK 버전별 분기 처리 필수
- `getSystemService()` 반복 호출과 타입 캐스팅
- Deprecated API 수동 Suppress 처리
- 복잡한 구조로 유지보수 어려움
</details>

<details>
<summary><strong>Simple UI - Vibrator Controller</strong></summary>

```kotlin
// 단순 진동 - 한 줄
private fun vibrate(milliseconds: Long) {
    getVibratorController().vibrate(milliseconds) // SDK 자동 처리!
}

// 패턴 진동 - 한 줄
private fun vibratePattern(pattern: LongArray, repeat: Int = -1) {
    getVibratorController().vibratePattern(pattern, repeat) // 끝!
}

// 웨이브폼 진동 (커스텀 패턴)
private fun vibrateWaveform() {
    val times = longArrayOf(0, 100, 50, 200, 50, 100)
    val amplitudes = intArrayOf(0, 128, 0, 255, 0, 128)
    getVibratorController().createWaveform(times, amplitudes, -1)
}

// 시스템 정의 진동
private fun vibrateClick() {
    getVibratorController().createPredefined(VibrationEffect.EFFECT_CLICK)
}

// 진동 취소
private fun cancelVibrate() {
    getVibratorController().cancel()
}
```
**장점:**
- **대폭 간소화** (복잡한 분기 → 단일 호출)
- SDK 버전 분기 완전 자동 (Vibrator/VibratorManager)
- 자동 예외 처리
- Deprecated API 내부에서 처리
- 안전한 예외 처리, Boolean 반환
</details>

<br>
</br>

### 셋째: Alarm 등록 비교

<details>
<summary><strong>순수 Android - Alarm 등록</strong></summary>

```kotlin
// 기존의 Alarm 등록 방법
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
private fun registerAlarm(hour: Int, minute: Int) {
    // 1. AlarmManager 획득
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // 2. Calendar 설정 - 시간 계산
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        // 오늘 시간 지났으면 내일로 설정 (수동 처리)
        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }
    }

    // 3. PendingIntent 생성 - 복잡한 플래그
    val intent = Intent(this, AlarmReceiver::class.java).apply {
        putExtra("ALARM_KEY", 1)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        this,
        1,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 4. AlarmClockInfo 생성 및 등록
    val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
}

// Alarm 삭제 - 복잡한 처리
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
**문제점:**
- Calendar 설정 및 오늘/내일 계산 수동 처리
- PendingIntent 플래그 수동 설정 필수
- AlarmClockInfo 직접 생성해야 함
- Receiver Class 관리 필요
- Null 처리 수동으로 반복
</details>

<details>
<summary><strong>Simple UI - Alarm Controller</strong></summary>

```kotlin
// 간단한 Alarm 등록 - 한 줄
private fun registerAlarm(hour: Int, minute: Int) {
    val alarmVo = AlarmVo(
        key = 1,
        title = "알람 제목",
        hour = hour,
        minute = minute,
        second = 0
    )
    getAlarmController().registerAlarmClock(AlarmReceiver::class.java, alarmVo) // 끝!
}

// 정확한 Alarm (Idle 모드에서도 실행)
private fun registerExactAlarm(hour: Int, minute: Int) {
    val alarmVo = AlarmVo(key = 2, title = "정확한 알람", hour = hour, minute = minute)
    getAlarmController().registerAlarmExactAndAllowWhileIdle(AlarmReceiver::class.java, alarmVo)
}

// Alarm 삭제 - 한 줄
private fun removeAlarm(key: Int) {
    getAlarmController().remove(key, AlarmReceiver::class.java)
}

// Alarm 존재 확인 - 한 줄
private fun checkAlarmExists(key: Int): Boolean {
    return getAlarmController().exists(key, AlarmReceiver::class.java)
}
```
**장점:**
- **큰 폭으로 간소화** (복잡한 설정 → VO 객체)
- Calendar 자동 계산 (오늘/내일 자동 판단)
- PendingIntent 자동 생성 (플래그 내장)
- AlarmClockInfo 자동 생성
- 자동 예외 처리, SDK 버전 자동 처리
- 안전한 예외 처리, Boolean 반환
</details>

<br>
</br>

### 넷째: Floating View 관리 비교

<details>
<summary><strong>순수 Android - Floating View 관리</strong></summary>

```kotlin
// 기존의 Floating View 추가
@RequiresPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
private fun addFloatingView() {
    // 1. WindowManager 획득
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

    // 2. LayoutParams 설정 - 복잡한 옵션
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

    // 3. View 생성
    val floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null)

    // 4. Touch 이벤트 수동 추가 - 매우 복잡
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

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - initialTouchX
                val deltaY = event.rawY - initialTouchY

                if (abs(deltaX) > 5 || abs(deltaY) > 5) {
                    isDragging = true
                    params.x = initialX + deltaX.toInt()
                    params.y = initialY + deltaY.toInt()

                    // 화면 경계 처리 - 수동 추가
                    params.x = params.x.coerceAtLeast(0)
                    params.y = params.y.coerceAtLeast(0)

                    windowManager.updateViewLayout(view, params)
                }
                true
            }

            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    view.performClick()
                }
                isDragging = false
                true
            }

            else -> false
        }
    }

    // 5. View 추가
    try {
        windowManager.addView(floatingView, params)
    } catch (e: Exception) {
        Log.e("FloatingView", "Failed to add floating view", e)
    }
}

// View 제거 - 참조 관리 필요
private var floatingView: View? = null

private fun removeFloatingView() {
    floatingView?.let {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            it.setOnTouchListener(null) // 리소스 정리 필요
            windowManager.removeView(it)
            floatingView = null
        } catch (e: Exception) {
            Log.e("FloatingView", "Failed to remove floating view", e)
        }
    }
}
```
**문제점:**
- WindowManager + LayoutParams 수동 설정 필수
- SDK 버전별 TYPE 분기 처리 필요
- Touch 이벤트를 완전히 수동으로 구현 (매우 복잡)
- 드래그 로직, 경계 계산 직접 구현
- 화면 경계 처리 수동 추가
- View 참조 관리 및 리소스 정리 직접 필요
</details>

<details>
<summary><strong>Simple UI - Floating View Controller</strong></summary>

```kotlin
// 간단한 Floating View 추가 - 자동 처리
@RequiresPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
private fun addFloatingView() {
    // 1. View 생성
    val icon = getImageView(R.drawable.ic_launcher_foreground).apply { setBackgroundColor(Color.WHITE) }
    // 2. FloatingDragView 생성 (Touch 이벤트 자동화)
    val dragView = FloatingDragView(icon, 100, 100).apply {
        lifecycleScope.launch {
            sfCollisionStateFlow.collect { item ->
                // 충돌 상태 자동화 및 콜백 (선택적)
                when (item.first) {
                    FloatingViewTouchType.TOUCH_DOWN -> { showFloatingView() }
                    FloatingViewTouchType.TOUCH_MOVE -> { moveFloatingView(item) }
                    FloatingViewTouchType.TOUCH_UP -> { upFloatingView(this@apply,item) }
                }
            }
        }
    }
    floatingViewController.addFloatingDragView(dragView)
}

private fun showFloatingView() {
    floatingViewController.getFloatingFixedView()?.view?.let {
        it.setVisible()
        showAnimScale(it, null)
    }
}

private fun moveFloatingView(item: Pair<FloatingViewTouchType, FloatingViewCollisionsType>) {
    floatingViewController.getFloatingFixedView()?.view?.let {
        if (item.second == FloatingViewCollisionsType.OCCURING) {
            val rotationAnim = ObjectAnimator.ofFloat(it, "rotation", 0.0f, 180.0f)
            rotationAnim.duration = 300
            rotationAnim.start()
        }
    }
}

private fun upFloatingView(floatingView:FloatingDragView,item: Pair<FloatingViewTouchType, FloatingViewCollisionsType>) {
    floatingViewController.getFloatingFixedView()?.view?.let {
        hideAnimScale(it, object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                floatingViewController.getFloatingFixedView()?.let { it.view.setGone() }
                if (item.second == FloatingViewCollisionsType.OCCURING) {
                    floatingViewController.removeFloatingDragView(floatingView)
                }
            }
        })
    }
}

// 고정 Floating View 추가 (드래그 불가)
@RequiresPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
private fun addFixedFloatingView() {
    val icon = getImageView(R.drawable.ic_launcher_foreground).apply { setBackgroundColor(Color.GREEN) }
    val fixedView = FloatingFixedView(icon, 200, 300) // or FloatingDragView(icon, 200, 300)
    floatingViewController.setFloatingFixedView(fixedView)
}

// View 제거 - 한 줄
private fun removeFloatingView(floatingDragView: FloatingDragView) {
    getFloatingViewController().removeFloatingDragView(floatingDragView)
}

// 모든 View 제거 - 한 줄
private fun removeAllFloatingViews() {
    getFloatingViewController().removeAllFloatingView() // 리소스 자동 정리!
}
```
**장점:**
- **압도적 간소화** (복잡한 구현 → 객체 생성)
- LayoutParams 자동 설정
- SDK 버전 자동 처리
- Touch 이벤트 완전 자동화 (ACTION_DOWN/MOVE/UP)
- 드래그 로직 내장 처리
- 화면 경계 자동 처리
- 충돌 감지 자동화 (드래그 뷰와 고정 뷰)
- Lifecycle 연동 자동 정리 (리소스 누수 방지)
</details>

<br>
</br>

### 다섯째: Notification 표시 비교

<details>
<summary><strong>순수 Android - Notification 표시</strong></summary>

```kotlin
// 기존의 Notification 표시 방법
@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
private fun showNotification() {
    // 1. NotificationManager 획득
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 2. Notification Channel 생성 (Android 8.0+)
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

    // 3. PendingIntent 생성
    val intent = Intent(this, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 4. Notification Builder 생성
    val builder = NotificationCompat.Builder(this, "channel_id").apply {
        setContentTitle("제목")
        setContentText("내용")
        setSmallIcon(R.drawable.ic_notification)
        setAutoCancel(true)
        setContentIntent(pendingIntent)
    }

    // 5. Notification 표시
    notificationManager.notify(1, builder.build())
}

// 진행률 알림 - 복잡한 구현
private var progressBuilder: NotificationCompat.Builder? = null

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
private fun showProgressNotification(progress: Int) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (progressBuilder == null) {
        progressBuilder = NotificationCompat.Builder(this, "channel_id").apply {
            setContentTitle("다운로드 중")
            setContentText("파일 다운로드")
            setSmallIcon(R.drawable.ic_download)
            setOngoing(true)
            setPriority(NotificationCompat.PRIORITY_LOW)
        }
    }

    progressBuilder?.setProgress(100, progress, false)
    notificationManager.notify(2, progressBuilder!!.build())
}
```
**문제점:**
- Channel 생성, Builder 설정, PendingIntent 모두 수동
- SDK 버전 분기 필요 (Android 8.0+)
- 진행률 알림용 Builder 참조 직접 관리
- 메모리 누수 위험 (Builder 참조 보관)
</details>

<details>
<summary><strong>Simple UI - Notification Controller</strong></summary>

```kotlin
// 간단한 Notification 표시 - 한 줄
private fun showNotification() {
    val notificationOption = SimpleNotificationOptionVo(
        notificationId = 1,
        title = "제목",
        content = "내용",
        smallIcon = R.drawable.ic_notification,
        isAutoCancel = true,
        clickIntent = Intent(this, MainActivity::class.java)
    )
    getNotificationController(SimpleNotificationType.ACTIVITY).showNotification(notificationOption) // 끝!
}

// 진행률 알림 - 간단한 생성 및 업데이트
private fun showProgressNotification() {
    val progressOption = SimpleProgressNotificationOptionVo(
        notificationId = 2,
        title = "다운로드 중",
        content = "파일 다운로드",
        smallIcon = R.drawable.ic_download,
        progressPercent = 0,
        onGoing = true
    )
    getNotificationController(SimpleNotificationType.ACTIVITY).showProgressNotification(progressOption)
}

// 진행률 업데이트 - 한 줄
private fun updateProgress(progress: Int) {
    getNotificationController(SimpleNotificationType.ACTIVITY).updateProgress(2, progress)
}

// 진행률 완료 - 한 줄
private fun completeProgress() {
    getNotificationController(SimpleNotificationType.ACTIVITY).completeProgress(2, "다운로드 완료")
}

// BigText 스타일 알림
private fun showBigTextNotification() {
    val option = SimpleNotificationOptionVo(
        notificationId = 3,
        title = "긴 텍스트 알림",
        content = "요약 내용",
        snippet = "매우 긴 텍스트가 여기에 표시됩니다. " +
                 "확장하면 전체 내용을 볼 수 있습니다.",
        smallIcon = R.drawable.ic_notification,
        style = NotificationStyle.BIG_TEXT
    )
    getNotificationController(SimpleNotificationType.ACTIVITY).showNotification(option)
}
```
**장점:**
- **큰 폭으로 간소화** (복잡한 설정 → VO 객체)
- Channel 자동 생성 및 관리
- PendingIntent 자동 생성 (타입별 구분)
- Builder 참조 자동 관리 (메모리 누수 방지)
- 진행률 알림 자동 정리 (30분 후)
- 다양한 스타일 간편 지원 (DEFAULT, BIG_TEXT, BIG_PICTURE, PROGRESS)
</details>

<br>
</br>

### 여섯째: WiFi 정보 조회 비교

<details>
<summary><strong>순수 Android - WiFi 정보 조회</strong></summary>

```kotlin
// 기존의 WiFi 정보 조회 방법
@RequiresPermission(allOf = [Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE])
private fun getWifiInfo() {
    // 1. WifiManager 획득
    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // 2. SDK 버전별 분기 처리
    val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ - NetworkCapabilities 사용
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            (capabilities.transportInfo as? WifiInfo)
        } else {
            null
        }
    } else {
        // Android 11 이하 - 구형 API
        @Suppress("DEPRECATION")
        wifiManager.connectionInfo
    }

    // 3. 정보 추출
    wifiInfo?.let { info ->
        val ssid = info.ssid.removeSurrounding("\"")
        val bssid = info.bssid
        val rssi = info.rssi
        val linkSpeed = info.linkSpeed

        Log.d("WiFi", "SSID: $ssid, RSSI: $rssi, Speed: $linkSpeed Mbps")
    }
}

// WiFi 스캔 - 복잡한 권한 및 처리
@RequiresPermission(allOf = [
    Manifest.permission.CHANGE_WIFI_STATE,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.ACCESS_FINE_LOCATION
])
private fun scanWifi() {
    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // 스캔 시작
    @Suppress("DEPRECATION")
    val success = wifiManager.startScan()

    if (success) {
        // 스캔 결과 조회
        val results = wifiManager.scanResults
        results.forEach { result ->
            Log.d("WiFi", "SSID: ${result.SSID}, Level: ${result.level}")
        }
    }
}

// 신호 강도 레벨 계산
private fun calculateSignalLevel(rssi: Int): Int {
    return WifiManager.calculateSignalLevel(rssi, 5)
}
```
**문제점:**
- SDK 버전별 분기 처리 복잡
- ConnectivityManager, WifiManager 모두 사용 필요
- SSID 따옴표 제거 수동 처리
- Deprecated API 수동 Suppress
- 권한 처리 복잡 (여러 권한 조합)
</details>

<details>
<summary><strong>Simple UI - WiFi Controller</strong></summary>

```kotlin
// 간단한 WiFi 정보 조회 - 한 줄
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
private fun getWifiInfo() {
    val wifiInfo = getWifiController().getConnectionInfo() // SDK 자동 분기!

    wifiInfo?.let {
        val ssid = getWifiController().getCurrentSsid() // 따옴표 자동 제거
        val rssi = getWifiController().getCurrentRssi()
        val linkSpeed = getWifiController().getCurrentLinkSpeed()

        Log.d("WiFi", "SSID: $ssid, RSSI: $rssi, Speed: $linkSpeed Mbps")
    }
}

// WiFi 스캔 - 간단한 호출
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

// 신호 강도 및 연결 상태 확인
private fun checkWifiStatus() {
    val isConnected = getWifiController().isConnectedWifi()
    val isEnabled = getWifiController().isWifiEnabled()
    val rssi = getWifiController().getCurrentRssi()
    val signalLevel = getWifiController().calculateSignalLevel(rssi, 5)

    Log.d("WiFi", "Connected: $isConnected, Enabled: $isEnabled, Signal: $signalLevel/5")
}

// 현대적 네트워크 상세 정보 (API 29+)
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
private fun getNetworkDetails() {
    val details = getWifiController().getModernNetworkDetails()

    details?.let {
        Log.d("WiFi", """
            연결: ${it.isConnected}
            인터넷: ${it.hasInternet}
            검증됨: ${it.isValidated}
            다운로드 속도: ${it.linkDownstreamBandwidthKbps} Kbps
            업로드 속도: ${it.linkUpstreamBandwidthKbps} Kbps
        """.trimIndent())
    }
}

// WiFi 대역 지원 확인
private fun checkWifiBands() {
    val is5GHz = getWifiController().is5GHzBandSupported()
    val is6GHz = getWifiController().is6GHzBandSupported() // API 30+

    Log.d("WiFi", "5GHz: $is5GHz, 6GHz: $is6GHz")
}
```
**장점:**
- **대폭 간소화** (복잡한 분기 → 단일 호출)
- SDK 버전 자동 분기 (Android 12+ / 11 이하)
- SSID 따옴표 자동 제거
- Deprecated API 내부 처리
- 편리한 헬퍼 메서드 (getCurrentSsid, getCurrentRssi 등)
- 현대적 API 자동 지원 (NetworkCapabilities)
- 5GHz/6GHz 대역 지원 확인 간편화
</details>

<br>
</br>

## System Service Manager Controller의 핵심 장점

### 1. **압도적인 코드 간소화**
- **SoftKeyboard**: 여러 단계 설정 → 한 줄 호출
- **Vibrator**: 복잡한 SDK 분기 → 단일 메서드
- **Alarm**: 복잡한 Calendar 설정 → VO 객체
- **Notification**: Channel/Builder 수동 관리 → 자동 관리
- **WiFi**: SDK 버전별 분기 → 통합 API
- **Floating View**: Touch 이벤트 수동 구현 → 완전 자동화

<br>
</br>

### 2. **SDK 버전 자동 처리**
- **Vibrator**: Vibrator (SDK < 31) 및 VibratorManager (SDK >= 31) 자동 분기
- **Notification**: Channel 생성 (SDK >= 26) 자동 처리
- **WiFi**: WifiInfo 조회 방식 (SDK < 31 / >= 31) 자동 분기
- **Floating View**: TYPE_PHONE 및 TYPE_APPLICATION_OVERLAY 자동 분기
- **Stylus Handwriting**: API 33+ 자동 처리
- **개발자는 신경 쓸 필요 없음!**

<br>
</br>

### 3. **안전한 자동화 및 예외 처리**
- **tryCatchSystemManager()**: 모든 Controller 내부 자동화
- **Boolean 반환**: 성공/실패 여부 반환, 문제 발생 시 기본값 자동 반환
- **Null 처리 자동**: getSystemService() Null 처리 자동
- **권한 처리 자동**: 권한 체크 자동 수행

<br>
</br>

### 4. **Lifecycle 연동 자동 정리**
- **onDestroy() 자동 호출**: BaseSystemService 상속
- **리소스 자동 정리**: Touch 리스너, View 참조 자동 정리
- **메모리 누수 방지**: WindowManager View 자동 제거
- **개발자 신경 쓸 필요 없음!**

<br>
</br>

### 5. **풍부한 기능 제공**
- **SoftKeyboard**: Coroutine 기반 지연 실행 지원
- **Vibrator**: 5가지 진동 타입 제공 (단순/패턴/웨이브폼/시스템 정의/취소)
- **Alarm**: 3가지 알람 타입 (AlarmClock/Exact/Normal) + Calendar 자동 계산
- **Notification**: 4가지 스타일 (DEFAULT/BIG_TEXT/BIG_PICTURE/PROGRESS) + 진행률 자동 관리
- **WiFi**: 연결 정보, 스캔, 신호 강도, 대역 지원 확인 등 풍부한 API
- **Floating View**: 충돌 감지, 드래그/고정 뷰, Touch 콜백 제공

<br>
</br>

## 개발자들의 후기

> **"Vibrator SDK 분기가 이렇게 간단해질줄 몰랐어!"**
>
> **"Notification 진행률 알림 관리가 자동이라니! Builder 참조 관리 안 해도 돼서 편해!"**
>
> **"WiFi 정보 조회할 때 SDK 버전별로 다른 API 쓰는 거 정말 짜증났는데, Controller가 알아서 처리해줘서 좋아!"**
>
> **"Floating View Touch 이벤트 추가하는데 50줄이었는데, Controller로 5줄로 끝났어!"**
>
> **"Alarm Calendar 계산 자동화 정말 편해! 오늘/내일 시간 신경 쓸 필요 없어!"**
>
> **"자동 처리, 안전한 예외 처리, Lifecycle까지 캡슐화! 코드가 깔끔해졌어!"**

<br>
</br>

## 결론: System Service의 새로운 표준

**System Service Manager Controller**는 복잡한 Android System Service 호출을 완전히 바꿉니다.
**getSystemService() 호출과 반복**, **SDK 버전 분기 처리**, **안전한 예외 처리**를
**제거하여 간결한 코드**로 **안전한 개발자 경험**을 제공합니다.

**SoftKeyboard, Vibrator, Alarm, Notification, WiFi, Floating View**
모든 복잡한 System Service가 **Controller 한 줄**로, 간단하고 **강력하게**.

지금 바로 시작하세요! ✨

<br>
</br>

## 실제 구현 예제보기

**라이브 예제 코드:**
> - System Service Manager Controller : `app/src/main/java/kr/open/library/simpleui_xml/system_service_manager/controller/ServiceManagerControllerActivity`
> - System Service Manager Info : `app/src/main/java/kr/open/library/simpleui_xml/system_service_manager/info/ServiceManagerInfoActivity`
> - 실제로 앱을 구동 시켜서 실제 구현 예제를 확인해 보세요!

<br>
</br>

**테스트 가능한 기능:**
- SoftKeyboard 표시/숨김/지연 실행
- Vibrator 단순/패턴/웨이브폼 진동
- Alarm 등록/삭제/존재 확인
- Notification 표시/진행률 업데이트/스타일 변경
- WiFi 정보 조회/스캔/신호 강도 확인
- Floating View 드래그/고정/충돌 감지
- 자동 예외 처리 및 안전한 예외 처리
- SDK 버전별 자동 분기 확인

<br>
</br>

## 🎯 제공되는 Controller 목록

**System Service Manager Controller**는 6가지 핵심 시스템 서비스를 제공합니다:

### **키보드 SoftKeyboard Controller** - 키보드 제어
- **show()/hide()**: 키보드 표시/숨김
- **showDelay()/hideDelay()**: 지연 실행 지원 (Coroutine/Runnable)
- **setSoftInputMode()**: 윈도우 Input Mode 설정
- **startStylusHandwriting()**: 스타일러스 펜 입력 시작 (API 33+)

<br>
</br>

### **진동 Vibrator Controller** - 진동 제어
- **vibrate()**: 단순 진동 (duration)
- **vibratePattern()**: 패턴 진동 (timing array)
- **createOneShot()**: 단발 진동 (duration + amplitude)
- **createWaveform()**: 웨이브폼 진동 (커스텀 패턴)
- **createPredefined()**: 시스템 정의 진동 (CLICK, DOUBLE_CLICK, TICK)
- **cancel()**: 진동 중지
- **hasVibrator()**: 진동 지원 여부 확인
- **SDK 버전 자동 처리**: Vibrator (SDK < 31) 및 VibratorManager (SDK >= 31)

<br>
</br>

### **알람 Alarm Controller** - 알람 관리
- **registerAlarmClock()**: 알람 시계 등록 (상태바 표시)
- **registerAlarmExactAndAllowWhileIdle()**: 정확한 알람 (Idle 모드에서도 실행)
- **registerAlarmAndAllowWhileIdle()**: 일반 알람 (Idle 모드 허용)
- **remove()**: 알람 삭제
- **exists()**: 알람 존재 확인
- **자동 Calendar 계산**: 오늘/내일 시간 자동 처리

<br>
</br>

### **알림 Notification Controller** - 알림 관리
- **showNotification()**: 알림 표시 (다양한 스타일 지원)
- **createChannel()**: 알림 채널 생성 및 관리
- **showProgressNotification()**: 진행률 알림 생성
- **updateProgress()**: 진행률 업데이트 (0~100%)
- **completeProgress()**: 진행률 완료 처리
- **cancelNotification()**: 특정 알림 취소
- **cancelAll()**: 모든 알림 취소
- **다양한 스타일**: DEFAULT, BIG_PICTURE, BIG_TEXT, PROGRESS
- **자동 채널 관리**: 기본 채널 자동 생성 및 관리
- **메모리 관리**: 진행률 알림 자동 정리 (30분 후)

<br>
</br>

### **WiFi Controller** - WiFi 정보 관리
- **isWifiEnabled()**: WiFi 활성화 여부 확인
- **getConnectionInfo()**: 현재 WiFi 연결 정보 조회
- **getScanResults()**: WiFi 스캔 결과 조회
- **getCurrentSsid()**: 현재 연결된 SSID 조회
- **getCurrentRssi()**: 신호 강도 조회
- **getCurrentLinkSpeed()**: 링크 속도 조회
- **isConnectedWifi()**: WiFi 연결 상태 확인
- **is5GHzBandSupported()**: 5GHz 대역 지원 여부
- **is6GHzBandSupported()**: 6GHz 대역 지원 여부 (API 30+)
- **reconnect()/disconnect()**: WiFi 재연결/연결 해제
- **getModernNetworkDetails()**: 네트워크 상세 정보 (API 29+)
- **SDK 버전 자동 처리**: 구형/신형 API 자동 분기

<br>
</br>

### **플로팅 Floating View Controller** - 플로팅 뷰 관리
- **addFloatingDragView()**: 드래그 가능한 플로팅 뷰 추가
- **setFloatingFixedView()**: 고정 플로팅 뷰 설정
- **removeFloatingDragView()**: 드래그 뷰 제거
- **removeFloatingFixedView()**: 고정 뷰 제거
- **removeAllFloatingView()**: 모든 플로팅 뷰 제거
- **충돌 감지**: 드래그 뷰와 고정 뷰 간 충돌 자동 감지
- **Touch 이벤트 자동화**: ACTION_DOWN/MOVE/UP 이벤트 처리

<br>
</br>

## 🔐 **Controller별 필수 권한**

각 Controller는 **사용하는 기능에 따라** 권한이 필요합니다. 필요한 Controller의 권한만 추가하세요.

### 📋 권한 요구사항 요약

| Controller | 필수 권한 | 특수 권한 | 권한 불필요 |
|:--|:--|:--:|:--:|
| **SoftKeyboardController** | - | - | ✅ |
| **VibratorController** | `VIBRATE` | - | - |
| **AlarmController** | `SCHEDULE_EXACT_ALARM` (API 31+) | - | - |
| **NotificationController** | `POST_NOTIFICATIONS` (API 33+) | - | - |
| **WifiController** | `ACCESS_WIFI_STATE`<br>`ACCESS_NETWORK_STATE`<br>`CHANGE_WIFI_STATE`<br>`ACCESS_FINE_LOCATION` | - | - |
| **FloatingViewController** | - | `SYSTEM_ALERT_WINDOW` | - |

<br>
</br>

#### 1️⃣ **SoftKeyboard Controller** - 권한 불필요 ✅

키보드 제어는 **권한이 필요하지 않습니다**.

```kotlin
// 바로 사용 가능
getSoftKeyboardController().show(editText)
```


<br>
</br>


#### 2️⃣ **Vibrator Controller** - VIBRATE 권한 필요

**AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.VIBRATE" />
```

**사용 예시**:
```kotlin
// 권한 선언만으로 바로 사용 가능 (런타임 요청 불필요)
getVibratorController().vibrate(200)
```

> **참고**: `VIBRATE`는 일반 권한으로 **런타임 요청 불필요**


<br>
</br>


#### 3️⃣ **Alarm Controller** - SCHEDULE_EXACT_ALARM 권한 (API 31+)

**AndroidManifest.xml**:
```xml
<!-- Android 12+ (API 31+)에서 정확한 알람 등록 시 필수 -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

**사용 예시**:
```kotlin
// API 31+ 에서는 별도 권한 확인 필요
checkSdkVersion(Build.VERSION_CODES.S) {
    val alarmManager = getSystemService(AlarmManager::class.java)
    if (!alarmManager.canScheduleExactAlarms()) {
        // 설정 화면으로 이동
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        startActivity(intent)
    }
}

// 알람 등록
getAlarmController().registerAlarmClock(receiver, alarmVo)
```

> **참고**: Android 12+ (API 31+)부터는 **사용자가 설정에서 직접 허용**해야 함


<br>
</br>


#### 4️⃣ **Notification Controller** - POST_NOTIFICATIONS 권한 (API 33+)

**AndroidManifest.xml**:
```xml
<!-- Android 13+ (API 33+)에서 알림 표시 시 필수 -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**런타임 권한 요청**:
```kotlin
// Android 13+ 에서는 런타임 권한 요청 필요
checkSdkVersion(Build.VERSION_CODES.TIRAMISU,
    positiveWork = {
        onRequestPermissions(listOf(Manifest.permission.POST_NOTIFICATIONS)) { deniedPermissions ->
            if (deniedPermissions.isEmpty()) {
                // 권한 허용됨
                getNotificationController().showNotification(...)
            } else {
                // 권한 거부됨
                toastShowShort("알림 권한이 필요합니다")
            }
        }
    },
    negativeWork = {
        // Android 12 이하는 권한 불필요
        getNotificationController().showNotification(...)
    }
)
```

> **참고**: Android 13+ (API 33+)부터는 **런타임 권한 요청 필수**


<br>
</br>


#### 5️⃣ **WiFi Controller** - 다중 권한 필요

**AndroidManifest.xml**:
```xml
<!-- 필수: WiFi 상태 조회 -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 선택: WiFi 제어 (켜기/끄기) -->
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />

<!-- 선택: WiFi 스캔 결과 조회 (API 23+) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

**런타임 권한 요청 (WiFi 스캔 사용 시)**:
```kotlin
// WiFi 스캔 결과 조회 시 위치 권한 필요 (Android 6.0+)
onRequestPermissions(listOf(
    Manifest.permission.ACCESS_FINE_LOCATION
)) { deniedPermissions ->
    if (deniedPermissions.isEmpty()) {
        // WiFi 스캔 가능
        val scanResults = getWifiController().getScanResults()
    } else {
        // 기본 정보만 조회
        val connectionInfo = getWifiController().getConnectionInfo()
    }
}
```

> **참고**:
> - `ACCESS_WIFI_STATE`는 일반 권한 (런타임 요청 불필요)
> - `ACCESS_FINE_LOCATION`은 위험 권한 (런타임 요청 필수)
> - WiFi 켜기/끄기는 Android 10+ (API 29+)부터 **더 이상 지원되지 않음**


<br>
</br>


#### 6️⃣ **Floating View Controller** - SYSTEM_ALERT_WINDOW 특수 권한

**AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

**런타임 권한 요청 (특수 권한)**:
```kotlin
// SYSTEM_ALERT_WINDOW는 특수 권한으로 별도 처리 필요
if (!Settings.canDrawOverlays(this)) {
    // 설정 화면으로 이동
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:$packageName")
    )
    startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
} else {
    // 권한 허용됨 - Floating View 추가
    getFloatingViewController().addFloatingDragView(...)
}

// 결과 처리
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_OVERLAY_PERMISSION) {
        if (Settings.canDrawOverlays(this)) {
            // 권한 허용됨
            getFloatingViewController().addFloatingDragView(...)
        } else {
            // 권한 거부됨
            toastShowShort("다른 앱 위에 표시 권한이 필요합니다")
        }
    }
}
```

**Simple UI의 onRequestPermissions() 사용 (자동 처리)**:
```kotlin
// 일반 권한과 특수 권한을 동시에 처리 가능!
onRequestPermissions(listOf(
    Manifest.permission.CAMERA,
    Manifest.permission.SYSTEM_ALERT_WINDOW  // 특수 권한 자동 처리!
)) { deniedPermissions ->
    if (deniedPermissions.isEmpty()) {
        // 모든 권한 허용됨
        getFloatingViewController().addFloatingDragView(...)
    }
}
```

> **참고**: `SYSTEM_ALERT_WINDOW`는 특수 권한으로 **별도 설정 화면**이 필요하지만, Simple UI의 `onRequestPermissions()`를 사용하면 **자동 처리** 가능!

<br>
</br>

### 📊 권한 타입별 정리

| 권한 타입 | 권한 목록 | 요청 방법 |
|:--|:--|:--|
| **일반 권한** | `VIBRATE`<br>`ACCESS_WIFI_STATE`<br>`ACCESS_NETWORK_STATE`<br>`CHANGE_WIFI_STATE` | Manifest 선언만으로 자동 허용 |
| **위험 권한** | `POST_NOTIFICATIONS` (API 33+)<br>`ACCESS_FINE_LOCATION` | 런타임 권한 요청 필수 |
| **특수 권한** | `SYSTEM_ALERT_WINDOW`<br>`SCHEDULE_EXACT_ALARM` (API 31+) | 설정 화면 이동 필요<br>(Simple UI는 자동 처리)
```
