package kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.tdscdma

import android.os.Build
import android.telephony.CellSignalStrengthTdscdma
import androidx.annotation.RequiresApi

public data class CellSignalStrengthDataTdscdma(
    private var cellSignalStrength: CellSignalStrengthTdscdma? = null
) {
    /***************************
     *  get cellSignalStrength *
     ***************************/
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getRscp(): Int? = cellSignalStrength?.rscp
    public fun getLevel(): Int? = cellSignalStrength?.level
    public fun getAsuLevel(): Int? = cellSignalStrength?.asuLevel
    public fun getAsuDbm(): Int? = cellSignalStrength?.dbm
}