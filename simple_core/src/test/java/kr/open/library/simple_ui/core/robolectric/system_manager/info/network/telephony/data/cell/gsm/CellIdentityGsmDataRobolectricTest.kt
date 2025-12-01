package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.gsm

import android.telephony.CellIdentityGsm
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.gsm.CellIdentityGsmData
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
 * Robolectric test for CellIdentityGsmData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellIdentityGsmDataRobolectricTest {
    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellIdentity_createsInstance() {
        val data = CellIdentityGsmData(cellIdentity = null)

        assertNotNull(data)
        assertNull(data.cellIdentity)
    }

    @Test
    fun constructor_withValidCellIdentity_createsInstance() {
        val mockCell = mock(CellIdentityGsm::class.java)
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        assertNotNull(data)
        assertNotNull(data.cellIdentity)
        assertEquals(mockCell, data.cellIdentity)
    }

    // ==============================================
    // Operator Alpha Long Tests
    // ==============================================

    @Test
    fun getOperatorAlphaLong_withNullCellIdentity_returnsNull() {
        val data = CellIdentityGsmData(cellIdentity = null)

        val result = data.getOperatorAlphaLong()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaLong_withMockedValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityGsm::class.java)
        `when`(mockCell.operatorAlphaLong).thenReturn("Test Operator Long")
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaLong()

        assertEquals("Test Operator Long", result)
    }

    @Test
    fun getOperatorAlphaLong_withNullValue_returnsNull() {
        val mockCell = mock(CellIdentityGsm::class.java)
        `when`(mockCell.operatorAlphaLong).thenReturn(null)
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaLong()

        assertNull(result)
    }

    // ==============================================
    // Operator Alpha Short Tests
    // ==============================================

    @Test
    fun getOperatorAlphaShort_withNullCellIdentity_returnsNull() {
        val data = CellIdentityGsmData(cellIdentity = null)

        val result = data.getOperatorAlphaShort()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaShort_withMockedValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityGsm::class.java)
        `when`(mockCell.operatorAlphaShort).thenReturn("Test Op")
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaShort()

        assertEquals("Test Op", result)
    }

    // ==============================================
    // ARFCN Tests
    // ==============================================

    @Test
    fun getArfcn_withNullCellIdentity_returnsNull() {
        val data = CellIdentityGsmData(cellIdentity = null)

        val result = data.getArfcn()

        assertNull(result)
    }

    @Test
    fun getArfcn_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityGsm::class.java)
        `when`(mockCell.arfcn).thenReturn(1234)
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        val result = data.getArfcn()

        assertEquals(1234, result)
    }

    // ==============================================
    // CID Tests
    // ==============================================

    @Test
    fun getCid_withNullCellIdentity_returnsNull() {
        val data = CellIdentityGsmData(cellIdentity = null)

        val result = data.getCid()

        assertNull(result)
    }

    @Test
    fun getCid_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityGsm::class.java)
        `when`(mockCell.cid).thenReturn(5678)
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        val result = data.getCid()

        assertEquals(5678, result)
    }

    // ==============================================
    // LAC Tests
    // ==============================================

    @Test
    fun getLac_withNullCellIdentity_returnsNull() {
        val data = CellIdentityGsmData(cellIdentity = null)

        val result = data.getLac()

        assertNull(result)
    }

    @Test
    fun getLac_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityGsm::class.java)
        `when`(mockCell.lac).thenReturn(9999)
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        val result = data.getLac()

        assertEquals(9999, result)
    }

    // ==============================================
    // BSIC Tests
    // ==============================================

    @Test
    fun getBsic_withNullCellIdentity_returnsNull() {
        val data = CellIdentityGsmData(cellIdentity = null)

        val result = data.getBsic()

        assertNull(result)
    }

    @Test
    fun getBsic_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityGsm::class.java)
        `when`(mockCell.bsic).thenReturn(63)
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        val result = data.getBsic()

        assertEquals(63, result)
    }

    // ==============================================
    // MCC String Tests
    // ==============================================

    @Test
    fun getMccString_withNullCellIdentity_returnsNull() {
        val data = CellIdentityGsmData(cellIdentity = null)

        val result = data.getMccString()

        assertNull(result)
    }

    @Test
    fun getMccString_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityGsm::class.java)
        `when`(mockCell.mccString).thenReturn("450")
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        val result = data.getMccString()

        assertEquals("450", result)
    }

    // ==============================================
    // MNC String Tests
    // ==============================================

    @Test
    fun getMncString_withNullCellIdentity_returnsNull() {
        val data = CellIdentityGsmData(cellIdentity = null)

        val result = data.getMncString()

        assertNull(result)
    }

    @Test
    fun getMncString_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityGsm::class.java)
        `when`(mockCell.mncString).thenReturn("05")
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        val result = data.getMncString()

        assertEquals("05", result)
    }

    // ==============================================
    // Mobile Network Operator Tests
    // ==============================================

    @Test
    fun getMobileNetworkOperator_withNullCellIdentity_returnsNull() {
        val data = CellIdentityGsmData(cellIdentity = null)

        val result = data.getMobileNetworkOperator()

        assertNull(result)
    }

    @Test
    fun getMobileNetworkOperator_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityGsm::class.java)
        `when`(mockCell.mobileNetworkOperator).thenReturn("45005")
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        val result = data.getMobileNetworkOperator()

        assertEquals("45005", result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameIdentityProducesSameData() {
        val mockCell = mock(CellIdentityGsm::class.java)
        val data1 = CellIdentityGsmData(cellIdentity = mockCell)
        val data2 = CellIdentityGsmData(cellIdentity = mockCell)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_copy_createsNewInstance() {
        val mockCell1 = mock(CellIdentityGsm::class.java)
        val mockCell2 = mock(CellIdentityGsm::class.java)
        val data = CellIdentityGsmData(cellIdentity = mockCell1)

        val copied = data.copy(cellIdentity = mockCell2)

        assertEquals(mockCell1, data.cellIdentity)
        assertEquals(mockCell2, copied.cellIdentity)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockCell = mock(CellIdentityGsm::class.java)
        val data = CellIdentityGsmData(cellIdentity = mockCell)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    fun integration_allGetters_withFullyMockedCell_returnCorrectValues() {
        val mockCell = mock(CellIdentityGsm::class.java)
        `when`(mockCell.operatorAlphaLong).thenReturn("Long Operator Name")
        `when`(mockCell.operatorAlphaShort).thenReturn("Short")
        `when`(mockCell.arfcn).thenReturn(100)
        `when`(mockCell.cid).thenReturn(200)
        `when`(mockCell.lac).thenReturn(300)
        `when`(mockCell.bsic).thenReturn(40)
        `when`(mockCell.mccString).thenReturn("450")
        `when`(mockCell.mncString).thenReturn("05")
        `when`(mockCell.mobileNetworkOperator).thenReturn("45005")

        val data = CellIdentityGsmData(cellIdentity = mockCell)

        assertEquals("Long Operator Name", data.getOperatorAlphaLong())
        assertEquals("Short", data.getOperatorAlphaShort())
        assertEquals(100, data.getArfcn())
        assertEquals(200, data.getCid())
        assertEquals(300, data.getLac())
        assertEquals(40, data.getBsic())
        assertEquals("450", data.getMccString())
        assertEquals("05", data.getMncString())
        assertEquals("45005", data.getMobileNetworkOperator())
    }
}
