package kr.open.library.simple_ui.robolectric.system_manager.info.network.telephony.data.current

import android.os.Build
import android.telephony.CellIdentityNr
import android.telephony.CellIdentityTdscdma
import android.telephony.CellSignalStrengthNr
import android.telephony.CellSignalStrengthTdscdma
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoTdscdma
import android.telephony.CellInfoWcdma
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentCellInfo
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
class CurrentCellInfoTest {

    @Test
    fun `populates lists for every cell info type`() {
        val lte = mock(CellInfoLte::class.java)
        val wcdma = mock(CellInfoWcdma::class.java)
        val cdma = mock(CellInfoCdma::class.java)
        val gsm = mock(CellInfoGsm::class.java)
        val nr = mock(CellInfoNr::class.java)
        val tdscdma = mock(CellInfoTdscdma::class.java)

        doReturn(mock(CellIdentityNr::class.java)).`when`(nr).cellIdentity
        doReturn(mock(CellSignalStrengthNr::class.java)).`when`(nr).cellSignalStrength
        doReturn(mock(CellIdentityTdscdma::class.java)).`when`(tdscdma).cellIdentity
        doReturn(mock(CellSignalStrengthTdscdma::class.java)).`when`(tdscdma).cellSignalStrength

        val info = CurrentCellInfo(listOf(lte, wcdma, cdma, gsm, nr, tdscdma))

        assertEquals(1, info.cellDataLteList.size)
        assertEquals(1, info.cellDataWcdmaList.size)
        assertEquals(1, info.cellDataCdmaList.size)
        assertEquals(1, info.cellDataGsmList.size)
        assertEquals(1, info.cellDataNrList.size)
        assertEquals(1, info.cellDataTdscdmaList.size)
    }

    @Test
    fun `toResString includes all categories`() {
        val nr = mock(CellInfoNr::class.java)
        val tdscdma = mock(CellInfoTdscdma::class.java)
        doReturn(mock(CellIdentityNr::class.java)).`when`(nr).cellIdentity
        doReturn(mock(CellSignalStrengthNr::class.java)).`when`(nr).cellSignalStrength
        doReturn(mock(CellIdentityTdscdma::class.java)).`when`(tdscdma).cellIdentity
        doReturn(mock(CellSignalStrengthTdscdma::class.java)).`when`(tdscdma).cellSignalStrength

        val info = CurrentCellInfo(listOf(
            mock(CellInfoGsm::class.java),
            mock(CellInfoCdma::class.java),
            mock(CellInfoLte::class.java),
            mock(CellInfoWcdma::class.java),
            tdscdma,
            nr
        ))

        val output = info.toResString()

        listOf("cellDataGsmList", "cellDataCdmaList", "cellDataWcdmaList",
            "cellDataTdscdmaList", "cellDataLteList", "cellDataNrList").forEach {
            assertTrue("Expected $it in output", output.contains(it))
        }
    }
}
