package kr.open.library.simple_ui.system_manager.info.network.telephony.data.current

import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.cdma.CellIdentityCdmaData
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.gsm.CellIdentityGsmData
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.lte.CellIdentityLteData
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.nr.CellIdentityNrData
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.tdscdma.CellIdentityTdscdmaData
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.wcdma.CellIdentityWcdmaData


/**
 * https://developer.android.com/reference/android/telephony/NetworkRegistrationInfo
 */
@RequiresApi(Build.VERSION_CODES.R)
public data class CurrentServiceState(val serviceState: ServiceState?) {
    
    public val cellDataNrList : MutableList<CellIdentityNrData> = mutableListOf()
    public val cellDataLteList : MutableList<CellIdentityLteData> = mutableListOf()
    public val cellDataWcdmaList : MutableList<CellIdentityWcdmaData> = mutableListOf()
    public val cellDataCdmaList : MutableList<CellIdentityCdmaData> = mutableListOf()
    public val cellDataGsmList  : MutableList<CellIdentityGsmData> = mutableListOf()
    public val cellDataTdscdmaList : MutableList<CellIdentityTdscdmaData> = mutableListOf()

    init {
        serviceState?.networkRegistrationInfoList?.forEach { item->
            val data = item.cellIdentity
            when(data) {
                is CellIdentityLte      ->  {   cellDataLteList.add(CellIdentityLteData(data as CellIdentityLte))  }
                is CellIdentityWcdma    ->  {   cellDataWcdmaList.add(CellIdentityWcdmaData(data as CellIdentityWcdma))   }
                is CellIdentityCdma     ->  {   cellDataCdmaList.add(CellIdentityCdmaData(data as CellIdentityCdma))   }
                is CellIdentityGsm      ->  {   cellDataGsmList.add(CellIdentityGsmData(data as CellIdentityGsm))    }
                is CellIdentityTdscdma  ->  {   cellDataTdscdmaList.add(CellIdentityTdscdmaData(data as CellIdentityTdscdma))   }
                else -> {
                    checkSdkVersion(Build.VERSION_CODES.R) {
                        when(data) {
                            is CellIdentityNr       ->  {   cellDataNrList.add(CellIdentityNrData(data as CellIdentityNr))   }
                        }
                    }
                }
            }
        }
    }

    public fun getNetworkRegistrationInfo(index:Int):NetworkRegistrationInfo? = serviceState?.networkRegistrationInfoList?.get(index)


    /**
     * return type Int
     *
     * NetworkRegistrationInfo.DOMAIN_CS(1)
     * NetworkRegistrationInfo.DOMAIN_PS(2)
     * NetworkRegistrationInfo.DOMAIN_CS_PS(3)
     * NetworkRegistrationInfo.DOMAIN_UNKNOWN(0)
     */
    public fun getDomain(index:Int): Int? = getNetworkRegistrationInfo(index)?.domain

    /**
     * return type Int
     * AccessNetworkConstants.TRANSPORT_TYPE_WWAN(1)
     * AccessNetworkConstants.TRANSPORT_TYPE_WLAN(2)
     */
    public fun getTransportType(index:Int): Int? = getNetworkRegistrationInfo(index)?.transportType

    /**
     * return type Int
     * TelephonyManager.NETWORK_TYPE_%%
     */
    public fun getAccessNetworkTechnology(index:Int): Int? = getNetworkRegistrationInfo(index)?.accessNetworkTechnology

    /**
     * return type Int
     *
     * NetworkRegistrationInfo.SERVICE_TYPE_UNKNOWN(0)
     * NetworkRegistrationInfo.SERVICE_TYPE_VOICE(1)
     * NetworkRegistrationInfo.SERVICE_TYPE_DATA(2)
     * NetworkRegistrationInfo.SERVICE_TYPE_SMS(3)
     * NetworkRegistrationInfo.SERVICE_TYPE_VIDEO(4)
     * NetworkRegistrationInfo.SERVICE_TYPE_EMERGENCY(5)
     */
    public fun getAvailableServices(index:Int): List<Int>? = getNetworkRegistrationInfo(index)?.availableServices

    public fun toResString() :String {
        var res :String = ""

        res =  " cellDataGsmList : ${cellDataGsmList.toList()}\n" +
                " cellDataCdmaList : ${cellDataCdmaList.toList()}\n" +
                " cellDataWcdmaList : ${cellDataWcdmaList.toList()}\n" +
                " cellDataTdscdmaList : ${cellDataTdscdmaList.toList()}\n" +
                " cellDataLteList : ${cellDataLteList.toList()}\n" +
                " cellDataNrList : ${cellDataNrList.toList()}\n\n"
        return res
    }
}