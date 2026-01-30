# WifiController vs Plain Android - Complete Comparison Guide
> **WifiController vs 순수 Android - 완벽 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_core` (UI-independent core module / UI 비의존 코어 모듈)
- **Package**: `kr.open.library.simple_ui.core.system_manager.controller.wifi`

<br></br>

## Overview (개요)
Provides simple APIs for WiFi info/status queries and scans.  
> WiFi 정보/상태 조회 및 스캔을 간단한 API로 제공합니다.

<br></br>

## At a Glance (한눈 비교)
| Item (항목)                | Plain Android (기본 방식)                                 | Simple UI (Simple UI) | Notes (비고) |
|--------------------------|-------------------------------------------------------|-----------------------|---|
| Info query               | Branching between `ConnectivityManager`/`WifiManager` | One-line call         | SDK branching automated<br>SDK 분기 자동 |
| SSID cleanup             | Manual quote removal                                  | Auto cleanup          | Helper provided<br>헬퍼 제공 |
| Scan handling            | Manual permission + calls                             | Simple call           | Permissions are the same<br>권한은 동일 |
| Deprecated API handling  | Handled by caller                                     | Handled internally    | Less boilerplate<br>코드 간소화 |

<br></br>

## Why It Matters (중요한 이유)
**Issues**
- Complex SDK version branching
- Need to use both ConnectivityManager and WifiManager
- Manual handling of deprecated APIs
> SDK 버전별 분기 처리 복잡
> <br>ConnectivityManager/WifiManager 동시 사용 필요
> <br>Deprecated API 수동 처리

**Advantages**
- Automatic SDK branching
- Automatic SSID quote removal
- Helper functions provided
> SDK 분기 자동 처리
> <br>SSID 따옴표 제거 자동 처리
> <br>헬퍼 함수 제공

<br></br>

## Plain Android (순수 Android 방식)
```kotlin
// Traditional WiFi information query method (기존의 WiFi 정보 조회 방법)
@RequiresPermission(allOf = [Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE])
private fun getWifiInfo() {
    // 1. Acquire WifiManager (WifiManager 획득)
    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // 2. SDK version-specific branching (SDK 버전별 분기 처리)
    val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ - Use NetworkCapabilities (NetworkCapabilities 사용)
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            (capabilities.transportInfo as? WifiInfo)
        } else {
            null
        }
    } else {
        // Android 11 and below - Legacy API (Android 11 이하 - 구형 API)
        @Suppress("DEPRECATION")
        wifiManager.connectionInfo
    }

    // 3. Extract information (정보 추출)
    wifiInfo?.let { info ->
        val ssid = info.ssid.removeSurrounding("\"")
        val bssid = info.bssid
        val rssi = info.rssi
        val linkSpeed = info.linkSpeed

        Log.d("WiFi", "SSID: $ssid, RSSI: $rssi, Speed: $linkSpeed Mbps")
    }
}

// WiFi scan - Complex permissions and handling (WiFi 스캔 - 복잡한 권한 및 처리)
@RequiresPermission(allOf = [
    Manifest.permission.CHANGE_WIFI_STATE,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.ACCESS_FINE_LOCATION
])
private fun scanWifi() {
    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // Start scan (스캔 시작)
    @Suppress("DEPRECATION")
    val success = wifiManager.startScan()

    if (success) {
        // Query scan results (스캔 결과 조회)
        val results = wifiManager.scanResults
        results.forEach { result ->
            Log.d("WiFi", "SSID: ${result.SSID}, Level: ${result.level}")
        }
    }
}
```

<br></br>

## Simple UI Approach (Simple UI 방식)
```kotlin
// Simple WiFi information query - One line (간단한 WiFi 정보 조회 - 한 줄)
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
private fun getWifiInfo() {
    val wifiInfo = getWifiController().getConnectionInfo() // Auto SDK branching (SDK 자동 분기)

    wifiInfo?.let {
        val ssid = getWifiController().getCurrentSsid() // Auto quote removal (따옴표 자동 제거)
        val rssi = getWifiController().getCurrentRssi()
        val linkSpeed = getWifiController().getCurrentLinkSpeed()

        Log.d("WiFi", "SSID: $ssid, RSSI: $rssi, Speed: $linkSpeed Mbps")
    }
}

// WiFi scan - Simple call (WiFi 스캔 - 간단 호출)
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
```

<br></br>

## Related Extensions (관련 확장 함수)
- `getWifiController()`  
  See full list / 전체 목록: [README_SYSTEM_MANAGER_EXTENSIONS.md](../../README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>

