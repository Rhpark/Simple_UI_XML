package kr.open.library.simple_ui.robolectric.system_manager.info.network.telephony.data.cell.nr

import android.os.Build
import android.telephony.CellIdentityNr
import kr.open.library.simple_ui.system_manager.info.network.telephony.data.cell.nr.CellIdentityNrData
import org.junit.Assert.assertArrayEquals
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
 * Robolectric test for CellIdentityNrData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellIdentityNrDataRobolectricTest {

    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellIdentity_createsInstance() {
        val data = CellIdentityNrData(cellIdentity = null)

        assertNotNull(data)
        assertNull(data.cellIdentity)
    }

    @Test
    fun constructor_withValidCellIdentity_createsInstance() {
        val mockCell = mock(CellIdentityNr::class.java)
        val data = CellIdentityNrData(cellIdentity = mockCell)

        assertNotNull(data)
        assertNotNull(data.cellIdentity)
        assertEquals(mockCell, data.cellIdentity)
    }

    // ==============================================
    // Band List Tests (API 30+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getBandList_withNullCellIdentity_returnsNull() {
        val data = CellIdentityNrData(cellIdentity = null)

        val result = data.getBandList()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getBandList_withValidValue_returnsCorrectArray() {
        val mockCell = mock(CellIdentityNr::class.java)
        val bands = intArrayOf(1, 3, 7, 20, 28, 78)
        `when`(mockCell.bands).thenReturn(bands)
        val data = CellIdentityNrData(cellIdentity = mockCell)

        val result = data.getBandList()

        assertArrayEquals(bands, result)
    }

    // ==============================================
    // PCI Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getPci_withNullCellIdentity_returnsNull() {
        val data = CellIdentityNrData(cellIdentity = null)

        val result = data.getPci()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getPci_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityNr::class.java)
        `when`(mockCell.pci).thenReturn(500)
        val data = CellIdentityNrData(cellIdentity = mockCell)

        val result = data.getPci()

        assertEquals(500, result)
    }

    // ==============================================
    // TAC Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getTac_withNullCellIdentity_returnsNull() {
        val data = CellIdentityNrData(cellIdentity = null)

        val result = data.getTac()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getTac_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityNr::class.java)
        `when`(mockCell.tac).thenReturn(12345)
        val data = CellIdentityNrData(cellIdentity = mockCell)

        val result = data.getTac()

        assertEquals(12345, result)
    }

    // ==============================================
    // NRARFCN Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getNrArfcn_withNullCellIdentity_returnsNull() {
        val data = CellIdentityNrData(cellIdentity = null)

        val result = data.getNrArfcn()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getNrArfcn_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityNr::class.java)
        `when`(mockCell.nrarfcn).thenReturn(632628)
        val data = CellIdentityNrData(cellIdentity = mockCell)

        val result = data.getNrArfcn()

        assertEquals(632628, result)
    }

    // ==============================================
    // NCI (Band NCI) Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getBandNci_withNullCellIdentity_returnsNull() {
        val data = CellIdentityNrData(cellIdentity = null)

        val result = data.getBandNci()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getBandNci_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityNr::class.java)
        `when`(mockCell.nci).thenReturn(123456789012345L)
        val data = CellIdentityNrData(cellIdentity = mockCell)

        val result = data.getBandNci()

        assertEquals(123456789012345L, result)
    }

    // ==============================================
    // MCC Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getMcc_withNullCellIdentity_returnsNull() {
        val data = CellIdentityNrData(cellIdentity = null)

        val result = data.getMcc()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getMcc_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityNr::class.java)
        `when`(mockCell.mccString).thenReturn("450")
        val data = CellIdentityNrData(cellIdentity = mockCell)

        val result = data.getMcc()

        assertEquals("450", result)
    }

    // ==============================================
    // MNC Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getMnc_withNullCellIdentity_returnsNull() {
        val data = CellIdentityNrData(cellIdentity = null)

        val result = data.getMnc()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getMnc_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityNr::class.java)
        `when`(mockCell.mncString).thenReturn("05")
        val data = CellIdentityNrData(cellIdentity = mockCell)

        val result = data.getMnc()

        assertEquals("05", result)
    }

    // ==============================================
    // Operator Alpha Long Tests
    // ==============================================

    @Test
    fun getOperatorAlphaLong_withNullCellIdentity_returnsNull() {
        val data = CellIdentityNrData(cellIdentity = null)

        val result = data.getOperatorAlphaLong()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaLong_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityNr::class.java)
        `when`(mockCell.operatorAlphaLong).thenReturn("Test 5G Operator Long")
        val data = CellIdentityNrData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaLong()

        assertEquals("Test 5G Operator Long", result)
    }

    // ==============================================
    // Operator Alpha Short Tests
    // ==============================================

    @Test
    fun getOperatorAlphaShort_withNullCellIdentity_returnsNull() {
        val data = CellIdentityNrData(cellIdentity = null)

        val result = data.getOperatorAlphaShort()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaShort_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityNr::class.java)
        `when`(mockCell.operatorAlphaShort).thenReturn("5G Op")
        val data = CellIdentityNrData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaShort()

        assertEquals("5G Op", result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameIdentityProducesSameData() {
        val mockCell = mock(CellIdentityNr::class.java)
        val data1 = CellIdentityNrData(cellIdentity = mockCell)
        val data2 = CellIdentityNrData(cellIdentity = mockCell)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_copy_createsNewInstance() {
        val mockCell1 = mock(CellIdentityNr::class.java)
        val mockCell2 = mock(CellIdentityNr::class.java)
        val data = CellIdentityNrData(cellIdentity = mockCell1)

        val copied = data.copy(cellIdentity = mockCell2)

        assertEquals(mockCell1, data.cellIdentity)
        assertEquals(mockCell2, copied.cellIdentity)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockCell = mock(CellIdentityNr::class.java)
        val data = CellIdentityNrData(cellIdentity = mockCell)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedCell_returnCorrectValues() {
        val mockCell = mock(CellIdentityNr::class.java)
        val bands = intArrayOf(1, 3, 7, 28, 78)
        `when`(mockCell.bands).thenReturn(bands)
        `when`(mockCell.pci).thenReturn(500)
        `when`(mockCell.tac).thenReturn(12345)
        `when`(mockCell.nrarfcn).thenReturn(632628)
        `when`(mockCell.nci).thenReturn(123456789012345L)
        `when`(mockCell.mccString).thenReturn("450")
        `when`(mockCell.mncString).thenReturn("05")
        `when`(mockCell.operatorAlphaLong).thenReturn("5G Operator Long")
        `when`(mockCell.operatorAlphaShort).thenReturn("5G Op")

        val data = CellIdentityNrData(cellIdentity = mockCell)

        assertArrayEquals(bands, data.getBandList())
        assertEquals(500, data.getPci())
        assertEquals(12345, data.getTac())
        assertEquals(632628, data.getNrArfcn())
        assertEquals(123456789012345L, data.getBandNci())
        assertEquals("450", data.getMcc())
        assertEquals("05", data.getMnc())
        assertEquals("5G Operator Long", data.getOperatorAlphaLong())
        assertEquals("5G Op", data.getOperatorAlphaShort())
    }
}
