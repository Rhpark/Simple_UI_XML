package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.cdma

import android.telephony.CellIdentityCdma

/**
 * Wrapper class for CellIdentityCdma.<br><br>
 * CellIdentityCdma를 위한 래퍼 클래스입니다.<br>
 *
 * Provides accessors for CDMA cell identity information.<br><br>
 * CDMA 셀 식별 정보에 대한 접근자를 제공합니다.<br>
 *
 * @property cellIdentity The original CellIdentityCdma object.<br><br>
 *                        원본 CellIdentityCdma 객체.
 */
public data class CellIdentityCdmaData(
    public val cellIdentity: CellIdentityCdma? = null,
) {
    /**
     * Gets the long alpha tag.<br><br>
     * 긴 알파 태그를 가져옵니다.<br>
     */
    public fun getOperatorAlphaLong(): CharSequence? = cellIdentity?.operatorAlphaLong

    /**
     * Gets the short alpha tag.<br><br>
     * 짧은 알파 태그를 가져옵니다.<br>
     */
    public fun getOperatorAlphaShort(): CharSequence? = cellIdentity?.operatorAlphaShort

    /**
     * Gets the Base Station ID.<br><br>
     * 기지국 ID를 가져옵니다.<br>
     */
    public fun getBasestationId(): Int? = cellIdentity?.basestationId

    /**
     * Gets the Network ID.<br><br>
     * 네트워크 ID를 가져옵니다.<br>
     */
    public fun getNetworkId(): Int? = cellIdentity?.networkId

    /**
     * Gets the System ID.<br><br>
     * 시스템 ID를 가져옵니다.<br>
     */
    public fun getSystemId(): Int? = cellIdentity?.systemId

    /**
     * Gets the latitude.<br><br>
     * 위도를 가져옵니다.<br>
     */
    public fun getLatitude(): Int? = cellIdentity?.latitude

    /**
     * Gets the longitude.<br><br>
     * 경도를 가져옵니다.<br>
     */
    public fun getLongitude(): Int? = cellIdentity?.longitude
}
