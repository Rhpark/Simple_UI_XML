# Network Connectivity Info vs Plain Android - Complete Comparison Guide
> **Network Connectivity Info vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_system_manager` (system manager module / system manager 전용 모듈)
- **Package**: `kr.open.library.simple_ui.system_manager.core.info.network.connectivity`

<br>

## Overview (개요)
Provides network connectivity queries and callback helpers across transport types.  
> 전송 타입별 네트워크 연결 조회와 콜백 헬퍼를 제공합니다.

<br>

## At a Glance (한눈 비교)
- **Basic Connectivity:** `isNetworkConnected()` - Check whether the active network has both capabilities and link properties (활성 네트워크의 능력·링크 속성 존재 여부)
  - This does not guarantee validated Internet access; inspect `NET_CAPABILITY_INTERNET` and `NET_CAPABILITY_VALIDATED` when required (검증된 인터넷 연결을 보장하지 않으며, 필요한 경우 두 capability를 추가 확인)
- **Transport Type Connection Check (Transport 타입별 연결 확인):**
  - `isConnectedWifi()` - Check WiFi connection (WiFi 연결 여부)
  - `isConnectedMobile()` - Check mobile data connection (모바일 데이터 연결 여부)
  - `isConnectedVPN()` - Check VPN connection (VPN 연결 여부)
  - `isConnectedBluetooth()` - Check Bluetooth connection (블루투스 연결 여부)
  - `isConnectedWifiAware()` - Check WiFi Aware connection (WiFi Aware 연결 여부)
  - `isConnectedEthernet()` - Check Ethernet connection (이더넷 연결 여부)
  - `isConnectedLowPan()` - Check LowPan connection (LowPan 연결 여부)
  - `isConnectedUSB()` - Check USB connection (API 31+) (USB 연결 여부)
- **WiFi Status:** `isWifiEnabled()` - Check if WiFi is enabled (WiFi 활성화 여부)
- **Network Capabilities:** `getNetworkCapabilities()` - Return NetworkCapabilities object (NetworkCapabilities 객체 반환)
- **Link Properties:** `getLinkProperties()` - Return LinkProperties object (LinkProperties 객체 반환)
- **IP Address Query:** `getIPAddressByNetworkType(type)` - Query IP address by network type (IPv4 only) (네트워크 타입별 IP 주소 조회 (IPv4 전용))
  - `TRANSPORT_ETHERNET` - Ethernet IP address (이더넷 IP 주소)
  - `TRANSPORT_WIFI` - WiFi IP address (WiFi IP 주소)
  - `TRANSPORT_CELLULAR` - Mobile data IP address (모바일 데이터 IP 주소)
  - Exclude loopback addresses, return IPv4 only (Loopback 주소 제외, IPv4만 반환)
  - Best-effort `ConnectivityManager.allNetworks` snapshot; deprecated on API 31+ (최선형 `allNetworks` 스냅샷 조회, API 31 이상에서 deprecated)
- **Callback Management (콜백 관리):**
  - `registerNetworkCallback(handler, ...)` - Register general network callback (일반 네트워크 콜백 등록)
  - `registerDefaultNetworkCallback(handler, ...)` - Register default network callback (기본 네트워크 콜백 등록)
  - `unregisterNetworkCallback()` - Unregister general network callback (일반 네트워크 콜백 해제)
  - `unregisterDefaultNetworkCallback()` - Unregister default network callback (기본 네트워크 콜백 해제)
  - A null `handler` uses the platform default connectivity callback thread, not the main thread (null `handler`는 메인 스레드가 아닌 플랫폼 기본 연결성 콜백 스레드 사용)
- **Callback Parameters (콜백 파라미터):**
  - `onNetworkAvailable` - Network connected (네트워크 연결됨)
  - `onNetworkLosing` - Network about to disconnect (네트워크 끊어질 예정)
  - `onNetworkLost` - Requested network lost; for the default callback, it may only mean that the network is no longer the default (요청 네트워크 상실. 기본 네트워크 콜백에서는 기본 지위 상실만 의미할 수 있음)
  - `onUnavailable` - Network unavailable (네트워크 사용 불가)
  - `onNetworkCapabilitiesChanged` - Network capabilities changed (NetworkCapabilitiesData type) (네트워크 능력 변경 (NetworkCapabilitiesData 타입))
  - `onLinkPropertiesChanged` - Link properties changed (NetworkLinkPropertiesData type) (링크 속성 변경 (NetworkLinkPropertiesData 타입))
  - `onBlockedStatusChanged` - Blocked status changed (차단 상태 변경)
- **Summary Info:** `getNetworkConnectivitySummary()` - Query all connection states at once (NetworkConnectivitySummary data class) (모든 연결 상태 한 번에 조회 (NetworkConnectivitySummary 데이터 클래스))

<br>

## Why It Matters (중요한 이유)
**Issues**
- Manual ConnectivityManager callbacks and capability handling are verbose
- Transport-specific checks often require repetitive code
- Proper unregister/cleanup must be managed manually
> - ConnectivityManager 콜백/능력 처리가 장황함
> - 전송 타입별 체크가 반복 코드로 이어짐
> - 해제/정리 로직을 직접 관리해야 함

**Advantages**
- Unified helpers for transport checks and summaries
- Callback registration/unregistration is standardized
- IP address helper simplifies transport-specific IPv4 lookup
> - 전송 타입 체크/요약을 위한 통합 헬퍼 제공
> - 콜백 등록/해제가 표준화됨
> - IP 주소 헬퍼로 전송 타입별 IPv4 조회 단순화

<br>

## Plain Android (순수 Android 방식)
- ConnectivityManager callbacks and NetworkCapabilities handling are implemented manually.
- Transport-specific checks (WiFi/Cellular/VPN/etc.) are repeated per use case.
- Transport-specific IP address queries require network, capability, and link property traversal.
> - ConnectivityManager 콜백과 NetworkCapabilities 처리를 직접 구현해야 합니다.
> - WiFi/Cellular/VPN 등 전송 타입별 체크가 반복됩니다.
> - 전송 타입별 IP 주소 조회에는 네트워크·능력·링크 속성 탐색이 필요합니다.

<br>

## Simple UI Approach (Simple UI 방식)
```kotlin
// Ready to use with permission declaration only (no runtime request needed)
// (권한 선언만으로 바로 사용 가능 (런타임 요청 불필요))
val networkInfo = NetworkConnectivityInfo(context)

// Network connection status (네트워크 연결 여부)
val isConnected = networkInfo.isNetworkConnected()
Log.d("Network", "Network Connected (네트워크 연결): $isConnected")

// WiFi connection status (WiFi 연결 여부)
val isWifi = networkInfo.isConnectedWifi()
Log.d("Network", "WiFi Connected (WiFi 연결): $isWifi")

// Mobile data connection status (모바일 데이터 연결 여부)
val isMobile = networkInfo.isConnectedMobile()
Log.d("Network", "Mobile Connected (모바일 연결): $isMobile")

// Network summary info (네트워크 요약 정보)
val summary = networkInfo.getNetworkConnectivitySummary()
Log.d("Network", "Summary (요약): $summary")

// Real-time network changes via ConnectivityManager callback (optional)
// (ConnectivityManager 콜백으로 네트워크 변경 실시간 수신 (선택))
networkInfo.registerDefaultNetworkCallback(
    onNetworkCapabilitiesChanged = { network, capabilities ->
        // Handle the latest default network capabilities
        // (최신 기본 네트워크 능력 처리)
    },
    onNetworkLost = { network ->
        // This network is no longer the app's default network
        // (이 네트워크는 더 이상 앱의 기본 네트워크가 아님)
    },
)

// Release the callback in the matching lifecycle event
// (대응하는 생명주기 이벤트에서 콜백 해제)
networkInfo.unregisterDefaultNetworkCallback()
```

<br>

## Permissions (권한)
Declare `ACCESS_NETWORK_STATE` for connectivity queries and callbacks. Declare `ACCESS_WIFI_STATE`
additionally only when calling `isWifiEnabled()` or `getNetworkConnectivitySummary()`. Both are
normal permissions and do not require a runtime request.
> 연결성 조회와 콜백에는 `ACCESS_NETWORK_STATE`를 선언합니다. `isWifiEnabled()` 또는
> `getNetworkConnectivitySummary()`를 호출할 때만 `ACCESS_WIFI_STATE`를 추가 선언합니다.
> 두 권한 모두 일반 권한이므로 런타임 요청은 필요하지 않습니다.

- [README_PERMISSION.md](../../../README_PERMISSION.md)

<br>

## Contract Boundaries (계약 경계)
- Query methods return a point-in-time snapshot; use callbacks to observe subsequent changes.
- `NetworkCapabilitiesData` and `NetworkLinkPropertiesData` are public callback payloads.
- String-parsed compatibility fields are best-effort values and may be null on different Android releases.
- `getIPAddressByNetworkType()` returns only the first non-loopback IPv4 address and is not a continuous observer.
> - 조회 메서드는 호출 시점의 스냅샷이며 이후 변경은 콜백으로 관찰합니다.
> - `NetworkCapabilitiesData`와 `NetworkLinkPropertiesData`는 공개 콜백 전달 타입입니다.
> - 문자열 파싱 기반 호환 필드는 Android 버전에 따라 null일 수 있는 최선형 값입니다.
> - `getIPAddressByNetworkType()`은 첫 번째 비루프백 IPv4 주소만 반환하며 지속 관찰 API가 아닙니다.

<br>

## Related Docs (관련 문서)
- Summary: [README_SERVICE_MANAGER_INFO.md](../README_SERVICE_MANAGER_INFO.md)
- Permission Guide: [README_PERMISSION.md](../../../README_PERMISSION.md)
- Android Network State Guide: [Read network state](https://developer.android.com/develop/connectivity/network-ops/reading-network-state)
- Android API Reference: [ConnectivityManager](https://developer.android.com/reference/android/net/ConnectivityManager)

<br>
