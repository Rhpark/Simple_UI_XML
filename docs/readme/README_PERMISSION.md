# Permission Guide for Simple UI
> **Simple UI 권한 가이드**

## Overview (개요)
This document summarizes permission requirements and troubleshooting for Simple UI features, with a focus on System Manager Info.  
> 이 문서는 Simple UI 기능의 권한 요구사항과 점검 방법을 정리하며, System Manager Info를 중심으로 설명합니다.

<br></br>

## Permission Requirements Summary (권한 요구사항 요약)
The table below lists required permissions by Info type and whether runtime permission is needed.  
> 아래 표는 Info 유형별 필수 권한과 런타임 권한 여부를 정리합니다.

| Info (정보)                    | Required Permissions (필수 권한)                                         | Runtime Permission (런타임 권한) | No Permission Required (권한 불필요) |
|:-----------------------------|:---------------------------------------------------------------------|:--:|:--:|
| **BatteryStateInfo**         | `BATTERY_STATS` (system-only, optional)                              | - | ✅ |
| **DisplayInfo**              | -                                                                    | - | ✅ |
| **LocationStateInfo**        | `ACCESS_FINE_LOCATION`<br>`ACCESS_COARSE_LOCATION`                   | ✅ | - |
| **SimInfo**                  | `READ_PHONE_STATE`<br>`READ_PHONE_NUMBERS`<br>`ACCESS_FINE_LOCATION` | ✅ | - |
| **TelephonyInfo**            | `READ_PHONE_STATE`<br>`READ_PHONE_NUMBERS`<br>`ACCESS_FINE_LOCATION` | ✅ | - |
| **NetworkConnectivityInfo**  | `ACCESS_NETWORK_STATE`<br>`ACCESS_WIFI_STATE` (optional / 선택)        | - | - |

<br></br>

## Permission Policy (권한 정책)
- **Runtime/Special only** – BaseSystemService validates runtime/special permissions only; non-dangerous permissions are treated as granted by design.
- **Default value fallback** – If runtime/special permission is missing or an error occurs, tryCatchSystemManager() returns a default value and logs a warning.
- **Refresh after grant** – Call BaseSystemService.refreshPermissions() after permissions are granted to update the internal cache.
- **Shared manifest permission reader** – Manifest-declared permission loading is centralized in `Context.readDeclaredManifestPermissions()` and shared by BaseSystemService and PermissionClassifier for consistent behavior.
> - **런타임/특수 권한만 검증** – BaseSystemService는 런타임/특수 권한만 검증하며, non-dangerous 권한은 설계상 허용된 것으로 간주합니다.
> - **기본값 반환** – 런타임/특수 권한이 없거나 오류가 발생하면 tryCatchSystemManager()가 기본값을 반환하고 경고를 기록합니다.
> - **권한 갱신** – 권한 허용 후 BaseSystemService.refreshPermissions()를 호출해 내부 캐시를 갱신하세요.
> - **매니페스트 권한 조회 공통화** – 매니페스트 선언 권한 조회는 `Context.readDeclaredManifestPermissions()`로 일원화되어 BaseSystemService와 PermissionClassifier가 동일 동작을 사용합니다.

<br></br>

## Permission Troubleshooting Checklist (권한 점검 체크리스트)
- **Runtime/Special only** – BaseSystemService validates runtime/special permissions only; non-dangerous permissions are treated as granted by design.
- **Check Manifest declaration** – Verify all required permissions are added to AndroidManifest.xml.
- **Runtime request** – Request dangerous permissions with requestPermissions() or your own ActivityResult logic.
- **Call refreshPermissions()** – After granting permission, call BaseSystemService.refreshPermissions() and Simple UI will immediately reflect the new state.
- **Check Logx** – If runtime/special permission is missing, tryCatchSystemManager() returns a default value and logs a warning to Logx.
> - **런타임/특수 권한만 검증** – BaseSystemService는 런타임/특수 권한만 검증하며, non-dangerous 권한은 설계상 허용된 것으로 간주합니다.
> - **Manifest 선언 확인** – 필요한 모든 권한을 `AndroidManifest.xml`에 추가했는지 확인하세요.
> - **런타임 요청** – 위험 권한은 `requestPermissions()` 또는 자체 ActivityResult 로직으로 요청합니다.
> - **refreshPermissions() 호출** – 권한 허용 후 `BaseSystemService.refreshPermissions()`를 호출하면 Simple UI가 즉시 새 상태를 반영합니다.
> - **Logx 확인** – 런타임/특수 권한이 없으면 `tryCatchSystemManager()`가 기본값을 반환하며 Logx에 경고를 남깁니다.

<br></br>

## Info-specific Permission Notes (Info별 권한 참고)
### Battery State Info - `BATTERY_STATS` (System Only, Optional) (배터리 상태 정보 - 시스템 전용, 선택)
BatteryStateInfo does not enforce `android.permission.BATTERY_STATS`. BaseSystemService validates runtime/special permissions only.  
> BatteryStateInfo는 `android.permission.BATTERY_STATS`를 강제 검증하지 않습니다. BaseSystemService는 런타임/특수 권한만 검증합니다.

See feature doc: [README_BATTERY_INFO.md](system_manager/info/core/README_BATTERY_INFO.md)

### Display Info - No Permission Required (표시 정보 - 권한 불필요)
Display information queries do not require permissions.  
> 디스플레이 정보 조회는 권한이 필요하지 않습니다.

See feature doc: [README_DISPLAY_INFO.md](system_manager/info/xml/README_DISPLAY_INFO.md)

### Location State Info - Location Permission Required (위치 상태 정보 - 위치 권한 필수)
LocationStateInfo requires location permissions for runtime access.  
> LocationStateInfo는 런타임 위치 권한이 필요합니다.

See feature doc: [README_LOCATION_INFO.md](system_manager/info/core/README_LOCATION_INFO.md)

### SIM Info - Phone/Location Permissions Required (SIM 정보 - 전화/위치 권한 필수)
SimInfo requires phone state/number and fine location permissions.  
> SimInfo는 전화 상태/번호 및 위치 권한이 필요합니다.

See feature doc: [README_SIM_INFO.md](system_manager/info/core/README_SIM_INFO.md)

### Telephony Info - Phone/Location Permissions Required (통신 정보 - 전화/위치 권한 필수)
TelephonyInfo requires phone state/number and fine location permissions.  
> TelephonyInfo는 전화 상태/번호 및 위치 권한이 필요합니다.

See feature doc: [README_TELEPHONY_INFO.md](system_manager/info/core/README_TELEPHONY_INFO.md)

### Network Connectivity Info - Network State Permissions (네트워크 연결 정보 - 네트워크 상태 권한)
NetworkConnectivityInfo requires network state permissions, and Wi-Fi state is optional.  
> NetworkConnectivityInfo는 네트워크 상태 권한이 필요하며, Wi-Fi 상태 권한은 선택입니다.

See feature doc: [README_NETWORK_INFO.md](system_manager/info/core/README_NETWORK_INFO.md)

<br></br>

## Permission Types Summary (권한 타입별 정리)
| Permission Type (권한 타입)          | Permissions (권한)                                                                                 | Request Method (요청 방식)                                                                  | Used by Info (사용처) |
|:---------------------------------|:-------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------|:--|
| **Normal Permissions**           | `ACCESS_NETWORK_STATE`<br>`ACCESS_WIFI_STATE`                                                    | Auto-granted with Manifest declaration<br>Manifest 선언 시 자동 허용                           | NetworkConnectivityInfo |
| **Dangerous Permissions**        | `ACCESS_FINE_LOCATION`<br>`ACCESS_COARSE_LOCATION`<br>`READ_PHONE_STATE`<br>`READ_PHONE_NUMBERS` | Runtime permission request required<br>런타임 권한 요청 필요                                     | LocationStateInfo<br>SimInfo<br>TelephonyInfo |
| **Signature/System Permissions** | `BATTERY_STATS`                                                                                  | System/privileged apps only (library does not enforce)<br>시스템/특권 앱 전용 (라이브러리에서 강제하지 않음) | BatteryStateInfo (optional / 선택) |

<br></br>

## Permission Request Tips (권한 요청 팁)
### Start with Required Permissions (필수 권한으로 시작)
```kotlin
// LocationStateInfo usage example - Required permissions
// (LocationStateInfo 사용 예시 - 필수 권한)
requestPermissions(
    permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // Start location tracking (위치 추적 시작)
            locationInfo.registerStart(
                coroutineScope = lifecycleScope,
                locationProvider = LocationManager.NETWORK_PROVIDER,
                updateCycleTime = 5000L,
                minDistanceM = 0.1f
            )
        }
    },
)
```

### Request Additional Permissions When Needed (필요 시 추가 권한 요청)
```kotlin
// When more precise location is needed (더 정확한 위치가 필요할 때)
requestPermissions(
    permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION  // Precise location (정확한 위치)
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // Use GPS-based location (GPS 기반 위치 사용)
            locationInfo.registerStart(
                coroutineScope = lifecycleScope,
                locationProvider = LocationManager.GPS_PROVIDER,
                updateCycleTime = 5000L,
                minDistanceM = 0.1f
            )
        }
    },
)
```

### Simple UI's Automatic Permission Handling (Simple UI의 자동 권한 처리)
```kotlin
// Request multiple permissions at once (여러 권한을 한 번에 요청)
requestPermissions(
    permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // All permissions granted (모든 권한 허용됨)
            startLocationTracking()
            loadSimInfo()
        } else {
            // Handle partial permission grant (일부만 허용된 경우 처리)
            Log.d("Permission", "Denied Permissions (거부된 권한): ${deniedResults.map { it.permission }}")
        }
    },
)
```

<br></br>
