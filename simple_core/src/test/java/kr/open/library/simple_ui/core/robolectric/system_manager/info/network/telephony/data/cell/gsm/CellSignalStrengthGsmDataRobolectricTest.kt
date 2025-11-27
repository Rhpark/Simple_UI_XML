package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.gsm

import android.os.Build
import android.telephony.CellSignalStrengthGsm
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.gsm.CellSignalStrengthGsmData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test for CellSignalStrengthGsmData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellSignalStrengthGsmDataRobolectricTest {

    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellSignalStrength_createsInstance() {
        val data = CellSignalStrengthGsmData(cellSignalStrength = null)

        assertNotNull(data)
    }

    @Test
    fun constructor_withValidCellSignalStrength_createsInstance() {
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        val data = CellSignalStrengthGsmData(cellSignalStrength = mockSignal)

        assertNotNull(data)
        assertNotNull(data.cellSignalStrength)
    }

    // ==============================================
    // DBM Tests
    // ==============================================

    @Test
    fun getDbm_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthGsmData(cellSignalStrength = null)

        val result = data.getDbm()

        assertNull(result)
    }

    @Test
    fun getDbm_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockSignal.dbm).thenReturn(-75)
        val data = CellSignalStrengthGsmData(cellSignalStrength = mockSignal)

        val result = data.getDbm()

        assertEquals(-75, result)
    }

    // ==============================================
    // Level Tests
    // ==============================================

    @Test
    fun getLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthGsmData(cellSignalStrength = null)

        val result = data.getLevel()

        assertNull(result)
    }

    @Test
    fun getLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockSignal.level).thenReturn(4)
        val data = CellSignalStrengthGsmData(cellSignalStrength = mockSignal)

        val result = data.getLevel()

        assertEquals(4, result)
    }

    // ==============================================
    // ASU Level Tests
    // ==============================================

    @Test
    fun getAsuLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthGsmData(cellSignalStrength = null)

        val result = data.getAsuLevel()

        assertNull(result)
    }

    @Test
    fun getAsuLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockSignal.asuLevel).thenReturn(31)
        val data = CellSignalStrengthGsmData(cellSignalStrength = mockSignal)

        val result = data.getAsuLevel()

        assertEquals(31, result)
    }

    // ==============================================
    // RSSI Tests (API 30+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getRssi_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthGsmData(cellSignalStrength = null)

        val result = data.getRssi()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getRssi_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockSignal.rssi).thenReturn(-80)
        val data = CellSignalStrengthGsmData(cellSignalStrength = mockSignal)

        val result = data.getRssi()

        assertEquals(-80, result)
    }

    // ==============================================
    // Timing Advance Tests
    // ==============================================

    @Test
    fun getTimingAdvance_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthGsmData(cellSignalStrength = null)

        val result = data.getTimingAdvance()

        assertNull(result)
    }

    @Test
    fun getTimingAdvance_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockSignal.timingAdvance).thenReturn(5)
        val data = CellSignalStrengthGsmData(cellSignalStrength = mockSignal)

        val result = data.getTimingAdvance()

        assertEquals(5, result)
    }

    // ==============================================
    // Bit Error Rate Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getBitErrorRate_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthGsmData(cellSignalStrength = null)

        val result = data.getBitErrorRate()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getBitErrorRate_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockSignal.bitErrorRate).thenReturn(3)
        val data = CellSignalStrengthGsmData(cellSignalStrength = mockSignal)

        val result = data.getBitErrorRate()

        assertEquals(3, result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameSignalProducesSameData() {
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        val data1 = CellSignalStrengthGsmData(cellSignalStrength = mockSignal)
        val data2 = CellSignalStrengthGsmData(cellSignalStrength = mockSignal)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_copy_createsNewInstance() {
        val mockSignal1 = mock(CellSignalStrengthGsm::class.java)
        val mockSignal2 = mock(CellSignalStrengthGsm::class.java)
        val data = CellSignalStrengthGsmData(cellSignalStrength = mockSignal1)

        val copied = data.copy(cellSignalStrength = mockSignal2)

        assertEquals(mockSignal1, data.cellSignalStrength)
        assertEquals(mockSignal2, copied.cellSignalStrength)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        val data = CellSignalStrengthGsmData(cellSignalStrength = mockSignal)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedSignal_returnCorrectValues() {
        val mockSignal = mock(CellSignalStrengthGsm::class.java)
        `when`(mockSignal.dbm).thenReturn(-75)
        `when`(mockSignal.level).thenReturn(4)
        `when`(mockSignal.asuLevel).thenReturn(31)
        `when`(mockSignal.rssi).thenReturn(-80)
        `when`(mockSignal.timingAdvance).thenReturn(5)
        `when`(mockSignal.bitErrorRate).thenReturn(3)

        val data = CellSignalStrengthGsmData(cellSignalStrength = mockSignal)

        assertEquals(-75, data.getDbm())
        assertEquals(4, data.getLevel())
        assertEquals(31, data.getAsuLevel())
        assertEquals(-80, data.getRssi())
        assertEquals(5, data.getTimingAdvance())
        assertEquals(3, data.getBitErrorRate())
    }
}
