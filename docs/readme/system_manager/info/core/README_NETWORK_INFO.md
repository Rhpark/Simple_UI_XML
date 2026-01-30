# Network Connectivity Info vs Plain Android - Complete Comparison Guide
> **Network Connectivity Info vs 순수 Android - 완벽 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.info.network.connectivity`

<br></br>

## Overview (개요)
Provides network connectivity queries and callback helpers across transport types.  
> 전송 타입별 네트워크 연결 조회와 콜백 헬퍼를 제공합니다.

<br></br>

## At a Glance (한눈 비교)
- **Basic Connectivity:** `isNetworkConnected()` - Check network connection status (네트워크 연결 여부)
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
  - Direct NetworkInterface query (NetworkInterface 직접 조회)
- **Callback Management (콜백 관리):**
  - `registerNetworkCallback(handler, ...)` - Register general network callback (일반 네트워크 콜백 등록)
  - `registerDefaultNetworkCallback(handler, ...)` - Register default network callback (기본 네트워크 콜백 등록)
  - `unregisterNetworkCallback()` - Unregister general network callback (일반 네트워크 콜백 해제)
  - `unregisterDefaultNetworkCallback()` - Unregister default network callback (기본 네트워크 콜백 해제)
- **Callback Parameters (콜백 파라미터):**
  - `onNetworkAvailable` - Network connected (네트워크 연결됨)
  - `onNetworkLosing` - Network about to disconnect (네트워크 끊어질 예정)
  - `onNetworkLost` - Network disconnected (네트워크 끊어짐)
  - `onUnavailable` - Network unavailable (네트워크 사용 불가)
  - `onNetworkCapabilitiesChanged` - Network capabilities changed (NetworkCapabilitiesData type) (네트워크 능력 변경 (NetworkCapabilitiesData 타입))
  - `onLinkPropertiesChanged` - Link properties changed (NetworkLinkPropertiesData type) (링크 속성 변경 (NetworkLinkPropertiesData 타입))
  - `onBlockedStatusChanged` - Blocked status changed (차단 상태 변경)
- **Summary Info:** `getNetworkConnectivitySummary()` - Query all connection states at once (NetworkConnectivitySummary data class) (모든 연결 상태 한 번에 조회 (NetworkConnectivitySummary 데이터 클래스))

<br></br>

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
- IP address helper simplifies NetworkInterface usage
> - 전송 타입 체크/요약을 위한 통합 헬퍼 제공
> - 콜백 등록/해제가 표준화됨
> - IP 주소 헬퍼로 NetworkInterface 사용 단순화

<br></br>

## Plain Android (순수 Android 방식)
- ConnectivityManager callbacks and NetworkCapabilities handling are implemented manually.
- Transport-specific checks (WiFi/Cellular/VPN/etc.) are repeated per use case.
- IP address queries require direct NetworkInterface traversal.
> - ConnectivityManager 콜백과 NetworkCapabilities 처리를 직접 구현해야 합니다.
> - WiFi/Cellular/VPN 등 전송 타입별 체크가 반복됩니다.
> - IP 주소 조회는 NetworkInterface 직접 탐색이 필요합니다.

<br></br>

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

// Real-time network changes via StateFlow (optional)
// (StateFlow로 네트워크 변경 실시간 수신 (선택))
networkInfo.registerDefaultNetworkCallback()
lifecycleScope.launch {
    // Detect network state changes (네트워크 상태 변경 감지)
}
```

<br></br>

## Permissions (권한)
NetworkConnectivityInfo requires network state permissions; Wi-Fi state is optional.  
> NetworkConnectivityInfo는 네트워크 상태 권한이 필요하며, Wi-Fi 상태 권한은 선택입니다.

- [README_PERMISSION.md](../../../README_PERMISSION.md)

<br></br>

## Related Docs (관련 문서)
- Summary: [README_SERVICE_MANAGER_INFO.md](../README_SERVICE_MANAGER_INFO.md)
- Permission Guide: [README_PERMISSION.md](../../../README_PERMISSION.md)

<br></br>
