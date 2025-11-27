package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.cdma

import android.telephony.CellSignalStrengthCdma
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.cdma.CellSignalStrengthCdmaData
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
 * Robolectric test for CellSignalStrengthCdmaData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellSignalStrengthCdmaDataRobolectricTest {

    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellSignalStrength_createsInstance() {
        val data = CellSignalStrengthCdmaData(cellSignalStrength = null)

        assertNotNull(data)
    }

    @Test
    fun constructor_withValidCellSignalStrength_createsInstance() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        assertNotNull(data)
    }

    // ==============================================
    // DBM Tests
    // ==============================================

    @Test
    fun getDbm_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthCdmaData(cellSignalStrength = null)

        val result = data.getDbm()

        assertNull(result)
    }

    @Test
    fun getDbm_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockSignal.dbm).thenReturn(-85)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        val result = data.getDbm()

        assertEquals(-85, result)
    }

    // ==============================================
    // Level Tests
    // ==============================================

    @Test
    fun getLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthCdmaData(cellSignalStrength = null)

        val result = data.getLevel()

        assertNull(result)
    }

    @Test
    fun getLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockSignal.level).thenReturn(3)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        val result = data.getLevel()

        assertEquals(3, result)
    }

    // ==============================================
    // ASU Level Tests
    // ==============================================

    @Test
    fun getAsuLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthCdmaData(cellSignalStrength = null)

        val result = data.getAsuLevel()

        assertNull(result)
    }

    @Test
    fun getAsuLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockSignal.asuLevel).thenReturn(25)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        val result = data.getAsuLevel()

        assertEquals(25, result)
    }

    // ==============================================
    // EVDO Level Tests
    // ==============================================

    @Test
    fun getEvdoLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthCdmaData(cellSignalStrength = null)

        val result = data.getEvdoLevel()

        assertNull(result)
    }

    @Test
    fun getEvdoLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockSignal.evdoLevel).thenReturn(2)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        val result = data.getEvdoLevel()

        assertEquals(2, result)
    }

    // ==============================================
    // EVDO DBM Tests
    // ==============================================

    @Test
    fun getEvdoDbm_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthCdmaData(cellSignalStrength = null)

        val result = data.getEvdoDbm()

        assertNull(result)
    }

    @Test
    fun getEvdoDbm_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockSignal.evdoDbm).thenReturn(-90)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        val result = data.getEvdoDbm()

        assertEquals(-90, result)
    }

    // ==============================================
    // EVDO ECIO Tests
    // ==============================================

    @Test
    fun getEvdoEcio_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthCdmaData(cellSignalStrength = null)

        val result = data.getEvdoEcio()

        assertNull(result)
    }

    @Test
    fun getEvdoEcio_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockSignal.evdoEcio).thenReturn(-100)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        val result = data.getEvdoEcio()

        assertEquals(-100, result)
    }

    // ==============================================
    // EVDO SNR Tests
    // ==============================================

    @Test
    fun getEvdoSnr_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthCdmaData(cellSignalStrength = null)

        val result = data.getEvdoSnr()

        assertNull(result)
    }

    @Test
    fun getEvdoSnr_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockSignal.evdoSnr).thenReturn(7)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        val result = data.getEvdoSnr()

        assertEquals(7, result)
    }

    // ==============================================
    // CDMA ECIO Tests
    // ==============================================

    @Test
    fun getCdmaEcio_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthCdmaData(cellSignalStrength = null)

        val result = data.getCdmaEcio()

        assertNull(result)
    }

    @Test
    fun getCdmaEcio_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockSignal.cdmaEcio).thenReturn(-110)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        val result = data.getCdmaEcio()

        assertEquals(-110, result)
    }

    // ==============================================
    // CDMA Level Tests
    // ==============================================

    @Test
    fun getCdmaLevel_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthCdmaData(cellSignalStrength = null)

        val result = data.getCdmaLevel()

        assertNull(result)
    }

    @Test
    fun getCdmaLevel_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockSignal.cdmaLevel).thenReturn(4)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        val result = data.getCdmaLevel()

        assertEquals(4, result)
    }

    // ==============================================
    // CDMA DBM Tests
    // ==============================================

    @Test
    fun getCdmaDbm_withNullCellSignalStrength_returnsNull() {
        val data = CellSignalStrengthCdmaData(cellSignalStrength = null)

        val result = data.getCdmaDbm()

        assertNull(result)
    }

    @Test
    fun getCdmaDbm_withValidValue_returnsCorrectValue() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockSignal.cdmaDbm).thenReturn(-88)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        val result = data.getCdmaDbm()

        assertEquals(-88, result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameSignalProducesSameData() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        val data1 = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)
        val data2 = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    fun integration_allGetters_withFullyMockedSignal_returnCorrectValues() {
        val mockSignal = mock(CellSignalStrengthCdma::class.java)
        `when`(mockSignal.dbm).thenReturn(-85)
        `when`(mockSignal.level).thenReturn(3)
        `when`(mockSignal.asuLevel).thenReturn(25)
        `when`(mockSignal.evdoLevel).thenReturn(2)
        `when`(mockSignal.evdoDbm).thenReturn(-90)
        `when`(mockSignal.evdoEcio).thenReturn(-100)
        `when`(mockSignal.evdoSnr).thenReturn(7)
        `when`(mockSignal.cdmaEcio).thenReturn(-110)
        `when`(mockSignal.cdmaLevel).thenReturn(4)
        `when`(mockSignal.cdmaDbm).thenReturn(-88)

        val data = CellSignalStrengthCdmaData(cellSignalStrength = mockSignal)

        assertEquals(-85, data.getDbm())
        assertEquals(3, data.getLevel())
        assertEquals(25, data.getAsuLevel())
        assertEquals(2, data.getEvdoLevel())
        assertEquals(-90, data.getEvdoDbm())
        assertEquals(-100, data.getEvdoEcio())
        assertEquals(7, data.getEvdoSnr())
        assertEquals(-110, data.getCdmaEcio())
        assertEquals(4, data.getCdmaLevel())
        assertEquals(-88, data.getCdmaDbm())
    }
}
