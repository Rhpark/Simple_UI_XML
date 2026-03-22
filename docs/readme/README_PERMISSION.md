# Permission Guide for Simple UI
> **Simple UI 권한 가이드**

## Overview (개요)
This document explains the permission-related architecture of Simple UI, covering:
- permission inspection helpers in `simple_core`
- permission request flow in `simple_xml`
- feature-specific permission requirements used by `system_manager`

> 이 문서는 Simple UI의 권한 관련 구조를 설명하며, 다음 범위를 다룹니다.
> - `simple_core`의 권한 판별/헬퍼
> - `simple_xml`의 권한 요청 흐름
> - `system_manager` 기능에서 사용하는 권한 요구사항

<br></br>

## Permission Architecture (권한 구조)

### simple_core
- Permission inspection helpers
- Permission classification
- Manifest-declared permission reading

> - 권한 판별 헬퍼
> - 권한 분류
> - Manifest 선언 권한 조회

### simple_xml
- Activity/Fragment/Dialog-based permission requesting
- Lifecycle-aware request coordination
- Saved-state restore/save support

> - Activity/Fragment/Dialog 기반 권한 요청
> - 생명주기 인식 요청 조정
> - 상태 저장/복원 지원

### system_manager
- Uses the above layers and defines feature-specific permission combinations

> - 위 두 레이어를 사용하며, 기능별 권한 조합을 정의합니다.

<br></br>

## simple_core Permission Helpers (simple_core 권한 헬퍼)

**Main APIs (주요 API):**
- `hasPermission(permission)`
- `hasPermissions(vararg permissions)`
- `hasPermissions(vararg permissions) { doWork }`
- `remainPermissions(permissions)`
- `hasUsageStatsPermission()`
- `hasAccessibilityServicePermission()`
- `hasNotificationListenerPermission()`
- `getPermissionProtectionLevel(permission)`
- `getPermissionBaseProtectionLevel(permission)`
- `readDeclaredManifestPermissions()`

> **주요 API**
> - `hasPermission(permission)`
> - `hasPermissions(vararg permissions)`
> - `hasPermissions(vararg permissions) { doWork }`
> - `remainPermissions(permissions)`
> - `hasUsageStatsPermission()`
> - `hasAccessibilityServicePermission()`
> - `hasNotificationListenerPermission()`
> - `getPermissionProtectionLevel(permission)`
> - `getPermissionBaseProtectionLevel(permission)`
> - `readDeclaredManifestPermissions()`

**Behavior summary (동작 요약):**
- Dangerous permissions are checked through runtime APIs.
- Normal permissions are treated as granted by design when declared in the manifest.
- Special app access permissions such as overlay, usage stats, notification listener, and accessibility are handled through dedicated checks.
- Manifest-declared permission reading is centralized through `Context.readDeclaredManifestPermissions()`.

> **동작 요약**
> - 위험 권한은 런타임 API를 통해 확인합니다.
> - 일반 권한은 Manifest에 선언되어 있으면 설계상 허용된 것으로 간주합니다.
> - 오버레이, 사용 기록, 알림 접근, 접근성 같은 특수 앱 액세스 권한은 전용 체크 방식으로 처리합니다.
> - Manifest 선언 권한 조회는 `Context.readDeclaredManifestPermissions()`로 공통화되어 있습니다.

**Usage example (사용 예시):**
```kotlin
if (context.hasPermission(Manifest.permission.CAMERA)) {
    openCamera()
}

val denied = context.remainPermissions(
    listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ),
)

if (denied.isEmpty()) {
    startLocationTracking()
}
```

<br></br>

## simple_xml Permission Request Flow (simple_xml 권한 요청 흐름)

Simple UI provides `PermissionRequester` through Activity, Fragment, and Dialog base classes.

> Simple UI는 Activity, Fragment, Dialog의 base class를 통해 `PermissionRequester`를 제공합니다.

**Key capabilities (핵심 기능):**
- `requestPermission(...)`
- `requestPermissions(...)`
- `onDeniedResult`
- `onRationaleNeeded`
- `onNavigateToSettings`
- `defer(policy)` for asynchronous rationale/settings UI
- `restoreState(savedInstanceState)`
- `saveState(outState)`

> **핵심 기능**
> - `requestPermission(...)`
> - `requestPermissions(...)`
> - `onDeniedResult`
> - `onRationaleNeeded`
> - `onNavigateToSettings`
> - rationale/settings UI의 비동기 전환을 위한 `defer(policy)`
> - `restoreState(savedInstanceState)`
> - `saveState(outState)`

**Request example (요청 예시):**
```kotlin
requestPermissions(
    permissions = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.SYSTEM_ALERT_WINDOW,
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            openCameraWithOverlay()
        } else {
            Logx.d("Permission", "Denied: $deniedResults")
        }
    },
    onRationaleNeeded = { request ->
        // Synchronous choice
        request.proceed()
    },
    onNavigateToSettings = { request ->
        // Asynchronous UI example
        request.defer() // default: CANCEL_ON_STOP
        showSettingsGuideDialog(
            onContinue = { request.proceed() },
            onCancel = { request.cancel() },
        )
    },
)
```

**What the base classes already do for you (base class가 자동으로 처리하는 것):**
- `RootActivity`, `RootFragment`, `RootDialogFragment` create `PermissionRequester` internally.
- They automatically call `restoreState(savedInstanceState)` / `saveState(outState)`.
- You call `requestPermissions(...)` directly from the screen class without manually wiring ActivityResult registration.

> **base class가 자동으로 처리하는 것**
> - `RootActivity`, `RootFragment`, `RootDialogFragment`가 내부적으로 `PermissionRequester`를 생성합니다.
> - `restoreState(savedInstanceState)` / `saveState(outState)`를 자동으로 연결합니다.
> - ActivityResult 등록을 직접 구성하지 않고 화면 클래스에서 `requestPermissions(...)`를 바로 호출할 수 있습니다.

<br></br>

**After process restore — consuming orphaned denied results (프로세스 복원 후 — orphaned 거부 결과 처리):**

If a permission request was in progress when the process was killed, the denied result cannot be delivered
via the original callback (lambdas are not serializable). Call `consumeOrphanedDeniedResults()` in `onCreate`
to retrieve these results.

> 프로세스가 종료된 시점에 권한 요청이 진행 중이었다면, 원래 콜백(람다)은 직렬화할 수 없으므로
> 결과를 전달받을 수 없습니다. `onCreate`에서 `consumeOrphanedDeniedResults()`를 호출해 해당 결과를 처리하세요.

```kotlin
// Activity / Fragment / DialogFragment 모두 동일한 방식으로 사용
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val orphaned = consumeOrphanedDeniedResults()
    if (orphaned.isNotEmpty()) {
        // 복원된 거부 결과 처리 (예: 설정 안내 UI 표시)
        // Handle restored denied results (e.g., show settings guidance UI)
        orphaned.forEach { result ->
            result.deniedResults.forEach { item ->
                // item.permission, item.result 로 분기 처리
            }
        }
    }
}
```

<br></br>

## Lifecycle and Host Rules (생명주기 및 호스트 규칙)

- Activity permission requests must be made after `super.onCreate()`.
- Fragment/Dialog permission requests must be made only after the host is attached (`isAdded == true`).
- `PermissionRequester` supports state restore/save for configuration changes and process recovery flows.
- If a rationale/settings callback returns without calling `proceed()`, `cancel()`, or `defer(policy)`, the flow is auto-cancelled.
- `defer()` uses `CANCEL_ON_STOP` by default, and `CANCEL_ON_DESTROY` can be used when the deferred UI must survive `onStop`.

> - Activity 권한 요청은 반드시 `super.onCreate()` 이후에 호출해야 합니다.
> - Fragment/Dialog 권한 요청은 반드시 host attach 이후(`isAdded == true`)에 호출해야 합니다.
> - `PermissionRequester`는 구성 변경 및 프로세스 복원 흐름을 위해 상태 저장/복원을 지원합니다.
> - rationale/settings 콜백이 `proceed()`, `cancel()`, `defer(policy)` 없이 반환되면 흐름은 자동 취소됩니다.
> - `defer()`의 기본 정책은 `CANCEL_ON_STOP`이며, `onStop` 이후에도 유지가 필요하면 `CANCEL_ON_DESTROY`를 사용할 수 있습니다.

<br></br>

## Denied Result Model (거부 결과 모델)

Simple UI can return more than a simple granted/denied result.

> Simple UI는 단순 granted/denied를 넘는 결과를 반환할 수 있습니다.

**Important result types (중요 결과 유형):**
- `DENIED`
- `PERMANENTLY_DENIED`
- `MANIFEST_UNDECLARED`
- `EMPTY_REQUEST`
- `NOT_SUPPORTED`
- `FAILED_TO_LAUNCH_SETTINGS`
- `LIFECYCLE_NOT_READY`

> **중요 결과 유형**
> - `DENIED`
> - `PERMANENTLY_DENIED`
> - `MANIFEST_UNDECLARED`
> - `EMPTY_REQUEST`
> - `NOT_SUPPORTED`
> - `FAILED_TO_LAUNCH_SETTINGS`
> - `LIFECYCLE_NOT_READY`

**Why this matters (왜 중요한가):**
- You can distinguish invalid input from a user denial.
- You can detect lifecycle misuse without guessing.
- You can handle unsupported permissions and settings-launch failures explicitly.

> **왜 중요한가**
> - 잘못된 입력과 실제 사용자 거부를 구분할 수 있습니다.
> - 생명주기 사용 오류를 추측하지 않고 결과로 확인할 수 있습니다.
> - 지원되지 않는 권한이나 설정 화면 실행 실패를 명시적으로 처리할 수 있습니다.

<br></br>

## Sample Usage in the App Module (app 모듈 샘플 사용 예시)

The sample app demonstrates these request flows in [PermissionsActivity.kt](../../app/src/main/java/kr/open/library/simpleui_xml/permission/PermissionsActivity.kt):
- single dangerous permission request (`CAMERA`)
- single dangerous permission request (`ACCESS_FINE_LOCATION`)
- dangerous + special mixed request (`WRITE_EXTERNAL_STORAGE` + `SYSTEM_ALERT_WINDOW`)
- special-only request (`SYSTEM_ALERT_WINDOW`)
- multiple special permissions request (`SYSTEM_ALERT_WINDOW` + `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`)

> 샘플 앱은 [PermissionsActivity.kt](../../app/src/main/java/kr/open/library/simpleui_xml/permission/PermissionsActivity.kt) 에서 다음 요청 흐름을 보여줍니다.
> - 단일 위험 권한 요청 (`CAMERA`)
> - 단일 위험 권한 요청 (`ACCESS_FINE_LOCATION`)
> - 위험 권한 + 특수 권한 혼합 요청 (`WRITE_EXTERNAL_STORAGE` + `SYSTEM_ALERT_WINDOW`)
> - 특수 권한 단독 요청 (`SYSTEM_ALERT_WINDOW`)
> - 복수 특수 권한 요청 (`SYSTEM_ALERT_WINDOW` + `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`)

<br></br>

## System Manager Permission Matrix (system_manager 권한 매트릭스)

The table below summarizes feature-level permission requirements for `system_manager` consumers.

> 아래 표는 `system_manager` 기능 소비자 관점에서 필요한 권한 조합을 정리합니다.

| Info (정보)                    | Required Permissions (필수 권한)                                         | Runtime Permission (런타임 권한) | No Permission Required (권한 불필요) |
|:-----------------------------|:---------------------------------------------------------------------|:--:|:--:|
| **BatteryStateInfo**         | `BATTERY_STATS` (system-only, optional)                              | - | ✅ |
| **DisplayInfo**              | -                                                                    | - | ✅ |
| **LocationStateInfo**        | `ACCESS_FINE_LOCATION`<br>`ACCESS_COARSE_LOCATION`                   | ✅ | - |
| **SimInfo**                  | `READ_PHONE_STATE`<br>`READ_PHONE_NUMBERS`<br>`ACCESS_FINE_LOCATION` | ✅ | - |
| **TelephonyInfo**            | `READ_PHONE_STATE`<br>`READ_PHONE_NUMBERS`<br>`ACCESS_FINE_LOCATION` | ✅ | - |
| **NetworkConnectivityInfo**  | `ACCESS_NETWORK_STATE`<br>`ACCESS_WIFI_STATE` (optional / 선택)        | - | - |

### Battery State Info - `BATTERY_STATS` (System Only, Optional) (배터리 상태 정보 - 시스템 전용, 선택)
BatteryStateInfo does not enforce `android.permission.BATTERY_STATS`. This is an optional system/privileged permission.
> BatteryStateInfo는 `android.permission.BATTERY_STATS`를 강제 검증하지 않습니다. 이는 선택적 시스템/특권 권한입니다.

See feature doc: [README_BATTERY_INFO.md](system_manager/info/core/README_BATTERY_INFO.md)

### Display Info - No Permission Required (표시 정보 - 권한 불필요)
Display information queries do not require permissions.
> 디스플레이 정보 조회는 권한이 필요하지 않습니다.

See feature doc: [README_DISPLAY_INFO.md](system_manager/info/xml/README_DISPLAY_INFO.md)

### Location State Info - Location Permission Required (위치 상태 정보 - 위치 권한 필수)
LocationStateInfo requires both fine and coarse location permissions for the supported runtime flow.
> LocationStateInfo는 지원되는 런타임 흐름에서 fine/coarse 위치 권한을 함께 요구합니다.

See feature doc: [README_LOCATION_INFO.md](system_manager/info/core/README_LOCATION_INFO.md)

### SIM Info - Phone/Location Permissions Required (SIM 정보 - 전화/위치 권한 필수)
SimInfo requires phone state, phone numbers, and fine location permissions.
> SimInfo는 전화 상태, 전화번호, fine location 권한을 요구합니다.

See feature doc: [README_SIM_INFO.md](system_manager/info/core/README_SIM_INFO.md)

### Telephony Info - Phone/Location Permissions Required (통신 정보 - 전화/위치 권한 필수)
TelephonyInfo requires phone state, phone numbers, and fine location permissions.
> TelephonyInfo는 전화 상태, 전화번호, fine location 권한을 요구합니다.

See feature doc: [README_TELEPHONY_INFO.md](system_manager/info/core/README_TELEPHONY_INFO.md)

### Network Connectivity Info - Network State Permissions (네트워크 연결 정보 - 네트워크 상태 권한)
NetworkConnectivityInfo requires network state permissions, and Wi-Fi state is optional.
> NetworkConnectivityInfo는 네트워크 상태 권한이 필요하며, Wi-Fi 상태 권한은 선택입니다.

See feature doc: [README_NETWORK_INFO.md](system_manager/info/core/README_NETWORK_INFO.md)

<br></br>

## Permission Types Summary (권한 타입별 정리)

| Permission Type (권한 타입)          | Example Permissions (예시 권한)                                                                           | How Simple UI treats it (Simple UI 처리 방식) |
|:---------------------------------|:------------------------------------------------------------------------------------------------------|:--|
| **Normal Permissions**           | `ACCESS_NETWORK_STATE`<br>`ACCESS_WIFI_STATE`                                                         | Manifest 선언 시 허용된 것으로 간주 |
| **Dangerous Permissions**        | `CAMERA`<br>`ACCESS_FINE_LOCATION`<br>`ACCESS_COARSE_LOCATION`<br>`READ_PHONE_STATE`<br>`READ_PHONE_NUMBERS` | 런타임 요청 대상 |
| **Special Permissions**          | `SYSTEM_ALERT_WINDOW`<br>`WRITE_SETTINGS`<br>`PACKAGE_USAGE_STATS`<br>`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | 설정 화면 이동 또는 전용 체크로 처리 |
| **Role Permissions**             | `android.app.role.*`                                                                                  | `RoleManager` 기반 흐름으로 처리 |
| **Signature/System Permissions** | `BATTERY_STATS`                                                                                       | 일반 앱 요청 대상으로는 지원하지 않거나 강제하지 않음 |

<br></br>

## Permission Troubleshooting Checklist (권한 점검 체크리스트)

- Check whether the permission is declared in `AndroidManifest.xml`.
- Verify whether the permission is dangerous, normal, special, or role-based.
- Make sure permission requests are called at the correct lifecycle timing.
- Check denied result types such as `MANIFEST_UNDECLARED`, `EMPTY_REQUEST`, and `LIFECYCLE_NOT_READY`.
- Use `onRationaleNeeded` and `onNavigateToSettings` when the flow requires explanation or settings navigation.
- In those callbacks, always finish the decision with `proceed()`, `cancel()`, or `defer(policy)`.
- Use `defer(CANCEL_ON_DESTROY)` only when the deferred UI must survive `onStop`; otherwise keep the default `CANCEL_ON_STOP`.
- Use `simple_core` helpers such as `hasPermission()` and `remainPermissions()` when you need pre-check logic.

> - `AndroidManifest.xml`에 권한이 선언되어 있는지 확인하세요.
> - 권한이 dangerous, normal, special, role 중 어떤 유형인지 확인하세요.
> - 권한 요청이 올바른 생명주기 시점에서 호출되는지 확인하세요.
> - `MANIFEST_UNDECLARED`, `EMPTY_REQUEST`, `LIFECYCLE_NOT_READY` 같은 결과 유형을 확인하세요.
> - 설명 UI나 설정 이동이 필요한 경우 `onRationaleNeeded`, `onNavigateToSettings`를 사용하세요.
> - 해당 콜백에서는 반드시 `proceed()`, `cancel()`, `defer(policy)` 중 하나로 결정을 마무리하세요.
> - `defer(CANCEL_ON_DESTROY)`는 `onStop` 이후에도 유지가 필요할 때만 사용하고, 그 외에는 기본값 `CANCEL_ON_STOP`을 유지하세요.
> - 사전 점검이 필요하면 `hasPermission()`, `remainPermissions()` 같은 `simple_core` 헬퍼를 활용하세요.

<br></br>
