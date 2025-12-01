package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.wcdma

import android.os.Build
import android.telephony.CellInfoWcdma
import androidx.annotation.RequiresApi

public data class CellInfoWcdmaData(
    public val cellInfo: CellInfoWcdma? = null,
) {
    private var cellDataWcdmaIdentity = CellIdentityWcdmaData(cellInfo?.cellIdentity)
    private var cellDataWcdmaSignalStrength = CellSignalStrengthWcdmaData(cellInfo?.cellSignalStrength)

    /*******************
     *  get CellInfoNr *
     *******************/
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getTimestampMillis(): Long? = cellInfo?.timestampMillis

    public fun isRegistered(): Boolean? = cellInfo?.isRegistered

    public fun getCellConnectionStatus(): Int? = cellInfo?.cellConnectionStatus

    public fun getIdentity(): CellIdentityWcdmaData = cellDataWcdmaIdentity

    public fun getSignalStrength(): CellSignalStrengthWcdmaData = cellDataWcdmaSignalStrength
}
