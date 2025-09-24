package kr.open.library.simple_ui.system_manager.info.network.telephony.data.current

import android.os.Build
import android.telephony.*
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.cdma.CellInfoCdmaData
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.gsm.CellInfoGsmData
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.lte.CellInfoLteData
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.nr.CellInfoNrData
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.tdscdma.CellInfoTdscdmaData
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.wcdma.CellInfoWcdmaData


public data class CurrentCellInfo(val cellInfo: List<CellInfo>) {
    public val cellDataCdmaList: MutableList<CellInfoCdmaData> = mutableListOf()
    public val cellDataGsmList  : MutableList<CellInfoGsmData> = mutableListOf()
    public val cellDataLteList : MutableList<CellInfoLteData> = mutableListOf()
    public val cellDataNrList : MutableList<CellInfoNrData> = mutableListOf()
    public val cellDataTdscdmaList : MutableList<CellInfoTdscdmaData> = mutableListOf()
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
    public fun toResString() :String {
        val res = "cellDataGsmList ${cellDataGsmList.toList()}\n" +
                "cellDataCdmaList ${cellDataCdmaList.toList()}\n"
                "cellDataWcdmaList ${cellDataWcdmaList.toList()}\n"
                "cellDataTdscdmaList ${cellDataTdscdmaList.toList()}\n"
                "cellDataLteList ${cellDataLteList.toList()}\n"
                "cellDataNrList ${cellDataNrList.toList()}\n\n"
        return res
    }
}