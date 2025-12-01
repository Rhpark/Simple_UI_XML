package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.cdma

import android.telephony.CellSignalStrengthCdma

/**
 * Wrapper class for CellSignalStrengthCdma.<br><br>
 * CellSignalStrengthCdma를 위한 래퍼 클래스입니다.<br>
 *
 * Provides accessors for CDMA signal strength metrics.<br><br>
 * CDMA 신호 강도 지표에 대한 접근자를 제공합니다.<br>
 *
 * @property cellSignalStrength The original CellSignalStrengthCdma object.<br><br>
 *                              원본 CellSignalStrengthCdma 객체.
 */
public data class CellSignalStrengthCdmaData(
    private var cellSignalStrength: CellSignalStrengthCdma? = null,
) {
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
     * Gets the EVDO level.<br><br>
     * EVDO 레벨을 가져옵니다.<br>
     */
    public fun getEvdoLevel(): Int? = cellSignalStrength?.evdoLevel

    /**
     * Gets the EVDO dBm.<br><br>
     * EVDO dBm을 가져옵니다.<br>
     */
    public fun getEvdoDbm(): Int? = cellSignalStrength?.evdoDbm

    /**
     * Gets the EVDO Ec/Io.<br><br>
     * EVDO Ec/Io를 가져옵니다.<br>
     */
    public fun getEvdoEcio(): Int? = cellSignalStrength?.evdoEcio

    /**
     * Gets the EVDO SNR.<br><br>
     * EVDO SNR을 가져옵니다.<br>
     */
    public fun getEvdoSnr(): Int? = cellSignalStrength?.evdoSnr

    /**
     * Gets the CDMA Ec/Io.<br><br>
     * CDMA Ec/Io를 가져옵니다.<br>
     */
    public fun getCdmaEcio(): Int? = cellSignalStrength?.cdmaEcio

    /**
     * Gets the CDMA level.<br><br>
     * CDMA 레벨을 가져옵니다.<br>
     */
    public fun getCdmaLevel(): Int? = cellSignalStrength?.cdmaLevel

    /**
     * Gets the CDMA dBm (RSSI).<br><br>
     * CDMA dBm (RSSI)을 가져옵니다.<br>
     */
    public fun getCdmaDbm(): Int? = cellSignalStrength?.cdmaDbm
}
