package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.current

import android.os.Build
import android.telephony.AccessNetworkConstants
import android.telephony.CellIdentityCdma
import android.telephony.CellIdentityGsm
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellIdentityTdscdma
import android.telephony.CellIdentityWcdma
import android.telephony.NetworkRegistrationInfo
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.current.CurrentServiceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class CurrentServiceStateTest {
    @Test
    fun `populates lists for each identity type`() {
        val serviceState = mock(ServiceState::class.java)
        val entries =
            listOf(
                mockNetworkRegistrationInfo(mock(CellIdentityLte::class.java)),
                mockNetworkRegistrationInfo(mock(CellIdentityWcdma::class.java)),
                mockNetworkRegistrationInfo(mock(CellIdentityCdma::class.java)),
                mockNetworkRegistrationInfo(mock(CellIdentityGsm::class.java)),
                mockNetworkRegistrationInfo(mock(CellIdentityTdscdma::class.java)),
                mockNetworkRegistrationInfo(mock(CellIdentityNr::class.java)),
            )
        doReturn(entries).`when`(serviceState).networkRegistrationInfoList

        val current = CurrentServiceState(serviceState)

        assertEquals(1, current.cellDataLteList.size)
        assertEquals(1, current.cellDataWcdmaList.size)
        assertEquals(1, current.cellDataCdmaList.size)
        assertEquals(1, current.cellDataGsmList.size)
        assertEquals(1, current.cellDataTdscdmaList.size)
        assertEquals(1, current.cellDataNrList.size)
    }

    private fun mockNetworkRegistrationInfo(identity: Any): NetworkRegistrationInfo {
        val info = mock(NetworkRegistrationInfo::class.java)
        doReturn(identity).`when`(info).cellIdentity
        return info
    }

    @Test
    fun `accessors expose registration info values`() {
        val serviceState = mock(ServiceState::class.java)
        val info = mock(NetworkRegistrationInfo::class.java)
        doReturn(listOf(info)).`when`(serviceState).networkRegistrationInfoList
        doReturn(NetworkRegistrationInfo.DOMAIN_PS).`when`(info).domain
        doReturn(AccessNetworkConstants.TRANSPORT_TYPE_WLAN).`when`(info).transportType
        doReturn(TelephonyManager.NETWORK_TYPE_LTE).`when`(info).accessNetworkTechnology
        doReturn(listOf(NetworkRegistrationInfo.SERVICE_TYPE_DATA)).`when`(info).availableServices

        val current = CurrentServiceState(serviceState)

        assertEquals(info, current.getNetworkRegistrationInfo(0))
        assertEquals(NetworkRegistrationInfo.DOMAIN_PS, current.getDomain(0))
        assertEquals(AccessNetworkConstants.TRANSPORT_TYPE_WLAN, current.getTransportType(0))
        assertEquals(TelephonyManager.NETWORK_TYPE_LTE, current.getAccessNetworkTechnology(0))
        assertEquals(listOf(NetworkRegistrationInfo.SERVICE_TYPE_DATA), current.getAvailableServices(0))
    }

    @Test
    fun `toResString contains populated lists`() {
        val serviceState = mock(ServiceState::class.java)
        val gsmInfo = mockNetworkRegistrationInfo(mock(CellIdentityGsm::class.java))
        val lteInfo = mockNetworkRegistrationInfo(mock(CellIdentityLte::class.java))
        doReturn(listOf(gsmInfo, lteInfo)).`when`(serviceState).networkRegistrationInfoList

        val current = CurrentServiceState(serviceState)

        val result = current.toResString()
        assertTrue(result.contains("cellDataGsmList"))
        assertTrue(result.contains("cellDataLteList"))
        assertTrue(result.contains(current.cellDataGsmList.first().toString()))
        assertTrue(result.contains(current.cellDataLteList.first().toString()))
    }
}
