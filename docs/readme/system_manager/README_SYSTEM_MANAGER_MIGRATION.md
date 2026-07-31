# System Manager Next Major Migration Guide
> **System Manager 다음 메이저 마이그레이션 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_system_manager`
- **Applies to**: the next major source state (다음 메이저 소스 상태)
- **Release version**: not fixed (릴리스 버전 미확정)

<br>

## Overview (개요)
The next major source state reduces the public API surface without changing the primary controller and info behavior. Five approved breaking changes are included.
> 다음 메이저 소스 상태는 대표 Controller·Info 동작을 유지하면서 공개 API 표면을 줄입니다. 승인된 Breaking Change 5개가 포함됩니다.

The published `0.5.1` coordinates are not renamed by this document. Apply this guide when adopting the future major release or building the next-major source state directly.
> 이 문서는 현재 배포된 `0.5.1` 좌표를 변경하지 않습니다. 향후 메이저 릴리스를 적용하거나 다음 메이저 소스 상태를 직접 빌드할 때 이 가이드를 사용하세요.

<br>

## Migration Summary (이전 요약)

| Previous API (기존 API) | Migration (이전 방법) |
| --- | --- |
| `Context.getSystemNotificationManager()` | Use `Context.getNotificationManager()`.<br>`Context.getNotificationManager()`를 사용합니다. |
| Core `Context.getWindowManager()` | Use `context.getSystemService(WindowManager::class.java)` when the raw Android service is required.<br>Android 원시 서비스가 필요하면 `context.getSystemService(WindowManager::class.java)`를 사용합니다. |
| Core `Context.getInputMethodManager()` | Use `context.getSystemService(InputMethodManager::class.java)` when the raw Android service is required.<br>Android 원시 서비스가 필요하면 `context.getSystemService(InputMethodManager::class.java)`를 사용합니다. |
| `PowerProfile.getBatteryCapacity()` | Use `BatteryStateInfo.getTotalCapacity()`.<br>`BatteryStateInfo.getTotalCapacity()`를 사용합니다. |
| `PowerProfile.getAveragePower()`, `PowerProfileVO` | No stable public replacement.<br>안정적인 공개 대체 API가 없습니다. |
| `TelephonyCallbackManager` | Use `TelephonyInfo` and its registration, StateFlow, per-slot setter, and unregister APIs.<br>`TelephonyInfo`의 등록·StateFlow·슬롯별 setter·해제 API를 사용합니다. |
| `CommonTelephonyCallback` raw listeners | No direct replacement; register through `TelephonyInfo`.<br>직접 대체 API가 없으며 `TelephonyInfo`를 통해 등록합니다. |
| `kr.open.library.simple_ui.system_manager.BuildConfig` | Use the consuming app or module's own build configuration.<br>소비 앱 또는 모듈 자체 빌드 설정을 사용합니다. |

<br>

## 1. Notification Accessor (알림 접근자)

`getSystemNotificationManager()` was a compatibility alias for `getNotificationManager()` and has been removed. The returned Android service and notification controller behavior are unchanged.
> `getSystemNotificationManager()`는 `getNotificationManager()`의 호환 별칭이었으며 제거되었습니다. 반환되는 Android 서비스와 알림 Controller 동작은 변경되지 않았습니다.

```kotlin
val notificationManager = context.getNotificationManager()
```

<br>

## 2. Core and XML Service Boundary (Core/XML 서비스 경계)

The Core `WindowManager` and `InputMethodManager` accessors were removed because library use is owned by the XML layer. `DisplayInfo`, `FloatingViewController`, and `SoftKeyboardController` continue to acquire the same Android services internally.
> Core의 `WindowManager`, `InputMethodManager` 접근자는 라이브러리 사용 책임이 XML 계층에 있으므로 제거되었습니다. `DisplayInfo`, `FloatingViewController`, `SoftKeyboardController`는 내부에서 동일한 Android 서비스를 계속 획득합니다.

Consumers using those XML controllers do not need source changes. Only code that called the removed Core accessors directly must use Android's typed service lookup.
> 해당 XML Controller를 사용하는 소비자는 소스 수정이 필요하지 않습니다. 제거된 Core 접근자를 직접 호출한 코드만 Android 타입 기반 서비스 조회로 변경합니다.

```kotlin
val windowManager = context.getSystemService(WindowManager::class.java)
val inputMethodManager = context.getSystemService(InputMethodManager::class.java)
```

<br>

## 3. Battery PowerProfile (배터리 PowerProfile)

`PowerProfile` and `PowerProfileVO` are now internal. Battery capacity consumers should use `BatteryStateInfo.getTotalCapacity()`.
> `PowerProfile`과 `PowerProfileVO`는 내부화되었습니다. 배터리 총 용량 소비자는 `BatteryStateInfo.getTotalCapacity()`를 사용하세요.

The internal fallback remains PowerProfile reflection → charge-counter estimation → error value. Individual average-power metrics do not receive a new public replacement.
> 내부 폴백은 PowerProfile 리플렉션 → chargeCounter 추정 → 오류 값 순서를 유지합니다. 개별 평균 전력 지표에는 새 공개 대체 API를 제공하지 않습니다.

<br>

## 4. Telephony Callback Implementation (Telephony 콜백 구현)

`CommonTelephonyCallback` and `TelephonyCallbackManager` are now internal. SDK branching, permission handling, callback lifecycle, StateFlow updates, and per-slot behavior remain owned by `TelephonyInfo`.
> `CommonTelephonyCallback`과 `TelephonyCallbackManager`는 내부화되었습니다. SDK 분기, 권한 처리, 콜백 생명주기, StateFlow 갱신, 슬롯별 동작은 계속 `TelephonyInfo`가 담당합니다.

| Previous API (기존 API) | Replacement API (대체 API) |
| --- | --- |
| `TelephonyCallbackManager(context)` | `TelephonyInfo(context)` |
| `registerSimpleCallback(...)` | `registerCallback(...)` |
| `unregisterSimpleCallback()` | `unregisterCallback()` |
| `registerAdvancedCallbackFromDefaultUSim(...)` | `registerTelephonyCallBackFromDefaultUSim(...)` |
| `registerAdvancedCallback(...)` | `registerTelephonyCallBack(...)` |
| `unregisterAdvancedCallback(slotIndex)` | `unregisterCallBack(slotIndex)` |
| Callback manager StateFlow and per-slot setters | The same consumer-facing properties and setters on `TelephonyInfo`<br>`TelephonyInfo`의 동일 소비자용 프로퍼티와 setter |

<br>

## 5. Library BuildConfig (라이브러리 BuildConfig)

`simple_system_manager` no longer generates its own `BuildConfig`. Do not use another Simple UI module's `BuildConfig` as a replacement.
> `simple_system_manager`는 자체 `BuildConfig`를 더 이상 생성하지 않습니다. 다른 Simple UI 모듈의 `BuildConfig`로 대체하지 마세요.

When a consumer needs a build flag, use the app or owning module's generated configuration or a consumer-defined build constant.
> 소비자에게 빌드 플래그가 필요하면 앱 또는 소유 모듈에서 생성된 설정이나 소비자가 정의한 빌드 상수를 사용하세요.

```kotlin
if (com.example.app.BuildConfig.DEBUG) {
    // App-specific debug behavior
    // 앱 전용 디버그 동작
}
```

<br>

## Unchanged Public Contracts (변경하지 않은 공개 계약)

- `NetworkBase` remains a public open base class.
- `NetworkCapabilitiesData : NetworkBase` remains unchanged.
- `NetworkLinkPropertiesData : NetworkBase` remains unchanged.
- Primary Controller·Info entry points and the five XML extension APIs remain public.
> - `NetworkBase`는 공개 `open` 기반 클래스로 유지됩니다.
> - `NetworkCapabilitiesData : NetworkBase` 상속은 유지됩니다.
> - `NetworkLinkPropertiesData : NetworkBase` 상속은 유지됩니다.
> - 대표 Controller·Info 진입점과 XML 확장 API 5개는 계속 공개됩니다.

<br>

## Consumer Checklist (소비자 체크리스트)

- Search source code for the removed APIs and implementation types listed above.
- Update only modules that directly reference a removed API; normal `TelephonyInfo`, `BatteryStateInfo`, and XML Controller consumers require no dependency declaration change.
- Use the app or owning module's build flags instead of the removed library `BuildConfig`.
- Rebuild the app against the next-major artifact and run device-specific telephony scenarios when applicable.
> - 위에 나열된 제거 API와 구현 타입을 소스 코드에서 검색합니다.
> - 제거 API를 직접 참조하는 모듈만 수정합니다. 일반적인 `TelephonyInfo`, `BatteryStateInfo`, XML Controller 소비자는 의존성 선언을 변경할 필요가 없습니다.
> - 제거된 라이브러리 `BuildConfig` 대신 앱 또는 소유 모듈의 빌드 플래그를 사용합니다.
> - 다음 메이저 아티팩트로 앱을 다시 빌드하고 필요한 경우 Telephony 실기기 시나리오를 실행합니다.

<br>

## Related Docs (관련 문서)
- [System Manager Extensions](README_SYSTEM_MANAGER_EXTENSIONS.md)
- [Battery Info](info/core/README_BATTERY_INFO.md)
- [Telephony Info](info/core/README_TELEPHONY_INFO.md)
- [Network Info](info/core/README_NETWORK_INFO.md)
