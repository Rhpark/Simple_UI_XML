package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.lte

import android.os.Build
import android.telephony.CellSignalStrengthLte
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.lte.CellSignalStrengthLteData
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
 * Robolectric test for CellSignalStrengthLteData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellSignalStrengthLteDataRobolectricTest {

    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellSignalStrength_createsInstance() {
        val data = CellSignalStrengthLteData(cellSignalStrength = null)

        assertNotNull(data)
    }

    @Test
    fun constructor_withValidCellSignalStrength_createsInstance() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        assertNotNull(data)
        assertNotNull(data.cellSignalStrength)
    }

    // ==============================================
    // CQI Table Index Tests (API 31+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getCqiTableIndex_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthLteData(cellSignalStrength = null)

        val result = data.getCqiTableIndex()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getCqiTableIndex_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockSignal.cqiTableIndex).thenReturn(2)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        val result = data.getCqiTableIndex()

        assertEquals(2, result)
    }

    // ==============================================
    // RSRP Tests
    // ==============================================

    @Test
    fun getRsrp_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthLteData(cellSignalStrength = null)

        val result = data.getRsrp()

        assertNull(result)
    }

    @Test
    fun getRsrp_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockSignal.rsrp).thenReturn(-95)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        val result = data.getRsrp()

        assertEquals(-95, result)
    }

    // ==============================================
    // RSRQ Tests
    // ==============================================

    @Test
    fun getRsrq_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthLteData(cellSignalStrength = null)

        val result = data.getRsrq()

        assertNull(result)
    }

    @Test
    fun getRsrq_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockSignal.rsrq).thenReturn(-10)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        val result = data.getRsrq()

        assertEquals(-10, result)
    }

    // ==============================================
    // RSSI Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getRssi_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthLteData(cellSignalStrength = null)

        val result = data.getRssi()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getRssi_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockSignal.rssi).thenReturn(-85)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        val result = data.getRssi()

        assertEquals(-85, result)
    }

    // ==============================================
    // RSSNR Tests
    // ==============================================

    @Test
    fun getRssnr_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthLteData(cellSignalStrength = null)

        val result = data.getRssnr()

        assertNull(result)
    }

    @Test
    fun getRssnr_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockSignal.rssnr).thenReturn(130)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        val result = data.getRssnr()

        assertEquals(130, result)
    }

    // ==============================================
    // CQI Tests
    // ==============================================

    @Test
    fun getCqi_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthLteData(cellSignalStrength = null)

        val result = data.getCqi()

        assertNull(result)
    }

    @Test
    fun getCqi_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockSignal.cqi).thenReturn(12)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        val result = data.getCqi()

        assertEquals(12, result)
    }

    // ==============================================
    // Timing Advance Tests
    // ==============================================

    @Test
    fun getTimingAdvance_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthLteData(cellSignalStrength = null)

        val result = data.getTimingAdvance()

        assertNull(result)
    }

    @Test
    fun getTimingAdvance_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockSignal.timingAdvance).thenReturn(8)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        val result = data.getTimingAdvance()

        assertEquals(8, result)
    }

    // ==============================================
    // DBM Tests
    // ==============================================

    @Test
    fun getDbm_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthLteData(cellSignalStrength = null)

        val result = data.getDbm()

        assertNull(result)
    }

    @Test
    fun getDbm_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockSignal.dbm).thenReturn(-92)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        val result = data.getDbm()

        assertEquals(-92, result)
    }

    // ==============================================
    // Level Tests
    // ==============================================

    @Test
    fun getLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthLteData(cellSignalStrength = null)

        val result = data.getLevel()

        assertNull(result)
    }

    @Test
    fun getLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockSignal.level).thenReturn(3)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        val result = data.getLevel()

        assertEquals(3, result)
    }

    // ==============================================
    // ASU Level Tests
    // ==============================================

    @Test
    fun getAsuLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthLteData(cellSignalStrength = null)

        val result = data.getAsuLevel()

        assertNull(result)
    }

    @Test
    fun getAsuLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockSignal.asuLevel).thenReturn(45)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        val result = data.getAsuLevel()

        assertEquals(45, result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameSignalProducesSameData() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        val data1 = CellSignalStrengthLteData(cellSignalStrength = mockSignal)
        val data2 = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_copy_createsNewInstance() {
        val mockSignal1 = mock(CellSignalStrengthLte::class.java)
        val mockSignal2 = mock(CellSignalStrengthLte::class.java)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal1)

        val copied = data.copy(cellSignalStrength = mockSignal2)

        assertEquals(mockSignal1, data.cellSignalStrength)
        assertEquals(mockSignal2, copied.cellSignalStrength)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun integration_allGetters_withFullyMockedSignal_returnCorrectValues() {
        val mockSignal = mock(CellSignalStrengthLte::class.java)
        `when`(mockSignal.cqiTableIndex).thenReturn(2)
        `when`(mockSignal.rsrp).thenReturn(-95)
        `when`(mockSignal.rsrq).thenReturn(-10)
        `when`(mockSignal.rssi).thenReturn(-85)
        `when`(mockSignal.rssnr).thenReturn(130)
        `when`(mockSignal.cqi).thenReturn(12)
        `when`(mockSignal.timingAdvance).thenReturn(8)
        `when`(mockSignal.dbm).thenReturn(-92)
        `when`(mockSignal.level).thenReturn(3)
        `when`(mockSignal.asuLevel).thenReturn(45)

        val data = CellSignalStrengthLteData(cellSignalStrength = mockSignal)

        assertEquals(2, data.getCqiTableIndex())
        assertEquals(-95, data.getRsrp())
        assertEquals(-10, data.getRsrq())
        assertEquals(-85, data.getRssi())
        assertEquals(130, data.getRssnr())
        assertEquals(12, data.getCqi())
        assertEquals(8, data.getTimingAdvance())
        assertEquals(-92, data.getDbm())
        assertEquals(3, data.getLevel())
        assertEquals(45, data.getAsuLevel())
    }
}
