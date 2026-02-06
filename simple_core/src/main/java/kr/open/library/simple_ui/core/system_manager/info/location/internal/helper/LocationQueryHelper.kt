package kr.open.library.simple_ui.core.system_manager.info.location.internal.helper

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.extentions.hasPermissions

/**
 * Provides location/provider query utilities and best-location selection.<br><br>
 * 위치/제공자 조회 유틸과 최적 위치 선택 로직을 제공합니다.<br>
 *
 * @param context Application context for permission checks.<br><br>
 *                권한 체크에 사용하는 애플리케이션 컨텍스트입니다.<br>
 * @param locationManager LocationManager instance.<br><br>
 *                        LocationManager 인스턴스입니다.<br>
 */
internal class LocationQueryHelper(
    private val context: Context,
    private val locationManager: LocationManager
) {
    /**
     * Checks if location services are enabled.<br><br>
     * 위치 서비스가 활성화되어 있는지 확인합니다.<br>
     */
    internal fun isLocationEnabled(): Boolean = safeCatch(false) {
        locationManager.isLocationEnabled
    }

    /**
     * Checks if GPS provider is enabled.<br><br>
     * GPS 제공자가 활성화되어 있는지 확인합니다.<br>
     */
    internal fun isGpsEnabled(): Boolean = safeCatch(false) {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Checks if Network provider is enabled.<br><br>
     * 네트워크 제공자가 활성화되어 있는지 확인합니다.<br>
     */
    internal fun isNetworkEnabled(): Boolean = safeCatch(false) {
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Checks if Passive provider is enabled.<br><br>
     * Passive 제공자가 활성화되어 있는지 확인합니다.<br>
     */
    internal fun isPassiveEnabled(): Boolean = safeCatch(false) {
        locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)
    }

    /**
     * Checks if Fused provider is enabled (API 31+).<br><br>
     * Fused 제공자가 활성화되어 있는지 확인합니다(API 31+).<br>
     */
    @RequiresApi(Build.VERSION_CODES.S)
    internal fun isFusedEnabled(): Boolean = safeCatch(false) {
        locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)
    }

    /**
     * Returns Fused provider enabled status or null when not supported.<br><br>
     * Fused 제공자 활성 상태를 반환하며, 미지원 시 null을 반환합니다.<br>
     */
    internal fun getFusedEnabledOrNull(): Boolean? = checkSdkVersion(Build.VERSION_CODES.S,
        positiveWork = { isFusedEnabled() },
        negativeWork = { null }
    )

    /**
     * Checks if any location provider is enabled.<br><br>
     * 하나라도 활성화된 위치 제공자가 있는지 확인합니다.<br>
     */
    internal fun isAnyEnabled(): Boolean = checkSdkVersion(Build.VERSION_CODES.S,
        positiveWork = { (isLocationEnabled() || isGpsEnabled() || isNetworkEnabled() || isPassiveEnabled() || isFusedEnabled()) },
        negativeWork = { (isLocationEnabled() || isGpsEnabled() || isNetworkEnabled() || isPassiveEnabled()) },
    )

    /**
     * Gets the last known location from available providers.<br><br>
     * 사용 가능한 제공자들 중 마지막으로 알려진 위치를 가져옵니다.<br>
     */
    @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    internal fun getLocation(): Location? {
        if (!isAnyEnabled()) {
            var logcatData =
                "can not find location!, isLocationEnabled ${isLocationEnabled()}, isGpsEnabled ${isGpsEnabled()}, isNetworkEnabled ${isNetworkEnabled()}, isPassiveEnabled ${isPassiveEnabled()}"
            logcatData += checkSdkVersion(Build.VERSION_CODES.S) { ", isFusedEnabled ${isFusedEnabled()}" } ?: ""
            Logx.e(logcatData)
            return null
        }

        if (!context.hasPermissions(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
            Logx.d("ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION permission is not granted")
            return null
        }

        return getBestLastKnownLocation()
    }

    /**
     * Selects the best last known location from available providers.<br><br>
     * 사용 가능한 제공자 중에서 가장 적절한 최근 위치를 선택합니다.<br>
     */
    @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun getBestLastKnownLocation(): Location? {
        val hasFine = context.hasPermissions(ACCESS_FINE_LOCATION)
        val hasCoarse = context.hasPermissions(ACCESS_COARSE_LOCATION)

        // 최적화: 최대 4개(Fused, GPS, Network, Passive) 제공자를 고려해 초기 용량을 지정
        val providers = ArrayList<String>(4)

        // API 31+면 FUSED 우선 고려
        checkSdkVersion(Build.VERSION_CODES.S) {
            if (isFusedEnabled()) providers.add(LocationManager.FUSED_PROVIDER)
        }

        // 정밀 권한이 있으면 GPS
        if (hasFine && isGpsEnabled()) {
            providers.add(LocationManager.GPS_PROVIDER)
        }

        // 대략 권한이 있으면 NETWORK
        if (hasCoarse && isNetworkEnabled()) {
            providers.add(LocationManager.NETWORK_PROVIDER)
        }

        // PASSIVE는 권한에 영향 덜 받으므로 마지막 후보
        if (isPassiveEnabled()) {
            providers.add(LocationManager.PASSIVE_PROVIDER)
        }

        var best: Location? = null
        for (provider in providers) {
            val loc = locationManager.getLastKnownLocation(provider) ?: continue
            if (LocationQuality.isBetter(loc, best)) {
                best = loc
            }
        }
        return best
    }
}
