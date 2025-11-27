package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.gsm

import android.os.Build
import android.telephony.CellSignalStrengthGsm
import androidx.annotation.RequiresApi

public data class CellSignalStrengthGsmData(
    public val cellSignalStrength: CellSignalStrengthGsm? = null
) {

    /******************************
     * get CellSignalStrengthLte  *
     ******************************/
    public fun getDbm(): Int? = cellSignalStrength?.dbm
    public fun getLevel(): Int? = cellSignalStrength?.level
    public fun getAsuLevel(): Int? = cellSignalStrength?.asuLevel
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getRssi(): Int? = cellSignalStrength?.rssi
    public fun getTimingAdvance(): Int? = cellSignalStrength?.timingAdvance
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getBitErrorRate(): Int? = cellSignalStrength?.bitErrorRate
}