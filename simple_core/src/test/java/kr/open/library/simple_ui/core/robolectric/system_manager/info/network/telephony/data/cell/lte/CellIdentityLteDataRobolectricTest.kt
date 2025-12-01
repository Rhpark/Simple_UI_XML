package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.lte

import android.os.Build
import android.telephony.CellIdentityLte
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.lte.CellIdentityLteData
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
 * Robolectric test for CellIdentityLteData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellIdentityLteDataRobolectricTest {
    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellIdentity_createsInstance() {
        val data = CellIdentityLteData(cellIdentity = null)

        assertNotNull(data)
        assertNull(data.cellIdentity)
    }

    @Test
    fun constructor_withValidCellIdentity_createsInstance() {
        val mockCell = mock(CellIdentityLte::class.java)
        val data = CellIdentityLteData(cellIdentity = mockCell)

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
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getBandList()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getBandList_withValidValue_returnsCorrectArray() {
        val mockCell = mock(CellIdentityLte::class.java)
        val bands = intArrayOf(1, 3, 7, 20)
        `when`(mockCell.bands).thenReturn(bands)
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getBandList()

        assertArrayEquals(bands, result)
    }

    // ==============================================
    // PCI Tests
    // ==============================================

    @Test
    fun getPci_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getPci()

        assertNull(result)
    }

    @Test
    fun getPci_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityLte::class.java)
        `when`(mockCell.pci).thenReturn(123)
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getPci()

        assertEquals(123, result)
    }

    // ==============================================
    // TAC Tests
    // ==============================================

    @Test
    fun getTac_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getTac()

        assertNull(result)
    }

    @Test
    fun getTac_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityLte::class.java)
        `when`(mockCell.tac).thenReturn(456)
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getTac()

        assertEquals(456, result)
    }

    // ==============================================
    // EARFCN Tests
    // ==============================================

    @Test
    fun getEarfcn_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getEarfcn()

        assertNull(result)
    }

    @Test
    fun getEarfcn_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityLte::class.java)
        `when`(mockCell.earfcn).thenReturn(6200)
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getEarfcn()

        assertEquals(6200, result)
    }

    // ==============================================
    // Bandwidth Tests
    // ==============================================

    @Test
    fun getBandWidth_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getBandWidth()

        assertNull(result)
    }

    @Test
    fun getBandWidth_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityLte::class.java)
        `when`(mockCell.bandwidth).thenReturn(20000)
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getBandWidth()

        assertEquals(20000, result)
    }

    // ==============================================
    // ECI Tests
    // ==============================================

    @Test
    fun getEci_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getEci()

        assertNull(result)
    }

    @Test
    fun getEci_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityLte::class.java)
        `when`(mockCell.ci).thenReturn(12345678)
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getEci()

        assertEquals(12345678, result)
    }

    // ==============================================
    // MCC Tests
    // ==============================================

    @Test
    fun getMcc_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getMcc()

        assertNull(result)
    }

    @Test
    fun getMcc_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityLte::class.java)
        `when`(mockCell.mccString).thenReturn("450")
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getMcc()

        assertEquals("450", result)
    }

    // ==============================================
    // MNC Tests
    // ==============================================

    @Test
    fun getMnc_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getMnc()

        assertNull(result)
    }

    @Test
    fun getMnc_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityLte::class.java)
        `when`(mockCell.mncString).thenReturn("05")
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getMnc()

        assertEquals("05", result)
    }

    // ==============================================
    // ENB Tests (Calculated from CI)
    // ==============================================

    @Test
    fun getEnb_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getEnb()

        assertNull(result)
    }

    @Test
    fun getEnb_withValidValue_returnsCorrectCalculation() {
        val mockCell = mock(CellIdentityLte::class.java)
        // CI = 0x12345 (74565) -> ENB should be (74565 >> 8) & 0xFFFFF = 291
        `when`(mockCell.ci).thenReturn(74565)
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getEnb()

        assertEquals(291, result)
    }

    // ==============================================
    // LCID Tests (Calculated from CI)
    // ==============================================

    @Test
    fun getLcid_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getLcid()

        assertNull(result)
    }

    @Test
    fun getLcid_withValidValue_returnsCorrectCalculation() {
        val mockCell = mock(CellIdentityLte::class.java)
        // CI = 0x12345 (74565) -> LCID should be 74565 & 0xFF = 69
        `when`(mockCell.ci).thenReturn(74565)
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getLcid()

        assertEquals(69, result)
    }

    // ==============================================
    // Mobile Network Operator Tests
    // ==============================================

    @Test
    fun getMobileNetworkOperator_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getMobileNetworkOperator()

        assertNull(result)
    }

    @Test
    fun getMobileNetworkOperator_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityLte::class.java)
        `when`(mockCell.mobileNetworkOperator).thenReturn("45005")
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getMobileNetworkOperator()

        assertEquals("45005", result)
    }

    // ==============================================
    // Operator Alpha Long Tests
    // ==============================================

    @Test
    fun getOperatorAlphaLong_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getOperatorAlphaLong()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaLong_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityLte::class.java)
        `when`(mockCell.operatorAlphaLong).thenReturn("Test Operator Long")
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaLong()

        assertEquals("Test Operator Long", result)
    }

    // ==============================================
    // Operator Alpha Short Tests
    // ==============================================

    @Test
    fun getOperatorAlphaShort_withNullCellIdentity_returnsNull() {
        val data = CellIdentityLteData(cellIdentity = null)

        val result = data.getOperatorAlphaShort()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaShort_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityLte::class.java)
        `when`(mockCell.operatorAlphaShort).thenReturn("Test Op")
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaShort()

        assertEquals("Test Op", result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameIdentityProducesSameData() {
        val mockCell = mock(CellIdentityLte::class.java)
        val data1 = CellIdentityLteData(cellIdentity = mockCell)
        val data2 = CellIdentityLteData(cellIdentity = mockCell)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_copy_createsNewInstance() {
        val mockCell1 = mock(CellIdentityLte::class.java)
        val mockCell2 = mock(CellIdentityLte::class.java)
        val data = CellIdentityLteData(cellIdentity = mockCell1)

        val copied = data.copy(cellIdentity = mockCell2)

        assertEquals(mockCell1, data.cellIdentity)
        assertEquals(mockCell2, copied.cellIdentity)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockCell = mock(CellIdentityLte::class.java)
        val data = CellIdentityLteData(cellIdentity = mockCell)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedCell_returnCorrectValues() {
        val mockCell = mock(CellIdentityLte::class.java)
        val bands = intArrayOf(1, 3, 7)
        `when`(mockCell.bands).thenReturn(bands)
        `when`(mockCell.pci).thenReturn(100)
        `when`(mockCell.tac).thenReturn(200)
        `when`(mockCell.earfcn).thenReturn(6200)
        `when`(mockCell.bandwidth).thenReturn(20000)
        `when`(mockCell.ci).thenReturn(12345678)
        `when`(mockCell.mccString).thenReturn("450")
        `when`(mockCell.mncString).thenReturn("05")
        `when`(mockCell.mobileNetworkOperator).thenReturn("45005")
        `when`(mockCell.operatorAlphaLong).thenReturn("Long Operator")
        `when`(mockCell.operatorAlphaShort).thenReturn("Short")

        val data = CellIdentityLteData(cellIdentity = mockCell)

        assertArrayEquals(bands, data.getBandList())
        assertEquals(100, data.getPci())
        assertEquals(200, data.getTac())
        assertEquals(6200, data.getEarfcn())
        assertEquals(20000, data.getBandWidth())
        assertEquals(12345678, data.getEci())
        assertEquals("450", data.getMcc())
        assertEquals("05", data.getMnc())
        assertEquals(48225, data.getEnb()) // (12345678 >> 8) & 0xFFFFF
        assertEquals(78, data.getLcid()) // 12345678 & 0xFF
        assertEquals("45005", data.getMobileNetworkOperator())
        assertEquals("Long Operator", data.getOperatorAlphaLong())
        assertEquals("Short", data.getOperatorAlphaShort())
    }
}
