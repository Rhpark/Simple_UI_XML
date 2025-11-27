package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.lte

import android.os.Build
import android.telephony.CellIdentityLte
import android.telephony.CellInfoLte
import android.telephony.CellSignalStrengthLte
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.lte.CellInfoLteData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test for CellInfoLteData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellInfoLteDataRobolectricTest {

    @Test
    fun constructor_withNullCellInfo_createsInstance() {
        val data = CellInfoLteData(cellInfo = null)

        assertNotNull(data)
    }

    @Test
    fun constructor_withValidCellInfo_createsInstance() {
        val mockInfo = mock(CellInfoLte::class.java)
        val mockIdentity = mock(CellIdentityLte::class.java)
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoLteData(cellInfo = mockInfo)

        assertNotNull(data)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getTimestampMillis_withNullCellInfo_returnsNull() {
        val data = CellInfoLteData(cellInfo = null)

        val result = data.getTimestampMillis()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getTimestampMillis_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoLte::class.java)
        val mockIdentity = mock(CellIdentityLte::class.java)
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(5555555555555L)

        val data = CellInfoLteData(cellInfo = mockInfo)

        assertEquals(5555555555555L, data.getTimestampMillis())
    }

    @Test
    fun getCellConnectionStatus_withNullCellInfo_returnsNull() {
        val data = CellInfoLteData(cellInfo = null)

        val result = data.getCellConnectionStatus()

        assertNull(result)
    }

    @Test
    fun getCellConnectionStatus_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoLte::class.java)
        val mockIdentity = mock(CellIdentityLte::class.java)
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.cellConnectionStatus).thenReturn(1)

        val data = CellInfoLteData(cellInfo = mockInfo)

        assertEquals(1, data.getCellConnectionStatus())
    }

    @Test
    fun isRegistered_withNullCellInfo_returnsNull() {
        val data = CellInfoLteData(cellInfo = null)

        val result = data.isRegistered()

        assertNull(result)
    }

    @Test
    fun isRegistered_withTrueValue_returnsTrue() {
        val mockInfo = mock(CellInfoLte::class.java)
        val mockIdentity = mock(CellIdentityLte::class.java)
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(true)

        val data = CellInfoLteData(cellInfo = mockInfo)

        assertTrue(data.isRegistered() == true)
    }

    @Test
    fun isRegistered_withFalseValue_returnsFalse() {
        val mockInfo = mock(CellInfoLte::class.java)
        val mockIdentity = mock(CellIdentityLte::class.java)
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(false)

        val data = CellInfoLteData(cellInfo = mockInfo)

        assertFalse(data.isRegistered() == true)
    }

    @Test
    fun getCellIdentity_withValidCellInfo_returnsIdentityData() {
        val mockInfo = mock(CellInfoLte::class.java)
        val mockIdentity = mock(CellIdentityLte::class.java)
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoLteData(cellInfo = mockInfo)

        val result = data.getCellIdentity()

        assertNotNull(result)
    }

    @Test
    fun getCellSignalStrength_withValidCellInfo_returnsSignalStrengthData() {
        val mockInfo = mock(CellInfoLte::class.java)
        val mockIdentity = mock(CellIdentityLte::class.java)
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoLteData(cellInfo = mockInfo)

        val result = data.getCellSignalStrength()

        assertNotNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedInfo_returnCorrectValues() {
        val mockInfo = mock(CellInfoLte::class.java)
        val mockIdentity = mock(CellIdentityLte::class.java)
        val mockSignal = mock(CellSignalStrengthLte::class.java)

        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(5555555555555L)
        `when`(mockInfo.cellConnectionStatus).thenReturn(1)
        `when`(mockInfo.isRegistered).thenReturn(true)

        val data = CellInfoLteData(cellInfo = mockInfo)

        assertEquals(5555555555555L, data.getTimestampMillis())
        assertEquals(1, data.getCellConnectionStatus())
        assertTrue(data.isRegistered() == true)
        assertNotNull(data.getCellIdentity())
        assertNotNull(data.getCellSignalStrength())
    }
}
