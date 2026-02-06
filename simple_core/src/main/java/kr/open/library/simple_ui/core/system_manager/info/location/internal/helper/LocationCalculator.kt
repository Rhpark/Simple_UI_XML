package kr.open.library.simple_ui.core.system_manager.info.location.internal.helper

import android.location.Location

/**
 * Provides location math utilities (distance, bearing, radius checks).<br><br>
 * 위치 계산 유틸(거리, 방위각, 반경 체크)을 제공합니다.<br>
 */
internal object LocationCalculator {
    /**
     * Calculates the distance between two locations in meters.<br><br>
     * 두 위치 간의 거리를 미터 단위로 계산합니다.<br>
     */
    internal fun calculateDistance(fromLocation: Location, toLocation: Location): Float =
        fromLocation.distanceTo(toLocation)

    /**
     * Calculates the bearing between two locations in degrees.<br><br>
     * 두 위치 간의 방위각을 도 단위로 계산합니다.<br>
     */
    internal fun calculateBearing(fromLocation: Location, toLocation: Location): Float =
        fromLocation.bearingTo(toLocation)

    /**
     * Checks if two locations are within a specified radius.<br><br>
     * 두 위치가 지정한 반경 안에 있는지 확인합니다.<br>
     */
    internal fun isLocationWithRadius(fromLocation: Location, toLocation: Location, radius: Float): Boolean =
        calculateDistance(fromLocation, toLocation) <= radius
}
