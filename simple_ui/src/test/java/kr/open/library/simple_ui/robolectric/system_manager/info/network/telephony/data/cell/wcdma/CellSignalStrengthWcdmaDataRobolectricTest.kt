package kr.open.library.simple_ui.robolectric.system_manager.info.network.telephony.data.cell.wcdma

import android.os.Build
import android.telephony.CellSignalStrengthWcdma
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.wcdma.CellSignalStrengthWcdmaData
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
 * Robolectric test for CellSignalStrengthWcdmaData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellSignalStrengthWcdmaDataRobolectricTest {

    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellSignalStrength_createsInstance() {
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = null)

        assertNotNull(data)
    }

    @Test
    fun constructor_withValidCellSignalStrength_createsInstance() {
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = mockSignal)

        assertNotNull(data)
        assertNotNull(data.cellSignalStrength)
    }

    // ==============================================
    // EcNo Tests (API 30+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getEcNo_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = null)

        val result = data.getEcNo()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getEcNo_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockSignal.ecNo).thenReturn(-105)
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = mockSignal)

        val result = data.getEcNo()

        assertEquals(-105, result)
    }

    // ==============================================
    // Level Tests
    // ==============================================

    @Test
    fun getLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = null)

        val result = data.getLevel()

        assertNull(result)
    }

    @Test
    fun getLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockSignal.level).thenReturn(3)
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = mockSignal)

        val result = data.getLevel()

        assertEquals(3, result)
    }

    // ==============================================
    // ASU Level Tests
    // ==============================================

    @Test
    fun getAsuLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = null)

        val result = data.getAsuLevel()

        assertNull(result)
    }

    @Test
    fun getAsuLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockSignal.asuLevel).thenReturn(35)
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = mockSignal)

        val result = data.getAsuLevel()

        assertEquals(35, result)
    }

    // ==============================================
    // ASU DBM Tests (same as RSCP)
    // ==============================================

    @Test
    fun getAsuDbm_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = null)

        val result = data.getAsuDbm()

        assertNull(result)
    }

    @Test
    fun getAsuDbm_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockSignal.dbm).thenReturn(-82)
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = mockSignal)

        val result = data.getAsuDbm()

        assertEquals(-82, result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameSignalProducesSameData() {
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        val data1 = CellSignalStrengthWcdmaData(cellSignalStrength = mockSignal)
        val data2 = CellSignalStrengthWcdmaData(cellSignalStrength = mockSignal)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_copy_createsNewInstance() {
        val mockSignal1 = mock(CellSignalStrengthWcdma::class.java)
        val mockSignal2 = mock(CellSignalStrengthWcdma::class.java)
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = mockSignal1)

        val copied = data.copy(cellSignalStrength = mockSignal2)

        assertEquals(mockSignal1, data.cellSignalStrength)
        assertEquals(mockSignal2, copied.cellSignalStrength)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        val data = CellSignalStrengthWcdmaData(cellSignalStrength = mockSignal)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedSignal_returnCorrectValues() {
        val mockSignal = mock(CellSignalStrengthWcdma::class.java)
        `when`(mockSignal.ecNo).thenReturn(-105)
        `when`(mockSignal.level).thenReturn(3)
        `when`(mockSignal.asuLevel).thenReturn(35)
        `when`(mockSignal.dbm).thenReturn(-82)

        val data = CellSignalStrengthWcdmaData(cellSignalStrength = mockSignal)

        assertEquals(-105, data.getEcNo())
        assertEquals(3, data.getLevel())
        assertEquals(35, data.getAsuLevel())
        assertEquals(-82, data.getAsuDbm())
    }
}
