package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.gsm

import android.os.Build
import android.telephony.CellIdentityGsm
import android.telephony.CellInfoGsm
import android.telephony.CellSignalStrengthGsm
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.gsm.CellInfoGsmData
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
 * Robolectric test for CellInfoGsmData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellInfoGsmDataRobolectricTest {
    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withValidCellInfo_createsInstance() {
        val mockInfo = mock(CellInfoGsm::class.java)
        val mockIdentity = mock(CellIdentityGsm::class.java)
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoGsmData(cellInfo = mockInfo)

        assertNotNull(data)
    }

    // ==============================================
    // Timestamp Millis Tests (API 30+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getTimestampMillis_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoGsm::class.java)
        val mockIdentity = mock(CellIdentityGsm::class.java)
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(9876543210123L)

        val data = CellInfoGsmData(cellInfo = mockInfo)

        val result = data.getTimestampMillis()

        assertEquals(9876543210123L, result)
    }

    // ==============================================
    // Cell Connection Status Tests
    // ==============================================

    @Test
    fun getCellConnectionStatus_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoGsm::class.java)
        val mockIdentity = mock(CellIdentityGsm::class.java)
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.cellConnectionStatus).thenReturn(2)

        val data = CellInfoGsmData(cellInfo = mockInfo)

        val result = data.getCellConnectionStatus()

        assertEquals(2, result)
    }

    // ==============================================
    // Is Registered Tests
    // ==============================================

    @Test
    fun isRegistered_withTrueValue_returnsTrue() {
        val mockInfo = mock(CellInfoGsm::class.java)
        val mockIdentity = mock(CellIdentityGsm::class.java)
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(true)

        val data = CellInfoGsmData(cellInfo = mockInfo)

        val result = data.isRegistered()

        assertTrue(result)
    }

    @Test
    fun isRegistered_withFalseValue_returnsFalse() {
        val mockInfo = mock(CellInfoGsm::class.java)
        val mockIdentity = mock(CellIdentityGsm::class.java)
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(false)

        val data = CellInfoGsmData(cellInfo = mockInfo)

        val result = data.isRegistered()

        assertFalse(result)
    }

    // ==============================================
    // Identity Tests
    // ==============================================

    @Test
    fun getIdentity_withValidCellInfo_returnsIdentityData() {
        val mockInfo = mock(CellInfoGsm::class.java)
        val mockIdentity = mock(CellIdentityGsm::class.java)
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoGsmData(cellInfo = mockInfo)

        val result = data.getIdentity()

        assertNotNull(result)
        assertNotNull(result.cellIdentity)
    }

    // ==============================================
    // Signal Strength Tests
    // ==============================================

    @Test
    fun getSignalStrength_withValidCellInfo_returnsSignalStrengthData() {
        val mockInfo = mock(CellInfoGsm::class.java)
        val mockIdentity = mock(CellIdentityGsm::class.java)
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoGsmData(cellInfo = mockInfo)

        val result = data.getSignalStrength()

        assertNotNull(result)
        assertNotNull(result.cellSignalStrength)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameInfoProducesSameData() {
        val mockInfo = mock(CellInfoGsm::class.java)
        val mockIdentity = mock(CellIdentityGsm::class.java)
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data1 = CellInfoGsmData(cellInfo = mockInfo)
        val data2 = CellInfoGsmData(cellInfo = mockInfo)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockInfo = mock(CellInfoGsm::class.java)
        val mockIdentity = mock(CellIdentityGsm::class.java)
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoGsmData(cellInfo = mockInfo)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedInfo_returnCorrectValues() {
        val mockInfo = mock(CellInfoGsm::class.java)
        val mockIdentity = mock(CellIdentityGsm::class.java)
        val mockSignal = mock(CellSignalStrengthGsm::class.java)

        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(9876543210123L)
        `when`(mockInfo.cellConnectionStatus).thenReturn(2)
        `when`(mockInfo.isRegistered).thenReturn(true)

        val data = CellInfoGsmData(cellInfo = mockInfo)

        assertEquals(9876543210123L, data.getTimestampMillis())
        assertEquals(2, data.getCellConnectionStatus())
        assertTrue(data.isRegistered())
        assertNotNull(data.getIdentity())
        assertNotNull(data.getSignalStrength())
    }
}
