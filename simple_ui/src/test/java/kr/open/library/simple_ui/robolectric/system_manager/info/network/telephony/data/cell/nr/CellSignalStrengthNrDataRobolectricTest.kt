package kr.open.library.simple_ui.robolectric.system_manager.info.network.telephony.data.cell.nr

import android.os.Build
import android.telephony.CellSignalStrengthNr
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.nr.CellSignalStrengthNrData
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
 * Robolectric test for CellSignalStrengthNrData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellSignalStrengthNrDataRobolectricTest {

    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellSignalStrength_createsInstance() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        assertNotNull(data)
    }

    @Test
    fun constructor_withValidCellSignalStrength_createsInstance() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        assertNotNull(data)
    }

    // ==============================================
    // CSI CQI Report Tests (API 31+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getCsiCqiReport_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        val result = data.getCsiCqiReport()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getCsiCqiReport_withValidValue_returnsCorrectList() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        val cqiReport = listOf(10, 11, 12, 13)
        `when`(mockSignal.csiCqiReport).thenReturn(cqiReport)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.getCsiCqiReport()

        assertEquals(cqiReport, result)
    }

    // ==============================================
    // CSI CQI Table Index Tests (API 31+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getCsiCqiTableIndex_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        val result = data.getCsiCqiTableIndex()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun getCsiCqiTableIndex_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockSignal.csiCqiTableIndex).thenReturn(1)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.getCsiCqiTableIndex()

        assertEquals(1, result)
    }

    // ==============================================
    // CSI RSRP Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getCsiRsrp_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        val result = data.getCsiRsrp()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getCsiRsrp_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockSignal.csiRsrp).thenReturn(-100)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.getCsiRsrp()

        assertEquals(-100, result)
    }

    // ==============================================
    // CSI RSRQ Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getCsiRsrq_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        val result = data.getCsiRsrq()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getCsiRsrq_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockSignal.csiRsrq).thenReturn(-12)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.getCsiRsrq()

        assertEquals(-12, result)
    }

    // ==============================================
    // CSI SINR Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getCsiSinr_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        val result = data.getCsiSinr()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getCsiSinr_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockSignal.csiSinr).thenReturn(15)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.getCsiSinr()

        assertEquals(15, result)
    }

    // ==============================================
    // SS RSRP Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getSsRsrp_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        val result = data.getSsRsrp()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getSsRsrp_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockSignal.ssRsrp).thenReturn(-95)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.getSsRsrp()

        assertEquals(-95, result)
    }

    // ==============================================
    // SS RSRQ Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getSsRsrq_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        val result = data.getSsRsrq()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getSsRsrq_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockSignal.ssRsrq).thenReturn(-8)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.getSsRsrq()

        assertEquals(-8, result)
    }

    // ==============================================
    // SS SINR Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getSsSinr_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        val result = data.getSsSinr()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getSsSinr_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockSignal.ssSinr).thenReturn(20)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.getSsSinr()

        assertEquals(20, result)
    }

    // ==============================================
    // DBM Tests
    // ==============================================

    @Test
    fun getDbm_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        val result = data.getDbm()

        assertNull(result)
    }

    @Test
    fun getDbm_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockSignal.dbm).thenReturn(-98)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.getDbm()

        assertEquals(-98, result)
    }

    // ==============================================
    // Level Tests
    // ==============================================

    @Test
    fun getLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        val result = data.getLevel()

        assertNull(result)
    }

    @Test
    fun getLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockSignal.level).thenReturn(4)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.getLevel()

        assertEquals(4, result)
    }

    // ==============================================
    // ASU Level Tests
    // ==============================================

    @Test
    fun getAsuLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthNrData(cellSignalStrength = null)

        val result = data.getAsuLevel()

        assertNull(result)
    }

    @Test
    fun getAsuLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        `when`(mockSignal.asuLevel).thenReturn(50)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.getAsuLevel()

        assertEquals(50, result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameSignalProducesSameData() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        val data1 = CellSignalStrengthNrData(cellSignalStrength = mockSignal)
        val data2 = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun integration_allGetters_withFullyMockedSignal_returnCorrectValues() {
        val mockSignal = mock(CellSignalStrengthNr::class.java)
        val cqiReport = listOf(10, 11, 12)
        `when`(mockSignal.csiCqiReport).thenReturn(cqiReport)
        `when`(mockSignal.csiCqiTableIndex).thenReturn(1)
        `when`(mockSignal.csiRsrp).thenReturn(-100)
        `when`(mockSignal.csiRsrq).thenReturn(-12)
        `when`(mockSignal.csiSinr).thenReturn(15)
        `when`(mockSignal.ssRsrp).thenReturn(-95)
        `when`(mockSignal.ssRsrq).thenReturn(-8)
        `when`(mockSignal.ssSinr).thenReturn(20)
        `when`(mockSignal.dbm).thenReturn(-98)
        `when`(mockSignal.level).thenReturn(4)
        `when`(mockSignal.asuLevel).thenReturn(50)

        val data = CellSignalStrengthNrData(cellSignalStrength = mockSignal)

        assertEquals(cqiReport, data.getCsiCqiReport())
        assertEquals(1, data.getCsiCqiTableIndex())
        assertEquals(-100, data.getCsiRsrp())
        assertEquals(-12, data.getCsiRsrq())
        assertEquals(15, data.getCsiSinr())
        assertEquals(-95, data.getSsRsrp())
        assertEquals(-8, data.getSsRsrq())
        assertEquals(20, data.getSsSinr())
        assertEquals(-98, data.getDbm())
        assertEquals(4, data.getLevel())
        assertEquals(50, data.getAsuLevel())
    }
}
