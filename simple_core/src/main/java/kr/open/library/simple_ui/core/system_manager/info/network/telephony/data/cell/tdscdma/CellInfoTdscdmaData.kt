package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.tdscdma

import android.os.Build
import android.telephony.CellInfoTdscdma
import androidx.annotation.RequiresApi

public data class CellInfoTdscdmaData(
    private var cellInfo: CellInfoTdscdma,
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    private var cellDataTdscdmaIdentity = CellIdentityTdscdmaData(cellInfo.cellIdentity)

    @RequiresApi(Build.VERSION_CODES.Q)
    private var cellDataTdscdmaSignalStrength = CellSignalStrengthDataTdscdma(cellInfo.cellSignalStrength)

    /*******************
     *  get CellInfoNr *
     *******************/
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getTimestampMillis(): Long = cellInfo.timestampMillis

    public fun isRegistered(): Boolean = cellInfo.isRegistered

    public fun getCellConnectionStatus(): Int = cellInfo.cellConnectionStatus

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getIdentity(): CellIdentityTdscdmaData = cellDataTdscdmaIdentity

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getSignalStrength(): CellSignalStrengthDataTdscdma = cellDataTdscdmaSignalStrength
}
