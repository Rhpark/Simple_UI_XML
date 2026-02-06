# SIM Info vs Plain Android - Complete Comparison Guide
> **SIM Info vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.info.network.sim`

<br></br>

## Overview (개요)
Provides SIM information helpers and multi-SIM utilities with safe permission fallback.  
> 안전한 권한 폴백과 멀티 SIM 유틸을 포함한 SIM 정보 헬퍼를 제공합니다.

<br></br>

## At a Glance (한눈 비교)
- **Basic Info:** `isDualSim()`, `isSingleSim()`, `isMultiSim()` - Check SIM type (SIM 타입 확인)
- **Active SIM:** `getActiveSimCount()`, `getActiveSimSlotIndexList()` - Active SIM information (활성화된 SIM 정보)
- **Read Permission:** `isCanReadSimInfo()` - Check if SIM info can be read (permission and initialization status) (SIM 정보 읽기 가능 여부 확인 (권한 및 초기화 상태))
- **Permission fallback:** Returns empty list/null and logs a warning when permission is missing; call `refreshPermissions()` after grant.
  - 권한이 없으면 빈 리스트/null을 반환하고 로그에 경고가 남습니다. 권한 허용 후 `refreshPermissions()` 호출을 권장합니다.
- **Subscription Info:** `getActiveSubscriptionInfoList()` - Query all subscription info (모든 구독 정보 조회)
- **Subscription ID:**
  - `getSubIdFromDefaultUSim()` - Query default SIM subscription ID (기본 SIM의 구독 ID 조회)
  - `getSubId(slotIndex)` - Query specific SIM slot subscription ID (특정 SIM 슬롯의 구독 ID 조회)
  - `subIdToSimSlotIndex(currentSubId)` - Convert Subscription ID to SIM slot index (Subscription ID를 SIM 슬롯 인덱스로 변환)
- **SubscriptionInfo Query:**
  - `getActiveSubscriptionInfoSubId(subId)` - Return SubscriptionInfo for specific SubID (특정 SubID의 SubscriptionInfo 반환)
  - `getActiveSubscriptionInfoSimSlot(slotIndex)` - Return SubscriptionInfo for specific SIM slot (특정 SIM 슬롯의 SubscriptionInfo 반환)
  - `getSubscriptionInfoSubIdFromDefaultUSim()` - Return SubscriptionInfo for default SIM (기본 SIM의 SubscriptionInfo 반환)
  - `getSubscriptionInfoSimSlotFromDefaultUSim()` - Return SubscriptionInfo for default SIM slot (기본 SIM 슬롯의 SubscriptionInfo 반환)
- **MCC/MNC (Default SIM):** `getMccFromDefaultUSimString()`, `getMncFromDefaultUSimString()` - Carrier codes (통신사 코드)
- **MCC/MNC (Per Slot):** `getMcc(slotIndex)`, `getMnc(slotIndex)` - Carrier codes for specific slot (특정 슬롯의 통신사 코드)
- **Phone Number:** `getPhoneNumberFromDefaultUSim()`, `getPhoneNumber(slotIndex)` - Query phone number (전화번호 조회)
- **SIM Status:** `getStatusFromDefaultUSim()`, `getActiveSimStatus(slotIndex)` - Check SIM status (SIM 상태 확인)
- **eSIM Support:** `isESimSupported()`, `isRegisterESim(slotIndex)` - Check eSIM (eSIM 확인)
- **Display Info:** `getDisplayNameFromDefaultUSim()`, `getCountryIsoFromDefaultUSim()` - Display name, country code (표시명, 국가 코드)
- **Roaming Status:** `isNetworkRoamingFromDefaultUSim()` - Check roaming status (로밍 여부 확인)
- **TelephonyManager Management:**
  - `updateUSimTelephonyManagerList()` - Update TelephonyManager list per SIM slot (SIM 슬롯별 TelephonyManager 목록 업데이트)
  - `getTelephonyManagerFromUSim(slotIndex)` - Return TelephonyManager for specific SIM slot (특정 SIM 슬롯의 TelephonyManager 반환)

<br></br>

## Why It Matters (중요한 이유)
**Issues**
- Multi-SIM querying requires multiple managers and permission checks
- Slot/SubId mapping must be handled manually
- Permission errors often cause empty results without clear handling
> - 멀티 SIM 조회는 여러 매니저와 권한 체크가 필요
> - 슬롯/SubId 매핑을 직접 처리해야 함
> - 권한 오류 시 결과가 비어 처리되기 쉬움

**Advantages**
- Unified SIM helpers reduce multi-SIM complexity
- Permission fallback returns safe defaults with warnings
- Slot-specific TelephonyManager access is provided
> - 멀티 SIM 복잡도를 줄이는 통합 헬퍼 제공
> - 권한 폴백으로 안전한 기본값과 경고 제공
> - 슬롯별 TelephonyManager 접근 제공

<br></br>

## Plain Android (순수 Android 방식)
- Manual use of `SubscriptionManager` and `TelephonyManager` per slot is required.
- Runtime permissions and API-level differences must be handled in the caller.
- SubId and slot index mapping logic is implemented manually.
> - 슬롯별 `SubscriptionManager`/`TelephonyManager`를 직접 사용해야 합니다.
> - 런타임 권한과 API 분기를 호출부에서 처리해야 합니다.
> - SubId와 슬롯 인덱스 매핑 로직을 수동 구현해야 합니다.

<br></br>

## Simple UI Approach (Simple UI 방식)
```kotlin
// Request phone state permission (required) (전화 상태 권한 요청 (필수))
requestPermissions(
    permissions = listOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.ACCESS_FINE_LOCATION
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // Permissions granted - Query SIM info (권한 허용됨 - SIM 정보 조회)
            val simInfo = SimInfo(context)

            // Check dual SIM (듀얼 SIM 확인)
            val isDualSim = simInfo.isDualSim()
            Log.d("SIM", "Dual SIM (듀얼 SIM): $isDualSim")

            // Active SIM count (활성 SIM 개수)
            val activeCount = simInfo.getActiveSimCount()
            Log.d("SIM", "Active SIM Count (활성 SIM 개수): $activeCount")

            // Query phone number (전화번호 조회)
            val phoneNumber = simInfo.getPhoneNumberFromDefaultUSim()
            Log.d("SIM", "Phone Number (전화번호): $phoneNumber")
        } else {
            // Permissions denied (권한 거부됨)
            toastShowShort("Phone state permission required (전화 상태 권한이 필요합니다)")
        }
    },
)
```

<br></br>

## Permissions (권한)
SimInfo requires phone state/number and fine location permissions.  
> SimInfo는 전화 상태/번호 및 위치 권한이 필요합니다.

- [README_PERMISSION.md](../../../README_PERMISSION.md)

<br></br>

## Related Docs (관련 문서)
- Summary: [README_SERVICE_MANAGER_INFO.md](../README_SERVICE_MANAGER_INFO.md)
- Permission Guide: [README_PERMISSION.md](../../../README_PERMISSION.md)

<br></br>

