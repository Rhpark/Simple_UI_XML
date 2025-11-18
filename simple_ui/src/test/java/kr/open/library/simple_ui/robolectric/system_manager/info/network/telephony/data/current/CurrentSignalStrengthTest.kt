package kr.open.library.simple_ui.robolectric.system_manager.info.network.telephony.data.current

import android.os.Build
import android.telephony.*
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.current.CurrentSignalStrength
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
class CurrentSignalStrengthTest {

    @Test
    fun `populates signal strength lists`() {
        val signalStrength = mock(SignalStrength::class.java)
        val strengths = listOf(
            mock(CellSignalStrengthLte::class.java),
            mock(CellSignalStrengthWcdma::class.java),
            mock(CellSignalStrengthCdma::class.java),
            mock(CellSignalStrengthGsm::class.java),
            mock(CellSignalStrengthTdscdma::class.java),
            mock(CellSignalStrengthNr::class.java)
        )
        doReturn(strengths).`when`(signalStrength).cellSignalStrengths

        val current = CurrentSignalStrength(signalStrength)

        assertEquals(1, current.cellDataLteList.size)
        assertEquals(1, current.cellDataWcdmaList.size)
        assertEquals(1, current.cellDataCdmaList.size)
        assertEquals(1, current.cellDataGsmList.size)
        assertEquals(1, current.cellDataTdscdmaList.size)
        assertEquals(1, current.cellDataNrList.size)
    }

    @Test
    fun `handles empty signal list`() {
        val signalStrength = mock(SignalStrength::class.java)
        doReturn(emptyList<CellSignalStrength>()).`when`(signalStrength).cellSignalStrengths

        val current = CurrentSignalStrength(signalStrength)

        assertTrue(current.cellDataLteList.isEmpty())
        assertTrue(current.cellDataNrList.isEmpty())
    }

    @Test
    fun `toResString contains each captured list`() {
        val signalStrength = mock(SignalStrength::class.java)
        val lte = mock(CellSignalStrengthLte::class.java)
        val gsm = mock(CellSignalStrengthGsm::class.java)
        doReturn(listOf(lte, gsm)).`when`(signalStrength).cellSignalStrengths

        val current = CurrentSignalStrength(signalStrength)

        val result = current.toResString()
        assertTrue(result.contains("cellDataLteList"))
        assertTrue(result.contains("cellDataGsmList"))
        assertTrue(result.contains(current.cellDataLteList.first().toString()))
        assertTrue(result.contains(current.cellDataGsmList.first().toString()))
    }
}
