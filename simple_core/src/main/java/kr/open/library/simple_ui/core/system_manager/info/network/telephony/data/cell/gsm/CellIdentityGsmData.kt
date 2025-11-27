package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.gsm

import android.telephony.CellIdentityGsm

public data class CellIdentityGsmData(
    public val cellIdentity: CellIdentityGsm? = null
) {
    /******************************
     * get currentCellIdentityLTE *
     ******************************/
    public fun getOperatorAlphaLong(): CharSequence? = cellIdentity?.operatorAlphaLong
    public fun getOperatorAlphaShort(): CharSequence? = cellIdentity?.operatorAlphaShort
    public fun getArfcn(): Int? = cellIdentity?.arfcn
    public fun getCid(): Int? = cellIdentity?.cid
    public fun getLac(): Int? = cellIdentity?.lac
    public fun getBsic(): Int? = cellIdentity?.bsic
    public fun getMccString(): String? = cellIdentity?.mccString
    public fun getMncString(): String? = cellIdentity?.mncString
    public fun getMobileNetworkOperator(): String? = cellIdentity?.mobileNetworkOperator
}