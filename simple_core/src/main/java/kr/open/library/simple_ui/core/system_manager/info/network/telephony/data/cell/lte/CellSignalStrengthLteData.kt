package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.lte

import android.os.Build
import android.telephony.CellSignalStrengthLte
import androidx.annotation.RequiresApi

/**
 * Wrapper class for CellSignalStrengthLte.<br><br>
 * CellSignalStrengthLte를 위한 래퍼 클래스입니다.<br>
 *
 * Provides accessors for LTE signal strength metrics.<br><br>
 * LTE 신호 강도 지표에 대한 접근자를 제공합니다.<br>
 *
 * @property cellSignalStrength The original CellSignalStrengthLte object.<br><br>
 *                              원본 CellSignalStrengthLte 객체.
 */
public data class CellSignalStrengthLteData(
    public val cellSignalStrength: CellSignalStrengthLte? = null
) {

    /******************************
     * get CellSignalStrengthLte  *
     ******************************/
    /**
     * Gets the CQI table index (API 31+).<br><br>
     * CQI 테이블 인덱스를 가져옵니다 (API 31+).<br>
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public fun getCqiTableIndex(): Int? = cellSignalStrength?.cqiTableIndex
    /**
     * Gets the RSRP (Reference Signal Received Power).<br><br>
     * RSRP (기준 신호 수신 전력)를 가져옵니다.<br>
     */
    public fun getRsrp(): Int? = cellSignalStrength?.rsrp
    /**
     * Gets the RSRQ (Reference Signal Received Quality).<br><br>
     * RSRQ (기준 신호 수신 품질)를 가져옵니다.<br>
     */
    public fun getRsrq(): Int? = cellSignalStrength?.rsrq
    /**
     * Gets the RSSI (Received Signal Strength Indicator) (API 29+).<br><br>
     * RSSI (수신 신호 강도 표시기)를 가져옵니다 (API 29+).<br>
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getRssi(): Int? = cellSignalStrength?.rssi
    /**
     * Gets the RSSNR (Reference Signal Signal-to-Noise Ratio).<br><br>
     * RSSNR (기준 신호 신호 대 잡음비)을 가져옵니다.<br>
     */
    public fun getRssnr(): Int? = cellSignalStrength?.rssnr
    /**
     * Gets the CQI (Channel Quality Indicator).<br><br>
     * CQI (채널 품질 표시기)를 가져옵니다.<br>
     */
    public fun getCqi(): Int? = cellSignalStrength?.cqi
    /**
     * Gets the timing advance.<br><br>
     * 타이밍 어드밴스를 가져옵니다.<br>
     */
    public fun getTimingAdvance(): Int? = cellSignalStrength?.timingAdvance
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
}