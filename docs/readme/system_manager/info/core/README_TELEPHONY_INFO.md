# Telephony Info vs Plain Android - Complete Comparison Guide
> **Telephony Info vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_system_manager` (system manager module / system manager 전용 모듈)
- **Package**: `kr.open.library.simple_ui.system_manager.core.info.network.telephony`

<br>

## Overview (개요)
Provides telephony helpers, network type parsing, and real-time callbacks with API compatibility.  
> 통신 정보 헬퍼, 네트워크 타입 파싱, API 호환 콜백을 제공합니다.

<br>

## At a Glance (한눈 비교)
- **Carrier Info:** `getCarrierName()`, `getMobileCountryCode()`, `getMobileNetworkCode()` - Carrier name, MCC/MNC (통신사명, MCC/MNC)
- **SIM Status:** `getSimState()`, `isSimReady()`, `getSimOperatorName()`, `getSimCountryIso()` - Check SIM status (SIM 상태 확인)
- **SIM Status String:** `getSimStateString()` - Convert SIM status to string (READY, ABSENT, PIN_REQUIRED, etc.) (SIM 상태를 문자열로 변환)
- **Phone Number:** `getPhoneNumber()` - Query phone number (전화번호 조회)
- **Call State:** `getCallState()` - Check call state (IDLE, RINGING, OFFHOOK) (통화 상태 확인)
- **Network Type:** `getNetworkType()`, `getDataNetworkType()` - Check network type (네트워크 타입 확인)
- **Network Type String:** `getNetworkTypeString()` - Convert 20+ network types to string (20가지 이상 네트워크 타입 문자열 변환)
  - 5G NR, LTE_CA, LTE, HSPA+, HSDPA, UMTS, EDGE, GPRS, CDMA, EVDO, GSM, TD_SCDMA, IWLAN, etc. (등)
- **Roaming:** `isNetworkRoaming()` - Check roaming status (로밍 상태 확인)
- **Signal Strength:** `currentSignalStrength` StateFlow + `getCurrentSignalStrength()` latest value (StateFlow + 최신 값 getter)
- **Service State:** `currentServiceState` StateFlow + `getCurrentServiceState()` latest value (StateFlow + 최신 값 getter)
- **Multi-SIM:** `getActiveSimCount()`, `getActiveSubscriptionInfoList()` - Multi-SIM support (멀티 SIM 지원)
- **TelephonyManager Query:** `getTelephonyManagerFromUSim(slotIndex)` - Return TelephonyManager for specific SIM slot (특정 SIM 슬롯의 TelephonyManager 반환)
- **Permission fallback:** Each guarded API checks only its own permission requirement. Missing optional permissions do not block unrelated telephony APIs.
  - 보호된 각 API는 자신에게 필요한 권한만 확인합니다. 선택 권한이 없어도 무관한 Telephony API는 차단되지 않습니다.
- **Real-time Callback (Basic):** `registerCallback(handler, onSignalStrength, onServiceState, onNetworkState)` - Callback + StateFlow updates (콜백 + StateFlow 자동 업데이트)
- **Unregister Callback:** `unregisterCallback()` - Unregister registered callback (등록된 콜백 해제)
- **Auto API Compatibility:** Automatic branching between TelephonyCallback (API 31+) vs PhoneStateListener (TelephonyCallback (API 31+) vs PhoneStateListener 자동 분기)

**Advanced Multi-SIM Per-Slot Callback System (API 31+) (고급 멀티 SIM 슬롯별 콜백 시스템 (API 31+)):**
- **Default SIM Callback:** `registerTelephonyCallBackFromDefaultUSim(executor, isGpsOn, ...)` - Complete callback for default SIM (기본 SIM에 대한 전체 콜백)
- **Per-Slot Callback:** `registerTelephonyCallBack(simSlotIndex, executor, isGpsOn, ...)` - Complete callback for specific SIM slot (특정 SIM 슬롯 전체 콜백)
  - `executor` - Executor for callback execution (콜백 실행을 위한 Executor)
  - `isGpsOn` - Enable GPS-based cell info callback (location permission required) (GPS 기반 셀 정보 콜백 활성화 여부 (위치 권한 필요))
  - `onActiveDataSubId` - Active data subscription ID change callback (활성 데이터 구독 ID 변경 콜백)
  - `onDataConnectionState` - Data connection state change callback (데이터 연결 상태 변경 콜백)
  - `onCellInfo` - Cell tower info change callback (CurrentCellInfo) (셀 타워 정보 변경 콜백)
  - `onSignalStrength` - Signal strength change callback (CurrentSignalStrength) (신호 강도 변경 콜백)
  - `onServiceState` - Service state change callback (CurrentServiceState) (서비스 상태 변경 콜백)
  - `onCallState` - Call state change callback (callState, phoneNumber) (통화 상태 변경 콜백)
  - `onDisplayInfo` - Display info change callback (TelephonyDisplayInfo - 5G icon, etc.) (디스플레이 정보 변경 콜백 (5G 아이콘 등))
  - `onTelephonyNetworkState` - Network type change callback (TelephonyNetworkState) (통신망 타입 변경 콜백)
- **Per-Slot Callback Unregister:** `unregisterCallBack(simSlotIndex)` - Unregister callback for specific slot (특정 슬롯의 콜백 해제)
- **Check Callback Registration:** `isRegistered(simSlotIndex)` - Check if callback is registered for specific slot (특정 슬롯의 콜백 등록 여부)

**Individual Callback Setters (Can be changed dynamically after registration) (개별 콜백 Setter (등록 후 동적 변경 가능)):**
- `setOnSignalStrength(slotIndex, callback)` - Set signal strength callback (신호 강도 콜백 설정)
- `setOnServiceState(slotIndex, callback)` - Set service state callback (서비스 상태 콜백 설정)
- `setOnActiveDataSubId(slotIndex, callback)` - Set active data SubID callback (활성 데이터 SubID 콜백 설정)
- `setOnDataConnectionState(slotIndex, callback)` - Set data connection state callback (데이터 연결 상태 콜백 설정)
- `setOnCellInfo(slotIndex, callback)` - Set cell info callback (셀 정보 콜백 설정)
- `setOnCallState(slotIndex, callback)` - Set call state callback (통화 상태 콜백 설정)
- `setOnDisplayState(slotIndex, callback)` - Set display info callback (디스플레이 정보 콜백 설정)
- `setOnTelephonyNetworkType(slotIndex, callback)` - Set network type callback (통신망 타입 콜백 설정)

<br>

## Why It Matters (중요한 이유)
**Issues**
- Telephony APIs require version-specific branching and callback management
- Multi-SIM environments need per-slot TelephonyManager coordination
- Permissions often block access without clear fallback handling
> - 통신 API는 버전별 분기와 콜백 관리가 필요
> - 멀티 SIM 환경은 슬롯별 TelephonyManager 조율이 필요
> - 권한 부족 시 접근이 제한되며 폴백 처리가 필요

**Advantages**
- Unified helpers simplify telephony queries and network type parsing
- StateFlow/Callback support for real-time updates
- Permission fallback returns safe defaults and warnings
> - 통신 조회/네트워크 타입 파싱을 단순화하는 헬퍼 제공
> - StateFlow/Callback 기반 실시간 업데이트 지원
> - 권한 폴백으로 안전한 기본값과 경고 제공

<br>

## Plain Android (순수 Android 방식)
- Manual branching between TelephonyCallback (API 31+) and PhoneStateListener is required.
- Runtime permissions must be handled for phone state/number/location access.
- Multi-SIM handling requires per-slot TelephonyManager construction.
> - TelephonyCallback (API 31+)와 PhoneStateListener 분기를 수동 처리해야 합니다.
> - 전화 상태/번호/위치 권한을 런타임에서 직접 처리해야 합니다.
> - 멀티 SIM은 슬롯별 TelephonyManager 구성이 필요합니다.

<br>

## Simple UI Approach (Simple UI 방식)
### Basic Example (기본 예시)
```kotlin
// Request the base permission only (기본 권한만 요청)
requestPermissions(
    permissions = listOf(Manifest.permission.READ_PHONE_STATE),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // Permissions granted - Query network info (권한 허용됨 - 통신망 정보 조회)
            val telephonyInfo = TelephonyInfo(context)

            // Carrier info (통신사 정보)
            val carrierName = telephonyInfo.getCarrierName()
            Log.d("Telephony", "Carrier (통신사): $carrierName")

            // Network type (네트워크 타입)
            val networkType = telephonyInfo.getNetworkTypeString()
            Log.d("Telephony", "Network (네트워크): $networkType")

            // SIM status (SIM 상태)
            val isSimReady = telephonyInfo.isSimReady()
            Log.d("Telephony", "SIM Ready (SIM 준비): $isSimReady")

            // Real-time signal strength via StateFlow (StateFlow로 신호 강도 실시간 수신)
            telephonyInfo.registerCallback()
            lifecycleScope.launch {
                telephonyInfo.currentSignalStrength.collect { signalStrength ->
                    Log.d("Telephony", "Signal Strength (신호 강도): ${signalStrength?.level}")
                }
            }
        }
    },
)
```

### Detailed Rationale Example (자세한 사유 예시)
```kotlin
val telephonyInfo = TelephonyInfo(context)

// Request an optional permission only when the related feature is used.
// (관련 기능을 사용할 때만 선택 권한 요청)
requestPermissions(
    permissions = listOf(Manifest.permission.READ_PHONE_NUMBERS),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // READ_PHONE_STATE or READ_PHONE_NUMBERS is accepted.
            // (READ_PHONE_STATE 또는 READ_PHONE_NUMBERS 중 하나를 허용)
            val phoneNumber = telephonyInfo.getPhoneNumber()
            Log.d("Telephony", "Phone Number (전화번호): $phoneNumber")
        } else {
            Log.d("Telephony", "Denied Permissions (거부된 권한): ${deniedResults.map { it.permission }}")
        }
    },
)

// ACCESS_FINE_LOCATION is additionally required only for cell info callbacks.
// (셀 정보 콜백에는 ACCESS_FINE_LOCATION을 추가로 요청)
requestPermissions(
    permissions = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            telephonyInfo.registerTelephonyCallBackFromDefaultUSim(
                executor = context.mainExecutor,
                isGpsOn = true,
            )
        }
    },
)
```

<br>

## Callback API Migration (콜백 API 마이그레이션)

`TelephonyCallbackManager` and `CommonTelephonyCallback` are internal implementation types in the next major source state. Use `TelephonyInfo` as the single consumer entry point.
> `TelephonyCallbackManager`와 `CommonTelephonyCallback`은 다음 메이저 소스 상태에서 내부 구현 타입입니다. 소비자 진입점으로는 `TelephonyInfo`만 사용해 주세요.

| Previous API (기존 API) | Replacement API (대체 API) |
| --- | --- |
| `TelephonyCallbackManager(context)` | `TelephonyInfo(context)` |
| `registerSimpleCallback(...)` | `registerCallback(...)` |
| `unregisterSimpleCallback()` | `unregisterCallback()` |
| `registerAdvancedCallbackFromDefaultUSim(...)` | `registerTelephonyCallBackFromDefaultUSim(...)` |
| `registerAdvancedCallback(...)` | `registerTelephonyCallBack(...)` |
| `unregisterAdvancedCallback(slotIndex)` | `unregisterCallBack(slotIndex)` |
| `currentSignalStrength`, `currentServiceState`, `currentNetworkState` | Same properties on `TelephonyInfo` (`TelephonyInfo`의 동일 프로퍼티) |

The raw listener objects exposed by `CommonTelephonyCallback` have no direct replacement. Register and unregister through `TelephonyInfo` so the library can manage SDK branching and callback lifecycle.
> `CommonTelephonyCallback`이 노출하던 원시 리스너 객체에는 직접 대체 API를 제공하지 않습니다. SDK 분기와 콜백 생명주기를 라이브러리가 관리할 수 있도록 `TelephonyInfo`를 통해 등록하고 해제해 주세요.

<br>

## Permissions (권한)
Permissions are feature-specific. The library checks the current permission state for each guarded operation. Call `refreshPermissions()` after a permission change only when reading the permission snapshot through `getPermissionInfo()` or `isPermissionGranted()`.
> 권한은 기능별로 적용됩니다. 라이브러리는 보호된 작업마다 현재 권한 상태를 확인합니다. 권한 변경 후 `getPermissionInfo()` 또는 `isPermissionGranted()`로 권한 스냅샷을 읽을 때만 `refreshPermissions()`를 호출하세요.

| Feature (기능) | Permission (권한) |
| --- | --- |
| Basic telephony, SIM/subscription, simple callback, advanced callback with `isGpsOn=false`<br>기본 전화망, SIM/구독, 단순 콜백, `isGpsOn=false` 고급 콜백 | `READ_PHONE_STATE` |
| `getPhoneNumber()` | `READ_PHONE_STATE` **or** `READ_PHONE_NUMBERS`<br>`READ_PHONE_STATE` **또는** `READ_PHONE_NUMBERS` |
| Cell info callback with `isGpsOn=true`<br>`isGpsOn=true` 셀 정보 콜백 | `READ_PHONE_STATE` + `ACCESS_FINE_LOCATION` |

- [README_PERMISSION.md](../../../README_PERMISSION.md)

<br>

## Related Docs (관련 문서)
- Next major migration: [README_SYSTEM_MANAGER_MIGRATION.md](../../README_SYSTEM_MANAGER_MIGRATION.md)
- Summary: [README_SERVICE_MANAGER_INFO.md](../README_SERVICE_MANAGER_INFO.md)
- Permission Guide: [README_PERMISSION.md](../../../README_PERMISSION.md)

<br>
