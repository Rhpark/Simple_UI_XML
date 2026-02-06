# Telephony Info vs Plain Android - Complete Comparison Guide
> **Telephony Info vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.info.network.telephony`

<br></br>

## Overview (개요)
Provides telephony helpers, network type parsing, and real-time callbacks with API compatibility.  
> 통신 정보 헬퍼, 네트워크 타입 파싱, API 호환 콜백을 제공합니다.

<br></br>

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
- **Permission fallback:** Returns safe defaults/empty lists and logs warnings when permission is missing; call `refreshPermissions()` after grant.
  - 권한이 없으면 안전한 기본값/빈 리스트를 반환하며 로그에 경고가 남습니다. 권한을 허용했다면 `refreshPermissions()`를 호출해 상태를 갱신하세요.
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

<br></br>

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

<br></br>

## Plain Android (순수 Android 방식)
- Manual branching between TelephonyCallback (API 31+) and PhoneStateListener is required.
- Runtime permissions must be handled for phone state/number/location access.
- Multi-SIM handling requires per-slot TelephonyManager construction.
> - TelephonyCallback (API 31+)와 PhoneStateListener 분기를 수동 처리해야 합니다.
> - 전화 상태/번호/위치 권한을 런타임에서 직접 처리해야 합니다.
> - 멀티 SIM은 슬롯별 TelephonyManager 구성이 필요합니다.

<br></br>

## Simple UI Approach (Simple UI 방식)
### Basic Example (기본 예시)
```kotlin
// Request required permissions together (필수 권한 일괄 요청)
requestPermissions(
    permissions = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.ACCESS_FINE_LOCATION
    ),
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
// Same permission set with explicit rationale (필요 권한을 이유와 함께 요청)
requestPermissions(
    permissions = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.ACCESS_FINE_LOCATION
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // All permissions granted - Full info access (모든 권한 허용됨 - 전체 정보 접근)
            val telephonyInfo = TelephonyInfo(context)

            // Query phone number (requires READ_PHONE_NUMBERS)
            // (전화번호 조회 (READ_PHONE_NUMBERS 필요))
            val phoneNumber = telephonyInfo.getPhoneNumber()
            Log.d("Telephony", "Phone Number (전화번호): $phoneNumber")

            // Cell tower location info (requires ACCESS_FINE_LOCATION)
            // (셀 타워 위치 정보 (ACCESS_FINE_LOCATION 필요))
            // ... Detailed cell info can be queried (상세 셀 정보 조회 가능)
        } else {
            // Partial permissions granted - Only safe APIs return data
            // (일부 권한만 허용됨 - 허용된 범위의 API만 사용 가능)
            Log.d("Telephony", "Denied Permissions (거부된 권한): ${deniedResults.map { it.permission }}")
        }
    },
)
```

<br></br>

## Permissions (권한)
TelephonyInfo requires phone state/number and fine location permissions.  
> TelephonyInfo는 전화 상태/번호 및 위치 권한이 필요합니다.

- [README_PERMISSION.md](../../../README_PERMISSION.md)

<br></br>

## Related Docs (관련 문서)
- Summary: [README_SERVICE_MANAGER_INFO.md](../README_SERVICE_MANAGER_INFO.md)
- Permission Guide: [README_PERMISSION.md](../../../README_PERMISSION.md)

<br></br>

