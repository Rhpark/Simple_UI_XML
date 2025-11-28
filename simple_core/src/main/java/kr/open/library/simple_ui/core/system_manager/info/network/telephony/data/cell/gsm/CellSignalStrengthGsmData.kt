package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.gsm

import android.os.Build
import android.telephony.CellSignalStrengthGsm
import androidx.annotation.RequiresApi

/**
 * Wrapper class for CellSignalStrengthGsm.<br><br>
 * CellSignalStrengthGsm을 위한 래퍼 클래스입니다.<br>
 *
 * Provides accessors for GSM signal strength metrics.<br><br>
 * GSM 신호 강도 지표에 대한 접근자를 제공합니다.<br>
 *
 * @property cellSignalStrength The original CellSignalStrengthGsm object.<br><br>
 *                              원본 CellSignalStrengthGsm 객체.
 */
public data class CellSignalStrengthGsmData(
    public val cellSignalStrength: CellSignalStrengthGsm? = null
) {

    /******************************
     * get CellSignalStrengthLte  *
     ******************************/
    /**
     * Gets the signal strength in dBm.<br><br>
     * dBm 단위의 신호 강도를 가져옵니다.<br>
     */
    public fun getDbm(): Int? = cellSignalStrength?.dbm
    /**
     * Gets the signal level.<br><br>
     * 신호 레벨을 가져옵니다.<br>
     */
    public fun getLevel(): Int? = cellSignalStrength?.level
    /**
     * Gets the ASU level.<br><br>
     * ASU 레벨을 가져옵니다.<br>
     */
    public fun getAsuLevel(): Int? = cellSignalStrength?.asuLevel
    /**
     * Gets the RSSI (Received Signal Strength Indicator) (API 30+).<br><br>
     * RSSI (수신 신호 강도 표시기)를 가져옵니다 (API 30+).<br>
     */
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getRssi(): Int? = cellSignalStrength?.rssi
    /**
     * Gets the timing advance.<br><br>
     * 타이밍 어드밴스를 가져옵니다.<br>
     */
    public fun getTimingAdvance(): Int? = cellSignalStrength?.timingAdvance
    /**
     * Gets the bit error rate (API 29+).<br><br>
     * 비트 오류율을 가져옵니다 (API 29+).<br>
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getBitErrorRate(): Int? = cellSignalStrength?.bitErrorRate
}