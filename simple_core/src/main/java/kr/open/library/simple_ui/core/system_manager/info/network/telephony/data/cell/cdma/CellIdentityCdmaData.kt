package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.cdma

import android.telephony.CellIdentityCdma

public data class CellIdentityCdmaData(
    public val cellIdentity: CellIdentityCdma? = null
) {
    /******************************
     * get currentCellIdentityCdma *
     ******************************/
    public fun getOperatorAlphaLong(): CharSequence? = cellIdentity?.operatorAlphaLong
    public fun getOperatorAlphaShort(): CharSequence? = cellIdentity?.operatorAlphaShort
    public fun getBasestationId(): Int? = cellIdentity?.basestationId
    public fun getNetworkId(): Int? = cellIdentity?.networkId
    public fun getSystemId(): Int? = cellIdentity?.systemId
    public fun getLatitude(): Int? = cellIdentity?.latitude
    public fun getLongitude(): Int? = cellIdentity?.longitude

}