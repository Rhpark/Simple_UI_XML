package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.nr

import android.os.Build
import android.telephony.CellIdentityNr
import androidx.annotation.RequiresApi

public data class CellIdentityNrData(
    public val cellIdentity: CellIdentityNr? = null,
) {
    /******************************
     *  get currentCellIdentityNr *
     ******************************/
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getBandList(): IntArray? = cellIdentity?.bands

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getPci(): Int? = cellIdentity?.pci

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getTac(): Int? = cellIdentity?.tac

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getNrArfcn(): Int? = cellIdentity?.nrarfcn

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getBandNci(): Long? = cellIdentity?.nci

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getMcc(): String? = cellIdentity?.mccString

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getMnc(): String? = cellIdentity?.mncString

    public fun getOperatorAlphaLong(): CharSequence? = cellIdentity?.operatorAlphaLong

    public fun getOperatorAlphaShort(): CharSequence? = cellIdentity?.operatorAlphaShort
}
