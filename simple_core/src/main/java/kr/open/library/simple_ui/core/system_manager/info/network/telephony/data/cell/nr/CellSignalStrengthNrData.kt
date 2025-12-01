package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.nr

import android.os.Build
import android.telephony.CellSignalStrengthNr
import androidx.annotation.RequiresApi

public data class CellSignalStrengthNrData(
    private var cellSignalStrength: CellSignalStrengthNr? = null,
) {
    /******************************
     *  get CellSignalStrengthNr  *
     ******************************/
    @RequiresApi(Build.VERSION_CODES.S)
    public fun getCsiCqiReport(): List<Int>? = cellSignalStrength?.csiCqiReport

    @RequiresApi(Build.VERSION_CODES.S)
    public fun getCsiCqiTableIndex(): Int? = cellSignalStrength?.csiCqiTableIndex

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getCsiRsrp(): Int? = cellSignalStrength?.csiRsrp

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getCsiRsrq(): Int? = cellSignalStrength?.csiRsrq

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getCsiSinr(): Int? = cellSignalStrength?.csiSinr

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getSsRsrp(): Int? = cellSignalStrength?.ssRsrp

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getSsRsrq(): Int? = cellSignalStrength?.ssRsrq

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getSsSinr(): Int? = cellSignalStrength?.ssSinr

    public fun getDbm(): Int? = cellSignalStrength?.dbm

    public fun getLevel(): Int? = cellSignalStrength?.level

    public fun getAsuLevel(): Int? = cellSignalStrength?.asuLevel
}
