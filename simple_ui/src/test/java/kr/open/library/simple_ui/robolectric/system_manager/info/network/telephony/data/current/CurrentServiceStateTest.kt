package kr.open.library.simple_ui.robolectric.system_manager.info.network.telephony.data.current

import android.os.Build
import android.telephony.CellIdentityCdma
import android.telephony.CellIdentityGsm
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellIdentityTdscdma
import android.telephony.CellIdentityWcdma
import android.telephony.NetworkRegistrationInfo
import android.telephony.ServiceState
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentServiceState
import org.junit.Assert.assertEquals
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
        val entries = listOf(
            mockNetworkRegistrationInfo(mock(CellIdentityLte::class.java)),
            mockNetworkRegistrationInfo(mock(CellIdentityWcdma::class.java)),
            mockNetworkRegistrationInfo(mock(CellIdentityCdma::class.java)),
            mockNetworkRegistrationInfo(mock(CellIdentityGsm::class.java)),
            mockNetworkRegistrationInfo(mock(CellIdentityTdscdma::class.java)),
            mockNetworkRegistrationInfo(mock(CellIdentityNr::class.java))
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
}
