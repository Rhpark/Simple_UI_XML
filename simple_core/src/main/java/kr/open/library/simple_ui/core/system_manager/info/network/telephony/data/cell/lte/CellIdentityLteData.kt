package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.lte

import android.os.Build
import android.telephony.CellIdentityLte
import androidx.annotation.RequiresApi

public data class CellIdentityLteData(
    public val cellIdentity: CellIdentityLte? = null
) {
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getBandList(): IntArray? = cellIdentity?.bands
    public fun getPci(): Int? = cellIdentity?.pci
    public fun getTac(): Int? = cellIdentity?.tac
    public fun getEarfcn(): Int? = cellIdentity?.earfcn
    public fun getBandWidth(): Int? = cellIdentity?.bandwidth
    public fun getEci(): Int? = cellIdentity?.ci
    public fun getMcc(): String? = cellIdentity?.mccString
    public fun getMnc(): String? = cellIdentity?.mncString
    public fun getEnb(): Int? = cellIdentity?.ci?.ushr(8)?.and(0xFFFFF)
    public fun getLcid(): Int? = cellIdentity?.ci?.and(0xFF)
    public fun getMobileNetworkOperator(): String? = cellIdentity?.mobileNetworkOperator
    public fun getOperatorAlphaLong(): CharSequence? = cellIdentity?.operatorAlphaLong
    public fun getOperatorAlphaShort(): CharSequence? = cellIdentity?.operatorAlphaShort
}