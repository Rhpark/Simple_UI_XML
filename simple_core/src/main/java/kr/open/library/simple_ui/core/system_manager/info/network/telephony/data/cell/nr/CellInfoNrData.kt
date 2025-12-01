package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.nr

import android.os.Build
import android.telephony.CellIdentityNr
import android.telephony.CellInfoNr
import android.telephony.CellSignalStrengthNr
import androidx.annotation.RequiresApi

public data class CellInfoNrData(
    private var cellInfo: CellInfoNr,
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    private var cellDataNrIdentity = CellIdentityNrData(cellInfo.cellIdentity as CellIdentityNr)

    @RequiresApi(Build.VERSION_CODES.Q)
    private var cellDataNrSignalStrength = CellSignalStrengthNrData(cellInfo.cellSignalStrength as CellSignalStrengthNr)

    /*******************
     *  get CellInfoNr *
     *******************/
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getTimestampMillis(): Long = cellInfo.timestampMillis

    public fun isRegistered(): Boolean = cellInfo.isRegistered

    public fun getCellConnectionStatus(): Int = cellInfo.cellConnectionStatus

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getIdentity(): CellIdentityNrData = cellDataNrIdentity

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getSignalStrength(): CellSignalStrengthNrData = cellDataNrSignalStrength
}
