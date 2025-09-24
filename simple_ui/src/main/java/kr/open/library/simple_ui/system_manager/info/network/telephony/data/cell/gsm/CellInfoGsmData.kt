package kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.gsm

import android.os.Build
import android.telephony.CellInfoGsm
import androidx.annotation.RequiresApi

public data class CellInfoGsmData(
    private var cellInfo: CellInfoGsm
) {
    private var cellDataGsmIdentity = CellIdentityGsmData(cellInfo.cellIdentity)
    private var cellDataGsmSignalStrength = CellSignalStrengthGsmData(cellInfo.cellSignalStrength)

    /*******************
     * get CellInfoLte *
     *******************/
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getTimestampMillis(): Long = cellInfo.timestampMillis
    public fun getCellConnectionStatus(): Int = cellInfo.cellConnectionStatus
    public fun isRegistered(): Boolean = cellInfo.isRegistered

    public fun getIdentity(): CellIdentityGsmData = cellDataGsmIdentity
    public fun getSignalStrength(): CellSignalStrengthGsmData = cellDataGsmSignalStrength

}