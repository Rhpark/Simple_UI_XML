package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current

import android.os.Build
import android.telephony.CellSignalStrength
import android.telephony.CellSignalStrengthCdma
import android.telephony.CellSignalStrengthGsm
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.CellSignalStrengthTdscdma
import android.telephony.CellSignalStrengthWcdma
import android.telephony.SignalStrength
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.cdma.CellSignalStrengthCdmaData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.gsm.CellSignalStrengthGsmData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.lte.CellSignalStrengthLteData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.nr.CellSignalStrengthNrData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.tdscdma.CellSignalStrengthDataTdscdma
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.wcdma.CellSignalStrengthWcdmaData

/**
 * Wrapper class for current signal strength.<br><br>
 * 현재 신호 강도를 위한 래퍼 클래스입니다.<br>
 *
 * Extracts and categorizes CellSignalStrength information.<br><br>
 * CellSignalStrength 정보를 추출하고 분류합니다.<br>
 *
 * @property signalStrength The original SignalStrength object.<br><br>
 *                          원본 SignalStrength 객체.
 */
@RequiresApi(Build.VERSION_CODES.Q)
public data class CurrentSignalStrength(
    public val signalStrength: SignalStrength?,
) {
    /** List of generic cell signal strength data.<br>일반 셀 신호 강도 데이터 목록. */
    public var cellDataList: List<CellSignalStrength>? = null

    /** List of CDMA cell signal strength data.<br>CDMA 셀 신호 강도 데이터 목록. */
    public val cellDataCdmaList: MutableList<CellSignalStrengthCdmaData> = mutableListOf()

    /** List of GSM cell signal strength data.<br>GSM 셀 신호 강도 데이터 목록. */
    public val cellDataGsmList: MutableList<CellSignalStrengthGsmData> = mutableListOf()

    /** List of LTE cell signal strength data.<br>LTE 셀 신호 강도 데이터 목록. */
    public val cellDataLteList: MutableList<CellSignalStrengthLteData> = mutableListOf()

    /** List of NR (5G) cell signal strength data.<br>NR (5G) 셀 신호 강도 데이터 목록. */
    public val cellDataNrList: MutableList<CellSignalStrengthNrData> = mutableListOf()

    /** List of TDSCDMA cell signal strength data.<br>TDSCDMA 셀 신호 강도 데이터 목록. */
    public val cellDataTdscdmaList: MutableList<CellSignalStrengthDataTdscdma> = mutableListOf()

    /** List of WCDMA cell signal strength data.<br>WCDMA 셀 신호 강도 데이터 목록. */
    public val cellDataWcdmaList: MutableList<CellSignalStrengthWcdmaData> = mutableListOf()

    init {
        cellDataList = signalStrength?.cellSignalStrengths as List<CellSignalStrength>
        cellDataList?.let {
            it.forEach { item ->

                when (item) {
                    is CellSignalStrengthLte -> {
                        cellDataLteList.add(CellSignalStrengthLteData(item))
                    }
                    is CellSignalStrengthWcdma -> {
                        cellDataWcdmaList.add(CellSignalStrengthWcdmaData(item))
                    }
                    is CellSignalStrengthCdma -> {
                        cellDataCdmaList.add(CellSignalStrengthCdmaData(item))
                    }
                    is CellSignalStrengthGsm -> {
                        cellDataGsmList.add(CellSignalStrengthGsmData(item))
                    }
                    else -> {
                        checkSdkVersion(Build.VERSION_CODES.Q) {
                            when (item) {
                                is CellSignalStrengthTdscdma -> {
                                    cellDataTdscdmaList.add(CellSignalStrengthDataTdscdma(item))
                                }
                                is CellSignalStrengthNr -> {
                                    cellDataNrList.add(CellSignalStrengthNrData(item))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Converts all signal strength data lists to a readable string.<br><br>
     * 모든 신호 강도 데이터 목록을 읽기 쉬운 문자열로 변환합니다.<br>
     *
     * @return Formatted string representation.<br><br>
     *         포맷된 문자열 표현.
     */
    public fun toResString(): String {
        var res: String = ""
        res += "cellDataGsmList ${cellDataGsmList.toList()}\n" +
            "cellDataCdmaList ${cellDataCdmaList.toList()}\n" +
            "cellDataWcdmaList ${cellDataWcdmaList.toList()}\n" +
            "cellDataTdscdmaList ${cellDataTdscdmaList.toList()}\n" +
            "cellDataLteList ${cellDataLteList.toList()}\n" +
            "cellDataNrList ${cellDataNrList.toList()}\n\n"
        return res
    }
}
