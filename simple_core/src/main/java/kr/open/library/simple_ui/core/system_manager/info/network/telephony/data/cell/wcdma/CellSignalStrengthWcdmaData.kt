package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.wcdma

import android.os.Build
import android.telephony.CellSignalStrengthWcdma
import androidx.annotation.RequiresApi

public data class CellSignalStrengthWcdmaData(
    public val cellSignalStrength: CellSignalStrengthWcdma? = null,
) {
    /***************************
     *  get cellSignalStrength *
     ***************************/
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getEcNo(): Int? = cellSignalStrength?.ecNo

    public fun getLevel(): Int? = cellSignalStrength?.level

    public fun getAsuLevel(): Int? = cellSignalStrength?.asuLevel

    /** same to Rscp */
    public fun getAsuDbm(): Int? = cellSignalStrength?.dbm
}
