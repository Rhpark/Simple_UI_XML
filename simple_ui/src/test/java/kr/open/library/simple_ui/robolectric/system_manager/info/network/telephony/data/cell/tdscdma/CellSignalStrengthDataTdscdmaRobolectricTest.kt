package kr.open.library.simple_ui.robolectric.system_manager.info.network.telephony.data.cell.tdscdma

import android.os.Build
import android.telephony.CellSignalStrengthTdscdma
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.tdscdma.CellSignalStrengthDataTdscdma
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
 * Robolectric test for CellSignalStrengthDataTdscdma
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellSignalStrengthDataTdscdmaRobolectricTest {

    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellSignalStrength_createsInstance() {
        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = null)

        assertNotNull(data)
    }

    @Test
    fun constructor_withValidCellSignalStrength_createsInstance() {
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = mockSignal)

        assertNotNull(data)
    }

    // ==============================================
    // RSCP Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getRscp_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = null)

        val result = data.getRscp()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getRscp_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockSignal.rscp).thenReturn(-95)
        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = mockSignal)

        val result = data.getRscp()

        assertEquals(-95, result)
    }

    // ==============================================
    // Level Tests
    // ==============================================

    @Test
    fun getLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = null)

        val result = data.getLevel()

        assertNull(result)
    }

    @Test
    fun getLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockSignal.level).thenReturn(2)
        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = mockSignal)

        val result = data.getLevel()

        assertEquals(2, result)
    }

    // ==============================================
    // ASU Level Tests
    // ==============================================

    @Test
    fun getAsuLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = null)

        val result = data.getAsuLevel()

        assertNull(result)
    }

    @Test
    fun getAsuLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockSignal.asuLevel).thenReturn(28)
        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = mockSignal)

        val result = data.getAsuLevel()

        assertEquals(28, result)
    }

    // ==============================================
    // ASU DBM Tests
    // ==============================================

    @Test
    fun getAsuDbm_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = null)

        val result = data.getAsuDbm()

        assertNull(result)
    }

    @Test
    fun getAsuDbm_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockSignal.dbm).thenReturn(-88)
        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = mockSignal)

        val result = data.getAsuDbm()

        assertEquals(-88, result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameSignalProducesSameData() {
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        val data1 = CellSignalStrengthDataTdscdma(cellSignalStrength = mockSignal)
        val data2 = CellSignalStrengthDataTdscdma(cellSignalStrength = mockSignal)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = mockSignal)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun integration_allGetters_withFullyMockedSignal_returnCorrectValues() {
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockSignal.rscp).thenReturn(-95)
        `when`(mockSignal.level).thenReturn(2)
        `when`(mockSignal.asuLevel).thenReturn(28)
        `when`(mockSignal.dbm).thenReturn(-88)

        val data = CellSignalStrengthDataTdscdma(cellSignalStrength = mockSignal)

        assertEquals(-95, data.getRscp())
        assertEquals(2, data.getLevel())
        assertEquals(28, data.getAsuLevel())
        assertEquals(-88, data.getAsuDbm())
    }
}
