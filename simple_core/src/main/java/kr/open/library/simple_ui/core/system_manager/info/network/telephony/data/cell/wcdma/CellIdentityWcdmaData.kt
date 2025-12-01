package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.wcdma

import android.os.Build
import android.telephony.CellIdentityWcdma
import androidx.annotation.RequiresApi

public data class CellIdentityWcdmaData(
    public val cellIdentity: CellIdentityWcdma? = null,
) {
    /******************************
     *  get currentCellIdentityNr *
     ******************************/
    public fun getMcc(): String? = cellIdentity?.mccString

    public fun getMnc(): String? = cellIdentity?.mncString

    public fun getOperatorAlphaLong(): CharSequence? = cellIdentity?.operatorAlphaLong

    public fun getOperatorAlphaShort(): CharSequence? = cellIdentity?.operatorAlphaShort

    public fun getMobileNetworkOperator(): String? = cellIdentity?.mobileNetworkOperator

    public fun getLac(): Int? = cellIdentity?.lac

    public fun getPsc(): Int? = cellIdentity?.psc

    public fun getUarfcn(): Int? = cellIdentity?.uarfcn

    public fun getUCid(): Int? = cellIdentity?.cid

    public fun getCid(): Int? = cellIdentity?.cid?.and(0xFFFF)

    public fun getHomeRnc(): Int? = cellIdentity?.cid?.ushr(16)?.and(0xFFF)

    @RequiresApi(Build.VERSION_CODES.R)
    public fun getCsgIdentity(): Int? = cellIdentity?.closedSubscriberGroupInfo?.csgIdentity

    @RequiresApi(Build.VERSION_CODES.R)
    public fun getCsgIndicator(): Boolean? = cellIdentity?.closedSubscriberGroupInfo?.csgIndicator

    @RequiresApi(Build.VERSION_CODES.R)
    public fun getHomeNodebName(): String? = cellIdentity?.closedSubscriberGroupInfo?.homeNodebName
}
