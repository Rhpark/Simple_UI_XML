package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.lte

import android.os.Build
import android.telephony.CellInfoLte
import androidx.annotation.RequiresApi

public data class CellInfoLteData(
    private var cellInfo : CellInfoLte?
) {
    private var cellDataLteIdentity = CellIdentityLteData(cellInfo?.cellIdentity)
    private var cellDataLteSignalStrength = CellSignalStrengthLteData(cellInfo?.cellSignalStrength)

    /*******************
     * get CellInfoLte *
     *******************/
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getTimestampMillis(): Long? = cellInfo?.timestampMillis
    public fun getCellConnectionStatus(): Int? = cellInfo?.cellConnectionStatus
    public fun isRegistered(): Boolean? = cellInfo?.isRegistered

    public fun getCellIdentity(): CellIdentityLteData = cellDataLteIdentity
    public fun getCellSignalStrength(): CellSignalStrengthLteData = cellDataLteSignalStrength

}