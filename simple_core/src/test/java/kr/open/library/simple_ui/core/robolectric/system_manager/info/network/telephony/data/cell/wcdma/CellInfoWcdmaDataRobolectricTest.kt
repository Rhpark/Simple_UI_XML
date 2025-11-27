package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.wcdma

import android.os.Build
import android.telephony.CellIdentityWcdma
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrengthWcdma
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.wcdma.CellInfoWcdmaData
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
 * Robolectric test for CellInfoWcdmaData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellInfoWcdmaDataRobolectricTest {

    @Test
    fun constructor_withNullCellInfo_createsInstance() {
        val data = CellInfoWcdmaData(cellInfo = null)

        assertNotNull(data)
        assertNull(data.cellInfo)
    }

    @Test
    fun constructor_withValidCellInfo_createsInstance() {
        val mockInfo = mock(CellInfoWcdma::class.java)
        val mockIdentity = mock(CellIdentityWcdma::class.java)
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoWcdmaData(cellInfo = mockInfo)

        assertNotNull(data)
        assertNotNull(data.cellInfo)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getTimestampMillis_withNullCellInfo_returnsNull() {
        val data = CellInfoWcdmaData(cellInfo = null)

        val result = data.getTimestampMillis()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getTimestampMillis_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoWcdma::class.java)
        val mockIdentity = mock(CellIdentityWcdma::class.java)
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(6666666666666L)

        val data = CellInfoWcdmaData(cellInfo = mockInfo)

        assertEquals(6666666666666L, data.getTimestampMillis())
    }

    @Test
    fun isRegistered_withNullCellInfo_returnsNull() {
        val data = CellInfoWcdmaData(cellInfo = null)

        val result = data.isRegistered()

        assertNull(result)
    }

    @Test
    fun isRegistered_withTrueValue_returnsTrue() {
        val mockInfo = mock(CellInfoWcdma::class.java)
        val mockIdentity = mock(CellIdentityWcdma::class.java)
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(true)

        val data = CellInfoWcdmaData(cellInfo = mockInfo)

        assertTrue(data.isRegistered() == true)
    }

    @Test
    fun isRegistered_withFalseValue_returnsFalse() {
        val mockInfo = mock(CellInfoWcdma::class.java)
        val mockIdentity = mock(CellIdentityWcdma::class.java)
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(false)

        val data = CellInfoWcdmaData(cellInfo = mockInfo)

        assertFalse(data.isRegistered() == true)
    }

    @Test
    fun getCellConnectionStatus_withNullCellInfo_returnsNull() {
        val data = CellInfoWcdmaData(cellInfo = null)

        val result = data.getCellConnectionStatus()

        assertNull(result)
    }

    @Test
    fun getCellConnectionStatus_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoWcdma::class.java)
        val mockIdentity = mock(CellIdentityWcdma::class.java)
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.cellConnectionStatus).thenReturn(2)

        val data = CellInfoWcdmaData(cellInfo = mockInfo)

        assertEquals(2, data.getCellConnectionStatus())
    }

    @Test
    fun getIdentity_withValidCellInfo_returnsIdentityData() {
        val mockInfo = mock(CellInfoWcdma::class.java)
        val mockIdentity = mock(CellIdentityWcdma::class.java)
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoWcdmaData(cellInfo = mockInfo)

        val result = data.getIdentity()

        assertNotNull(result)
    }

    @Test
    fun getIdentity_withNullCellInfo_returnsIdentityDataWithNullIdentity() {
        val data = CellInfoWcdmaData(cellInfo = null)

        val result = data.getIdentity()

        assertNotNull(result)
        assertNull(result.cellIdentity)
    }

    @Test
    fun getSignalStrength_withValidCellInfo_returnsSignalStrengthData() {
        val mockInfo = mock(CellInfoWcdma::class.java)
        val mockIdentity = mock(CellIdentityWcdma::class.java)
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoWcdmaData(cellInfo = mockInfo)

        val result = data.getSignalStrength()

        assertNotNull(result)
    }

    @Test
    fun getSignalStrength_withNullCellInfo_returnsSignalStrengthDataWithNullSignal() {
        val data = CellInfoWcdmaData(cellInfo = null)

        val result = data.getSignalStrength()

        assertNotNull(result)
    }

    @Test
    fun dataClass_equality_sameInfoProducesSameData() {
        val mockInfo = mock(CellInfoWcdma::class.java)
        val mockIdentity = mock(CellIdentityWcdma::class.java)
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data1 = CellInfoWcdmaData(cellInfo = mockInfo)
        val data2 = CellInfoWcdmaData(cellInfo = mockInfo)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockInfo = mock(CellInfoWcdma::class.java)
        val mockIdentity = mock(CellIdentityWcdma::class.java)
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoWcdmaData(cellInfo = mockInfo)

        val result = data.toString()

        assertNotNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedInfo_returnCorrectValues() {
        val mockInfo = mock(CellInfoWcdma::class.java)
        val mockIdentity = mock(CellIdentityWcdma::class.java)
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)

        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(6666666666666L)
        `when`(mockInfo.isRegistered).thenReturn(true)
        `when`(mockInfo.cellConnectionStatus).thenReturn(2)

        val data = CellInfoWcdmaData(cellInfo = mockInfo)

        assertEquals(6666666666666L, data.getTimestampMillis())
        assertTrue(data.isRegistered() == true)
        assertEquals(2, data.getCellConnectionStatus())
        assertNotNull(data.getIdentity())
        assertNotNull(data.getSignalStrength())
    }
}
