package kr.open.library.simple_ui.core.system_manager.info.location

import android.location.Location

/**
 * Sealed class representing various location state events.<br><br>
 * 다양한 위치 상태 이벤트를 나타내는 sealed 클래스입니다.<br>
 * 각 이벤트는 관찰 가능한 위치 정보를 포함합니다.<br>
 */
public sealed class LocationStateEvent {
    /**
     * Event for GPS provider enabled/disabled status updates.<br><br>
     * GPS 제공자 활성/비활성 상태 변화에 대한 이벤트입니다.<br>
     *
     * @param isEnabled `true` if GPS is enabled; `false` otherwise.<br><br>
     *                  GPS가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public data class OnGpsEnabled(
        val isEnabled: Boolean,
    ) : LocationStateEvent()

    /**
     * Event for Fused location provider enabled/disabled status updates.<br><br>
     * Fused 위치 제공자 활성/비활성 상태 변화에 대한 이벤트입니다.<br>
     *
     * @param isEnabled `true` if the Fused provider is enabled; `false` otherwise.<br><br>
     *                  Fused 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public data class OnFusedEnabled(
        val isEnabled: Boolean,
    ) : LocationStateEvent()

    /**
     * Event for Network location provider enabled/disabled status updates.<br><br>
     * 네트워크 위치 제공자 활성/비활성 상태 변화에 대한 이벤트입니다.<br>
     *
     * @param isEnabled `true` if the Network provider is enabled; `false` otherwise.<br><br>
     *                  네트워크 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public data class OnNetworkEnabled(
        val isEnabled: Boolean,
    ) : LocationStateEvent()

    /**
     * Event for Passive location provider enabled/disabled status updates.<br><br>
     * Passive 위치 제공자 활성/비활성 상태 변화에 대한 이벤트입니다.<br>
     *
     * @param isEnabled `true` if the Passive provider is enabled; `false` otherwise.<br><br>
     *                  Passive 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    public data class OnPassiveEnabled(
        val isEnabled: Boolean,
    ) : LocationStateEvent()

    /**
     * Event for location change updates.<br><br>
     * 위치 변경에 대한 이벤트입니다.<br>
     *
     * @param location The updated location, or `null` if unavailable.<br><br>
     *                 최신 위치 정보이며, 없으면 `null`입니다.<br>
     */
    public data class OnLocationChanged(
        val location: Location?,
    ) : LocationStateEvent()
}
