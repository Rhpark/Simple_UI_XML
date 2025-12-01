package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.cdma

import android.os.Build
import android.telephony.CellIdentityCdma
import android.telephony.CellInfoCdma
import android.telephony.CellSignalStrengthCdma
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.cdma.CellInfoCdmaData
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
 * Robolectric test for CellInfoCdmaData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellInfoCdmaDataRobolectricTest {
    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellInfo_createsInstance() {
        val data = CellInfoCdmaData(cellInfo = null)

        assertNotNull(data)
    }

    @Test
    fun constructor_withValidCellInfo_createsInstance() {
        val mockInfo = mock(CellInfoCdma::class.java)
        val mockIdentity = mock(CellIdentityCdma::class.java)
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoCdmaData(cellInfo = mockInfo)

        assertNotNull(data)
    }

    // ==============================================
    // Timestamp Millis Tests (API 30+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getTimestampMillis_withNullCellInfo_returnsNull() {
        val data = CellInfoCdmaData(cellInfo = null)

        val result = data.getTimestampMillis()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getTimestampMillis_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoCdma::class.java)
        val mockIdentity = mock(CellIdentityCdma::class.java)
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(1234567890123L)

        val data = CellInfoCdmaData(cellInfo = mockInfo)

        val result = data.getTimestampMillis()

        assertEquals(1234567890123L, result)
    }

    // ==============================================
    // Cell Connection Status Tests
    // ==============================================

    @Test
    fun getCellConnectionStatus_withNullCellInfo_returnsNull() {
        val data = CellInfoCdmaData(cellInfo = null)

        val result = data.getCellConnectionStatus()

        assertNull(result)
    }

    @Test
    fun getCellConnectionStatus_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoCdma::class.java)
        val mockIdentity = mock(CellIdentityCdma::class.java)
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.cellConnectionStatus).thenReturn(1)

        val data = CellInfoCdmaData(cellInfo = mockInfo)

        val result = data.getCellConnectionStatus()

        assertEquals(1, result)
    }

    // ==============================================
    // Is Registered Tests
    // ==============================================

    @Test
    fun isRegistered_withNullCellInfo_returnsNull() {
        val data = CellInfoCdmaData(cellInfo = null)

        val result = data.isRegistered()

        assertNull(result)
    }

    @Test
    fun isRegistered_withTrueValue_returnsTrue() {
        val mockInfo = mock(CellInfoCdma::class.java)
        val mockIdentity = mock(CellIdentityCdma::class.java)
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(true)

        val data = CellInfoCdmaData(cellInfo = mockInfo)

        val result = data.isRegistered()

        assertTrue(result == true)
    }

    @Test
    fun isRegistered_withFalseValue_returnsFalse() {
        val mockInfo = mock(CellInfoCdma::class.java)
        val mockIdentity = mock(CellIdentityCdma::class.java)
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(false)

        val data = CellInfoCdmaData(cellInfo = mockInfo)

        val result = data.isRegistered()

        assertFalse(result == true)
    }

    // ==============================================
    // Identity Tests
    // ==============================================

    @Test
    fun getIdentity_withValidCellInfo_returnsIdentityData() {
        val mockInfo = mock(CellInfoCdma::class.java)
        val mockIdentity = mock(CellIdentityCdma::class.java)
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoCdmaData(cellInfo = mockInfo)

        val result = data.getIdentity()

        assertNotNull(result)
    }

    @Test
    fun getIdentity_withNullCellInfo_returnsIdentityDataWithNullIdentity() {
        val data = CellInfoCdmaData(cellInfo = null)

        val result = data.getIdentity()

        assertNotNull(result)
        assertNull(result.cellIdentity)
    }

    // ==============================================
    // Signal Strength Tests
    // ==============================================

    @Test
    fun getSignalStrength_withValidCellInfo_returnsSignalStrengthData() {
        val mockInfo = mock(CellInfoCdma::class.java)
        val mockIdentity = mock(CellIdentityCdma::class.java)
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoCdmaData(cellInfo = mockInfo)

        val result = data.getSignalStrength()

        assertNotNull(result)
    }

    @Test
    fun getSignalStrength_withNullCellInfo_returnsSignalStrengthDataWithNullSignal() {
        val data = CellInfoCdmaData(cellInfo = null)

        val result = data.getSignalStrength()

        assertNotNull(result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameInfoProducesSameData() {
        val mockInfo = mock(CellInfoCdma::class.java)
        val mockIdentity = mock(CellIdentityCdma::class.java)
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data1 = CellInfoCdmaData(cellInfo = mockInfo)
        val data2 = CellInfoCdmaData(cellInfo = mockInfo)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockInfo = mock(CellInfoCdma::class.java)
        val mockIdentity = mock(CellIdentityCdma::class.java)
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoCdmaData(cellInfo = mockInfo)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedInfo_returnCorrectValues() {
        val mockInfo = mock(CellInfoCdma::class.java)
        val mockIdentity = mock(CellIdentityCdma::class.java)
        val mockSignal = mock(CellSignalStrengthCdma::class.java)

        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(1234567890123L)
        `when`(mockInfo.cellConnectionStatus).thenReturn(1)
        `when`(mockInfo.isRegistered).thenReturn(true)

        val data = CellInfoCdmaData(cellInfo = mockInfo)

        assertEquals(1234567890123L, data.getTimestampMillis())
        assertEquals(1, data.getCellConnectionStatus())
        assertTrue(data.isRegistered() == true)
        assertNotNull(data.getIdentity())
        assertNotNull(data.getSignalStrength())
    }
}
