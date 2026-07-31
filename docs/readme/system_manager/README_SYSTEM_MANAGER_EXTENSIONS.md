# System Manager Extensions vs Plain Android - Complete Comparison Guide
> **System Manager Extensions vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_system_manager` (system manager 전용 모듈)
- **Packages**:
  - `kr.open.library.simple_ui.system_manager.core.extensions`
  - `kr.open.library.simple_ui.system_manager.xml.extensions`

<br>

## Overview (개요)
Summarizes system service accessors and controller/info entry points owned by `simple_system_manager`.
> `simple_system_manager`가 제공하는 시스템 서비스 접근자와 controller/info 확장 진입점을 정리합니다.

<br>

## At a Glance (한눈 비교)

### Core System Services (Core 시스템 서비스)

| Function (함수) | Return (반환) | Notes (비고) |
|---|---|---|
| `getBatteryManager()` | `BatteryManager` | Battery service (배터리 서비스) |
| `getTelephonyManager()` | `TelephonyManager` | Telephony service (전화 서비스) |
| `getSubscriptionManager()` | `SubscriptionManager` | Subscription service (구독 서비스) |
| `getEuiccManager()` | `EuiccManager` | eSIM service (eSIM 서비스) |
| `getConnectivityManager()` | `ConnectivityManager` | Connectivity service (연결성 서비스) |
| `getWifiManager()` | `WifiManager` | Wi-Fi service (Wi-Fi 서비스) |
| `getLocationManager()` | `LocationManager` | Location service (위치 서비스) |
| `getAlarmManager()` | `AlarmManager` | Alarm service (알람 서비스) |
| `getNotificationManager()` | `NotificationManager` | Primary notification service accessor (대표 알림 서비스 접근자) |
| `getPowerManager()` | `PowerManager` | Power service (전원 서비스) |
| `getBluetoothManager()` | `BluetoothManager` | Bluetooth service (블루투스 서비스) |
| `getVibrator()` | `Vibrator` | Legacy vibrator service for API 30 and below (API 30 이하 레거시 진동 서비스) |
| `getVibratorManager()` | `VibratorManager` | API 31+ (`@RequiresApi(S)`) |

### Core Controllers and Info (Core 컨트롤러·정보)

| Function (함수) | Return (반환) | Description (설명) |
|---|---|---|
| `getAlarmController()` | `AlarmController` | Alarm control (알람 제어) |
| `getNotificationController(channel)` | `SimpleNotificationController` | Notification control (알림 제어) |
| `getVibratorController()` | `VibratorController` | Vibration control (진동 제어) |
| `getWifiController()` | `WifiController` | Wi-Fi control (Wi-Fi 제어) |
| `getBatteryStateInfo()` | `BatteryStateInfo` | Battery state access (배터리 상태 조회) |
| `getLocationStateInfo()` | `LocationStateInfo` | Location state access (위치 상태 조회) |

### XML Controllers and Info (XML 컨트롤러·정보)

| Function (함수) | Return (반환) | Description (설명) | Related Docs (관련 상세 문서) |
|---|---|---|---|
| `getSoftKeyboardController()` | `SoftKeyboardController` | Keyboard show/hide/delay control<br>키보드 표시/숨김/지연 제어 | [README_SOFTKEYBOARD_CONTROLLER.md](controller/xml/README_SOFTKEYBOARD_CONTROLLER.md) |
| `getFloatingViewController()` | `FloatingViewController` | Floating view control<br>플로팅 뷰 제어 | [README_FLOATING_VIEW_CONTROLLER.md](controller/xml/README_FLOATING_VIEW_CONTROLLER.md) |
| `getDisplayInfo()` | `DisplayInfo` | Display info access<br>디스플레이 정보 제공 | [README_DISPLAY_INFO.md](info/xml/README_DISPLAY_INFO.md) |
| `Window.getSystemBarController()` | `SystemBarController` | System bar control (cached)<br>시스템 바 제어(캐시) | [README_SYSTEMBAR_CONTROLLER.md](controller/xml/README_SYSTEMBAR_CONTROLLER.md) |
| `Window.destroySystemBarControllerCache()` | `Unit` | Clear system bar controller cache<br>시스템 바 컨트롤러 캐시 제거 | [README_SYSTEMBAR_CONTROLLER.md](controller/xml/README_SYSTEMBAR_CONTROLLER.md) |

<br>

## Why It Matters (중요한 이유)
Provides a compact map of Context and Window extension entry points for `simple_system_manager`.  
> `simple_system_manager`의 Context, Window 확장 함수 진입점과 역할을 한 번에 확인할 수 있습니다.

Extensions provide typed system service access and the primary entry points for controllers and info objects.
> 확장 함수는 타입이 지정된 시스템 서비스 접근과 controller/info 객체의 기본 진입점을 제공합니다.

<br>

## Usage Example (사용 예시)
```kotlin
private val alarmController by lazy { getAlarmController() }
private val connectivityManager by lazy { getConnectivityManager() }

// Use the primary notification service accessor
// (대표 알림 서비스 접근자 사용)
private val notificationManager by lazy { getNotificationManager() }
```

```kotlin
private fun showKeyboard(editText: EditText) {
    getSoftKeyboardController().show(editText)
}
```

```kotlin
private fun setupSystemBar(window: Window) {
    val controller = window.getSystemBarController()
    controller.setStatusBarDarkIcon(true)
    controller.setNavigationBarDarkIcon(false)
}

private fun clearSystemBar(window: Window) {
    window.destroySystemBarControllerCache()
}
```

<br>

## Notes (주의사항)
- `getNotificationManager()` is the only public notification service accessor.
  > `getNotificationManager()`가 유일한 공개 알림 서비스 접근자입니다.
- Core no longer exposes `getWindowManager()` or `getInputMethodManager()`. Consumers that need the raw Android services should use `Context.getSystemService(Class)`.
  > Core는 `getWindowManager()`와 `getInputMethodManager()`를 더 이상 공개하지 않습니다. Android 원시 서비스가 필요한 소비자는 `Context.getSystemService(Class)`를 사용하세요.
- `getVibrator()` targets API 30 and below; use `getVibratorManager()` on API 31+.
  > `getVibrator()`는 API 30 이하용이며 API 31 이상에서는 `getVibratorManager()`를 사용합니다.
- Floating view 계열은 `SYSTEM_ALERT_WINDOW` 권한이 필요할 수 있습니다.
- Floating controller의 Boolean 반환은 실제 `WindowManager` 적용 성공 여부를 기준으로 합니다.
- `removeAllFloatingView()`는 `first-failure-stop` 전략이며 일반 호출에서는 부분 정리 상태가 남을 수 있습니다.
- System bar controller는 `Window` 단위로 캐시되므로 사용 종료 시 `destroySystemBarControllerCache()` 호출을 권장합니다.
- `window.getSystemBarController()`와 `window.destroySystemBarControllerCache()`는 `@MainThread` 계약이며 Debug 빌드에서는 오프 메인스레드 호출 시 `IllegalStateException`으로 즉시 실패합니다.
- `clearTint`, `applyWindowInsetsAsPadding`, `bindLifecycleObserver`/`unbindLifecycleObserver`는 View 확장 범위이므로 `docs/readme/README_EXTENSIONS.md`를 참조하십시오.

<br>

## Related Docs (관련 문서)
- Next major migration: [README_SYSTEM_MANAGER_MIGRATION.md](README_SYSTEM_MANAGER_MIGRATION.md)
- Core controller docs: `docs/readme/system_manager/controller/core/`
- Core info docs: `docs/readme/system_manager/info/core/`
- Controller docs: `docs/readme/system_manager/controller/xml/`
- Info docs: `docs/readme/system_manager/info/xml/`
- Feature docs: `simple_system_manager/docs/feature/system_manager/`
