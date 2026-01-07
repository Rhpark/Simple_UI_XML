package kr.open.library.simple_ui.core.system_manager.info.location

import android.location.Location

object LocationStateConstants {
    /**
     * Error value (null) used when boolean location information cannot be retrieved or status is unavailable.<br><br>
     * Boolean형 위치 정보를 가져올 수 없거나 상태를 알 수 없을 때 사용하는 오류 값(null)입니다.<br>
     */
    public val LOCATION_ERROR_VALUE_BOOLEAN: Boolean? = null

    /**
     * Error value (null) used when Location object cannot be retrieved.<br><br>
     * Location 객체를 가져올 수 없을 때 사용하는 오류 값(null)입니다.<br>
     */
    public val LOCATION_ERROR_VALUE_LOCATION: Location? = null

    /**
     * Default update cycle time in milliseconds.<br><br>
     * 기본 업데이트 주기 시간 (밀리초)입니다.<br>
     */
    public const val DEFAULT_UPDATE_CYCLE_TIME = 2000L

    /**
     * Minimum update cycle time in milliseconds.<br><br>
     * 최소 업데이트 주기 시간 (밀리초)입니다.<br>
     */
    public const val MIN_UPDATE_CYCLE_TIME = 1000L

    /**
     * Default minimum distance between location updates in meters.<br><br>
     * 위치 업데이트 기본 최소 거리 (미터)입니다.<br>
     */
    public const val DEFAULT_UPDATE_CYCLE_DISTANCE = 2.0f

    /**
     * Minimum distance threshold for location updates in meters.<br><br>
     * 위치 업데이트 최소 거리 임계값 (미터)입니다.<br>
     */
    public const val MIN_UPDATE_CYCLE_DISTANCE = 0.0f
}
