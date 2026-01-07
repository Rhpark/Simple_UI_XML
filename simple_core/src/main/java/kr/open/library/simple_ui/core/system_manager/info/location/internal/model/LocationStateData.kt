package kr.open.library.simple_ui.core.system_manager.info.location.internal.model

import android.location.Location

/**
 * Internal data class representing a snapshot of location metrics.<br><br>
 * 위치 메트릭 스냅샷을 나타내는 내부 데이터 클래스입니다.<br>
 *
 * @param location The updated location, or `null` if unavailable.<br><br>
 *                 최신 위치 정보이며, 없으면 `null`입니다.<br>
 * @param isGpsEnabled `true` if GPS is enabled; `false` otherwise.<br><br>
 *                     GPS가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
 * @param isNetworkEnabled `true` if the Network provider is enabled; `false` otherwise.<br><br>
 *                         네트워크 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
 * @param isPassiveEnabled `true` if the Passive provider is enabled; `false` otherwise.<br><br>
 *                         Passive 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
 * @param isFusedEnabled `true` if the Fused provider is enabled; `false` otherwise.<br><br>
 *                       Fused 제공자가 활성화되어 있으면 `true`, 아니면 `false`입니다.<br>
 */
internal data class LocationStateData(
    val location: Location?,
    val isGpsEnabled: Boolean?,
    val isNetworkEnabled: Boolean?,
    val isPassiveEnabled: Boolean?,
    val isFusedEnabled: Boolean?
)
