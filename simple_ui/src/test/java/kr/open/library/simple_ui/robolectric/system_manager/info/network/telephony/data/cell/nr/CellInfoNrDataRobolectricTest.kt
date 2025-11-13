package kr.open.library.simple_ui.robolectric.system_manager.info.network.telephony.data.cell.nr

import android.os.Build
import android.telephony.CellIdentityNr
import android.telephony.CellInfoNr
import android.telephony.CellSignalStrengthNr
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.nr.CellInfoNrData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test for CellInfoNrData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellInfoNrDataRobolectricTest {

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun constructor_withValidCellInfo_createsInstance() {
        val mockInfo = mock(CellInfoNr::class.java)
        val mockIdentity = mock(CellIdentityNr::class.java)
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoNrData(cellInfo = mockInfo)

        assertNotNull(data)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getTimestampMillis_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoNr::class.java)
        val mockIdentity = mock(CellIdentityNr::class.java)
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(7777777777777L)

        val data = CellInfoNrData(cellInfo = mockInfo)

        assertEquals(7777777777777L, data.getTimestampMillis())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun isRegistered_withTrueValue_returnsTrue() {
        val mockInfo = mock(CellInfoNr::class.java)
        val mockIdentity = mock(CellIdentityNr::class.java)
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(true)

        val data = CellInfoNrData(cellInfo = mockInfo)

        assertTrue(data.isRegistered())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun isRegistered_withFalseValue_returnsFalse() {
        val mockInfo = mock(CellInfoNr::class.java)
        val mockIdentity = mock(CellIdentityNr::class.java)
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(false)

        val data = CellInfoNrData(cellInfo = mockInfo)

        assertFalse(data.isRegistered())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getCellConnectionStatus_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoNr::class.java)
        val mockIdentity = mock(CellIdentityNr::class.java)
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.cellConnectionStatus).thenReturn(2)

        val data = CellInfoNrData(cellInfo = mockInfo)

        assertEquals(2, data.getCellConnectionStatus())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getIdentity_withValidCellInfo_returnsIdentityData() {
        val mockInfo = mock(CellInfoNr::class.java)
        val mockIdentity = mock(CellIdentityNr::class.java)
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoNrData(cellInfo = mockInfo)

        val result = data.getIdentity()

        assertNotNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getSignalStrength_withValidCellInfo_returnsSignalStrengthData() {
        val mockInfo = mock(CellInfoNr::class.java)
        val mockIdentity = mock(CellIdentityNr::class.java)
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoNrData(cellInfo = mockInfo)

        val result = data.getSignalStrength()

        assertNotNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedInfo_returnCorrectValues() {
        val mockInfo = mock(CellInfoNr::class.java)
        val mockIdentity = mock(CellIdentityNr::class.java)
        val mockSignal = mock(CellSignalStrengthNr::class.java)

        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(7777777777777L)
        `when`(mockInfo.isRegistered).thenReturn(true)
        `when`(mockInfo.cellConnectionStatus).thenReturn(2)

        val data = CellInfoNrData(cellInfo = mockInfo)

        assertEquals(7777777777777L, data.getTimestampMillis())
        assertTrue(data.isRegistered())
        assertEquals(2, data.getCellConnectionStatus())
        assertNotNull(data.getIdentity())
        assertNotNull(data.getSignalStrength())
    }
}
