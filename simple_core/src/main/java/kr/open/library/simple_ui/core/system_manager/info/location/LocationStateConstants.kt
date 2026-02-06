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
    public const val DEFAULT_UPDATE_CYCLE_TIME = 5000L

    /**
     * Low-power mode value that disables periodic polling while keeping listener/receiver registration active.<br>
     * When used, periodic polling is skipped and only an initial one-shot sync is performed.<br><br>
     * 주기 폴링을 비활성화하여 저전력 모드로 동작하게 만드는 값입니다.<br>
     * 이 값을 사용하면 listener/receiver 등록은 유지되며, 초기 1회 동기화만 수행합니다.<br>
     */
    public const val POLLING_DISABLED_UPDATE_CYCLE_TIME = -1L

    /**
     * Legacy alias for low-power polling disabled mode.<br><br>
     * 저전력(폴링 비활성) 모드에 대한 기존 별칭입니다.<br>
     */
    @Deprecated(
        message = "Use POLLING_DISABLED_UPDATE_CYCLE_TIME (low-power polling disabled mode).",
        ReplaceWith("POLLING_DISABLED_UPDATE_CYCLE_TIME")
    )
    public const val DISABLE_UPDATE_CYCLE_TIME = POLLING_DISABLED_UPDATE_CYCLE_TIME

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
    public const val MIN_UPDATE_CYCLE_DISTANCE = 0.1f

    /**
     * Time threshold in milliseconds to consider a location as "significantly newer" or "significantly older".<br>
     * Locations with time difference greater than this value are prioritized/deprioritized based on recency.<br>
     * Based on Android's official LocationManager documentation recommendation.<br><br>
     * 위치를 "상당히 새로운" 또는 "상당히 오래된" 것으로 간주하는 시간 임계값(밀리초)입니다.<br>
     * 이 값보다 큰 시간 차이를 가진 위치는 최신성에 따라 우선/후순위가 결정됩니다.<br>
     * Android 공식 LocationManager 문서 권장 사항에 기반합니다.<br>
     */
    public const val SIGNIFICANT_TIME_DELTA_MS = 10_000L

    /**
     * Accuracy threshold in meters to consider a location as "significantly less accurate".<br>
     * If the new location's accuracy is worse by more than this value, it may be rejected even if newer.<br><br>
     * 위치를 "상당히 덜 정확한" 것으로 간주하는 정확도 임계값(미터)입니다.<br>
     * 새 위치의 정확도가 이 값보다 더 나쁘면, 더 최신이더라도 거부될 수 있습니다.<br>
     */
    public const val SIGNIFICANT_ACCURACY_DELTA_METERS = 200
}
