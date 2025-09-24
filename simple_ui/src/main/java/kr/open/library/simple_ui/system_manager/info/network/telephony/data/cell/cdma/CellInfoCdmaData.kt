package kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.cdma

import android.os.Build
import android.telephony.CellInfoCdma
import androidx.annotation.RequiresApi


public data class CellInfoCdmaData(
    private var cellInfo: CellInfoCdma?=null
) {
    private var cellDataCdmaIdentity = CellIdentityCdmaData(cellInfo?.cellIdentity)
    private var cellDataCdmaSignalStrength = CellSignalStrengthCdmaData(cellInfo?.cellSignalStrength)

    /*******************
     * get CellInfoLte *
     *******************/
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getTimestampMillis(): Long? = cellInfo?.timestampMillis
    public fun getCellConnectionStatus(): Int? = cellInfo?.cellConnectionStatus
    public fun isRegistered(): Boolean? = cellInfo?.isRegistered

    public fun getIdentity(): CellIdentityCdmaData = cellDataCdmaIdentity
    public fun getSignalStrength(): CellSignalStrengthCdmaData = cellDataCdmaSignalStrength

}