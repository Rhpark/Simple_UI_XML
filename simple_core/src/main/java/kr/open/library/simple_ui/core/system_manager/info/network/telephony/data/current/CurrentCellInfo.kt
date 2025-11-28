package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current

import android.os.Build
import android.telephony.*
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.cdma.CellInfoCdmaData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.gsm.CellInfoGsmData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.lte.CellInfoLteData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.nr.CellInfoNrData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.tdscdma.CellInfoTdscdmaData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.wcdma.CellInfoWcdmaData


/**
 * Wrapper class for current cell information.<br><br>
 * 현재 셀 정보를 위한 래퍼 클래스입니다.<br>
 *
 * Categorizes CellInfo into specific technology types (GSM, CDMA, LTE, NR, etc.).<br><br>
 * CellInfo를 특정 기술 유형(GSM, CDMA, LTE, NR 등)으로 분류합니다.<br>
 *
 * @property cellInfo The original list of CellInfo objects.<br><br>
 *                    원본 CellInfo 객체 목록.
 */
public data class CurrentCellInfo(val cellInfo: List<CellInfo>) {
    /** List of CDMA cell data.<br>CDMA 셀 데이터 목록. */
    public val cellDataCdmaList: MutableList<CellInfoCdmaData> = mutableListOf()
    /** List of GSM cell data.<br>GSM 셀 데이터 목록. */
    public val cellDataGsmList  : MutableList<CellInfoGsmData> = mutableListOf()
    /** List of LTE cell data.<br>LTE 셀 데이터 목록. */
    public val cellDataLteList : MutableList<CellInfoLteData> = mutableListOf()
    /** List of NR (5G) cell data.<br>NR (5G) 셀 데이터 목록. */
    public val cellDataNrList : MutableList<CellInfoNrData> = mutableListOf()
    /** List of TDSCDMA cell data.<br>TDSCDMA 셀 데이터 목록. */
    public val cellDataTdscdmaList : MutableList<CellInfoTdscdmaData> = mutableListOf()
    /** List of WCDMA cell data.<br>WCDMA 셀 데이터 목록. */
    public val cellDataWcdmaList : MutableList<CellInfoWcdmaData> = mutableListOf()

    init {
        cellInfo.forEach { item ->
            when(item) {
                is CellInfoLte      -> {   cellDataLteList.add(CellInfoLteData(item))  }
                is CellInfoWcdma    ->  {   cellDataWcdmaList.add(CellInfoWcdmaData(item))   }
                is CellInfoCdma     ->  {   cellDataCdmaList.add(CellInfoCdmaData(item))   }
                is CellInfoGsm      ->  {   cellDataGsmList.add(CellInfoGsmData(item))    }
            }
            checkSdkVersion(Build.VERSION_CODES.Q) {
                when(item) {
                    is CellInfoNr       ->  {   cellDataNrList.add(CellInfoNrData(item))   }
                    is CellInfoTdscdma  ->  {   cellDataTdscdmaList.add(CellInfoTdscdmaData(item))   }
                }
            }
        }
    }

    /**
     * Converts all cell data lists to a readable string.<br><br>
     * 모든 셀 데이터 목록을 읽기 쉬운 문자열로 변환합니다.<br>
     *
     * @return Formatted string representation.<br><br>
     *         포맷된 문자열 표현.
     */
    public fun toResString(): String = "cellDataGsmList ${cellDataGsmList.toList()}\n" +
            "cellDataCdmaList ${cellDataCdmaList.toList()}\n" +
            "cellDataWcdmaList ${cellDataWcdmaList.toList()}\n" +
            "cellDataTdscdmaList ${cellDataTdscdmaList.toList()}\n" +
            "cellDataLteList ${cellDataLteList.toList()}\n" +
            "cellDataNrList ${cellDataNrList.toList()}\n\n"
}