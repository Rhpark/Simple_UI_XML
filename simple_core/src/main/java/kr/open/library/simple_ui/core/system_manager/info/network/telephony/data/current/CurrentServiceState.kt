package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current

import android.os.Build
import android.telephony.CellIdentityCdma
import android.telephony.CellIdentityGsm
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellIdentityTdscdma
import android.telephony.CellIdentityWcdma
import android.telephony.NetworkRegistrationInfo
import android.telephony.ServiceState
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.cdma.CellIdentityCdmaData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.gsm.CellIdentityGsmData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.lte.CellIdentityLteData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.nr.CellIdentityNrData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.tdscdma.CellIdentityTdscdmaData
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.wcdma.CellIdentityWcdmaData

/**
 * Wrapper class for current service state.<br><br>
 * 현재 서비스 상태를 위한 래퍼 클래스입니다.<br>
 *
 * Extracts CellIdentity information and provides helpers for NetworkRegistrationInfo.<br><br>
 * CellIdentity 정보를 추출하고 NetworkRegistrationInfo를 위한 헬퍼를 제공합니다.<br>
 *
 * @see android.telephony.NetworkRegistrationInfo
 *
 * @property serviceState The original ServiceState object.<br><br>
 *                        원본 ServiceState 객체.
 */
@RequiresApi(Build.VERSION_CODES.R)
public data class CurrentServiceState(
    val serviceState: ServiceState?,
) {
    /** List of NR (5G) cell identity data.<br>NR (5G) 셀 식별 데이터 목록. */
    public val cellDataNrList: MutableList<CellIdentityNrData> = mutableListOf()

    /** List of LTE cell identity data.<br>LTE 셀 식별 데이터 목록. */
    public val cellDataLteList: MutableList<CellIdentityLteData> = mutableListOf()

    /** List of WCDMA cell identity data.<br>WCDMA 셀 식별 데이터 목록. */
    public val cellDataWcdmaList: MutableList<CellIdentityWcdmaData> = mutableListOf()

    /** List of CDMA cell identity data.<br>CDMA 셀 식별 데이터 목록. */
    public val cellDataCdmaList: MutableList<CellIdentityCdmaData> = mutableListOf()

    /** List of GSM cell identity data.<br>GSM 셀 식별 데이터 목록. */
    public val cellDataGsmList: MutableList<CellIdentityGsmData> = mutableListOf()

    /** List of TDSCDMA cell identity data.<br>TDSCDMA 셀 식별 데이터 목록. */
    public val cellDataTdscdmaList: MutableList<CellIdentityTdscdmaData> = mutableListOf()

    init {
        serviceState?.networkRegistrationInfoList?.forEach { item ->
            val data = item.cellIdentity
            when (data) {
                is CellIdentityLte -> {
                    cellDataLteList.add(CellIdentityLteData(data as CellIdentityLte))
                }
                is CellIdentityWcdma -> {
                    cellDataWcdmaList.add(CellIdentityWcdmaData(data as CellIdentityWcdma))
                }
                is CellIdentityCdma -> {
                    cellDataCdmaList.add(CellIdentityCdmaData(data as CellIdentityCdma))
                }
                is CellIdentityGsm -> {
                    cellDataGsmList.add(CellIdentityGsmData(data as CellIdentityGsm))
                }
                is CellIdentityTdscdma -> {
                    cellDataTdscdmaList.add(CellIdentityTdscdmaData(data as CellIdentityTdscdma))
                }
                else -> {
                    checkSdkVersion(Build.VERSION_CODES.R) {
                        when (data) {
                            is CellIdentityNr -> {
                                cellDataNrList.add(CellIdentityNrData(data as CellIdentityNr))
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets NetworkRegistrationInfo at the specified index.<br><br>
     * 지정된 인덱스의 NetworkRegistrationInfo를 가져옵니다.<br>
     *
     * @param index Index in the list.<br><br>
     *              리스트 내 인덱스.
     * @return NetworkRegistrationInfo, or null.<br><br>
     *         NetworkRegistrationInfo, 또는 null.
     */
    public fun getNetworkRegistrationInfo(index: Int): NetworkRegistrationInfo? = serviceState?.networkRegistrationInfoList?.get(index)

    /**
     * Gets the domain from NetworkRegistrationInfo.<br><br>
     * NetworkRegistrationInfo에서 도메인을 가져옵니다.<br>
     *
     * Values:<br>
     * - `NetworkRegistrationInfo.DOMAIN_CS` (1)<br>
     * - `NetworkRegistrationInfo.DOMAIN_PS` (2)<br>
     * - `NetworkRegistrationInfo.DOMAIN_CS_PS` (3)<br>
     * - `NetworkRegistrationInfo.DOMAIN_UNKNOWN` (0)<br><br>
     * 값:<br>
     * - `NetworkRegistrationInfo.DOMAIN_CS` (1)<br>
     * - `NetworkRegistrationInfo.DOMAIN_PS` (2)<br>
     * - `NetworkRegistrationInfo.DOMAIN_CS_PS` (3)<br>
     * - `NetworkRegistrationInfo.DOMAIN_UNKNOWN` (0)<br>
     *
     * @param index Index in the list.<br><br>
     *              리스트 내 인덱스.
     * @return Domain value.<br><br>
     *         도메인 값.
     */
    public fun getDomain(index: Int): Int? = getNetworkRegistrationInfo(index)?.domain

    /**
     * Gets the transport type from NetworkRegistrationInfo.<br><br>
     * NetworkRegistrationInfo에서 전송 타입을 가져옵니다.<br>
     *
     * Values:<br>
     * - `AccessNetworkConstants.TRANSPORT_TYPE_WWAN` (1)<br>
     * - `AccessNetworkConstants.TRANSPORT_TYPE_WLAN` (2)<br><br>
     * 값:<br>
     * - `AccessNetworkConstants.TRANSPORT_TYPE_WWAN` (1)<br>
     * - `AccessNetworkConstants.TRANSPORT_TYPE_WLAN` (2)<br>
     *
     * @param index Index in the list.<br><br>
     *              리스트 내 인덱스.
     * @return Transport type value.<br><br>
     *         전송 타입 값.
     */
    public fun getTransportType(index: Int): Int? = getNetworkRegistrationInfo(index)?.transportType

    /**
     * Gets the access network technology from NetworkRegistrationInfo.<br><br>
     * NetworkRegistrationInfo에서 액세스 네트워크 기술을 가져옵니다.<br>
     *
     * Returns `TelephonyManager.NETWORK_TYPE_*` constants.<br><br>
     * `TelephonyManager.NETWORK_TYPE_*` 상수를 반환합니다.<br>
     *
     * @param index Index in the list.<br><br>
     *              리스트 내 인덱스.
     * @return Access network technology value.<br><br>
     *         액세스 네트워크 기술 값.
     */
    public fun getAccessNetworkTechnology(index: Int): Int? = getNetworkRegistrationInfo(index)?.accessNetworkTechnology

    /**
     * Gets the available services from NetworkRegistrationInfo.<br><br>
     * NetworkRegistrationInfo에서 사용 가능한 서비스를 가져옵니다.<br>
     *
     * Values:<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_UNKNOWN` (0)<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_VOICE` (1)<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_DATA` (2)<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_SMS` (3)<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_VIDEO` (4)<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_EMERGENCY` (5)<br><br>
     * 값:<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_UNKNOWN` (0)<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_VOICE` (1)<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_DATA` (2)<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_SMS` (3)<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_VIDEO` (4)<br>
     * - `NetworkRegistrationInfo.SERVICE_TYPE_EMERGENCY` (5)<br>
     *
     * @param index Index in the list.<br><br>
     *              리스트 내 인덱스.
     * @return List of available services.<br><br>
     *         사용 가능한 서비스 목록.
     */
    public fun getAvailableServices(index: Int): List<Int>? = getNetworkRegistrationInfo(index)?.availableServices

    /**
     * Converts all cell data lists to a readable string.<br><br>
     * 모든 셀 데이터 목록을 읽기 쉬운 문자열로 변환합니다.<br>
     *
     * @return Formatted string representation.<br><br>
     *         포맷된 문자열 표현.
     */
    public fun toResString(): String {
        var res: String = ""

        res = " cellDataGsmList : ${cellDataGsmList.toList()}\n" +
            " cellDataCdmaList : ${cellDataCdmaList.toList()}\n" +
            " cellDataWcdmaList : ${cellDataWcdmaList.toList()}\n" +
            " cellDataTdscdmaList : ${cellDataTdscdmaList.toList()}\n" +
            " cellDataLteList : ${cellDataLteList.toList()}\n" +
            " cellDataNrList : ${cellDataNrList.toList()}\n\n"
        return res
    }
}
