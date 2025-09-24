package kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.lte

import android.os.Build
import android.telephony.CellSignalStrengthLte
import androidx.annotation.RequiresApi

public data class CellSignalStrengthLteData(
    public val cellSignalStrength: CellSignalStrengthLte? = null
) {

    /******************************
     * get CellSignalStrengthLte  *
     ******************************/
    @RequiresApi(Build.VERSION_CODES.S)
    public fun getCqiTableIndex(): Int? = cellSignalStrength?.cqiTableIndex
    public fun getRsrp(): Int? = cellSignalStrength?.rsrp
    public fun getRsrq(): Int? = cellSignalStrength?.rsrq
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getRssi(): Int? = cellSignalStrength?.rssi
    public fun getRssnr(): Int? = cellSignalStrength?.rssnr
    public fun getCqi(): Int? = cellSignalStrength?.cqi
    public fun getTimingAdvance(): Int? = cellSignalStrength?.timingAdvance
    public fun getDbm(): Int? = cellSignalStrength?.dbm
    public fun getLevel(): Int? = cellSignalStrength?.level
    public fun getAsuLevel(): Int? = cellSignalStrength?.asuLevel
}