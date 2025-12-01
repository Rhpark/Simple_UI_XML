package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.cdma

import android.telephony.CellIdentityCdma
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.cdma.CellIdentityCdmaData
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
 * Robolectric test for CellIdentityCdmaData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellIdentityCdmaDataRobolectricTest {
    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellIdentity_createsInstance() {
        val data = CellIdentityCdmaData(cellIdentity = null)

        assertNotNull(data)
        assertNull(data.cellIdentity)
    }

    @Test
    fun constructor_withValidCellIdentity_createsInstance() {
        val mockCell = mock(CellIdentityCdma::class.java)
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        assertNotNull(data)
        assertNotNull(data.cellIdentity)
        assertEquals(mockCell, data.cellIdentity)
    }

    // ==============================================
    // Operator Alpha Long Tests
    // ==============================================

    @Test
    fun getOperatorAlphaLong_withNullCellIdentity_returnsNull() {
        val data = CellIdentityCdmaData(cellIdentity = null)

        val result = data.getOperatorAlphaLong()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaLong_withMockedValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.operatorAlphaLong).thenReturn("Test Operator Long")
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaLong()

        assertEquals("Test Operator Long", result)
    }

    @Test
    fun getOperatorAlphaLong_withNullValue_returnsNull() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.operatorAlphaLong).thenReturn(null)
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaLong()

        assertNull(result)
    }

    // ==============================================
    // Operator Alpha Short Tests
    // ==============================================

    @Test
    fun getOperatorAlphaShort_withNullCellIdentity_returnsNull() {
        val data = CellIdentityCdmaData(cellIdentity = null)

        val result = data.getOperatorAlphaShort()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaShort_withMockedValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.operatorAlphaShort).thenReturn("Test Op")
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaShort()

        assertEquals("Test Op", result)
    }

    // ==============================================
    // Basestation ID Tests
    // ==============================================

    @Test
    fun getBasestationId_withNullCellIdentity_returnsNull() {
        val data = CellIdentityCdmaData(cellIdentity = null)

        val result = data.getBasestationId()

        assertNull(result)
    }

    @Test
    fun getBasestationId_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.basestationId).thenReturn(12345)
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.getBasestationId()

        assertEquals(12345, result)
    }

    @Test
    fun getBasestationId_withZeroValue_returnsZero() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.basestationId).thenReturn(0)
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.getBasestationId()

        assertEquals(0, result)
    }

    // ==============================================
    // Network ID Tests
    // ==============================================

    @Test
    fun getNetworkId_withNullCellIdentity_returnsNull() {
        val data = CellIdentityCdmaData(cellIdentity = null)

        val result = data.getNetworkId()

        assertNull(result)
    }

    @Test
    fun getNetworkId_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.networkId).thenReturn(999)
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.getNetworkId()

        assertEquals(999, result)
    }

    // ==============================================
    // System ID Tests
    // ==============================================

    @Test
    fun getSystemId_withNullCellIdentity_returnsNull() {
        val data = CellIdentityCdmaData(cellIdentity = null)

        val result = data.getSystemId()

        assertNull(result)
    }

    @Test
    fun getSystemId_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.systemId).thenReturn(8888)
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.getSystemId()

        assertEquals(8888, result)
    }

    // ==============================================
    // Latitude Tests
    // ==============================================

    @Test
    fun getLatitude_withNullCellIdentity_returnsNull() {
        val data = CellIdentityCdmaData(cellIdentity = null)

        val result = data.getLatitude()

        assertNull(result)
    }

    @Test
    fun getLatitude_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.latitude).thenReturn(370000) // Latitude in quarter-seconds
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.getLatitude()

        assertEquals(370000, result)
    }

    @Test
    fun getLatitude_withNegativeValue_returnsNegativeValue() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.latitude).thenReturn(-370000)
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.getLatitude()

        assertEquals(-370000, result)
    }

    // ==============================================
    // Longitude Tests
    // ==============================================

    @Test
    fun getLongitude_withNullCellIdentity_returnsNull() {
        val data = CellIdentityCdmaData(cellIdentity = null)

        val result = data.getLongitude()

        assertNull(result)
    }

    @Test
    fun getLongitude_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.longitude).thenReturn(1270000) // Longitude in quarter-seconds
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.getLongitude()

        assertEquals(1270000, result)
    }

    @Test
    fun getLongitude_withNegativeValue_returnsNegativeValue() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.longitude).thenReturn(-1270000)
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.getLongitude()

        assertEquals(-1270000, result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameIdentityProducesSameData() {
        val mockCell = mock(CellIdentityCdma::class.java)
        val data1 = CellIdentityCdmaData(cellIdentity = mockCell)
        val data2 = CellIdentityCdmaData(cellIdentity = mockCell)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_copy_createsNewInstance() {
        val mockCell1 = mock(CellIdentityCdma::class.java)
        val mockCell2 = mock(CellIdentityCdma::class.java)
        val data = CellIdentityCdmaData(cellIdentity = mockCell1)

        val copied = data.copy(cellIdentity = mockCell2)

        assertEquals(mockCell1, data.cellIdentity)
        assertEquals(mockCell2, copied.cellIdentity)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockCell = mock(CellIdentityCdma::class.java)
        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    fun integration_allGetters_withFullyMockedCell_returnCorrectValues() {
        val mockCell = mock(CellIdentityCdma::class.java)
        `when`(mockCell.operatorAlphaLong).thenReturn("Long Operator Name")
        `when`(mockCell.operatorAlphaShort).thenReturn("Short")
        `when`(mockCell.basestationId).thenReturn(100)
        `when`(mockCell.networkId).thenReturn(200)
        `when`(mockCell.systemId).thenReturn(300)
        `when`(mockCell.latitude).thenReturn(400)
        `when`(mockCell.longitude).thenReturn(500)

        val data = CellIdentityCdmaData(cellIdentity = mockCell)

        assertEquals("Long Operator Name", data.getOperatorAlphaLong())
        assertEquals("Short", data.getOperatorAlphaShort())
        assertEquals(100, data.getBasestationId())
        assertEquals(200, data.getNetworkId())
        assertEquals(300, data.getSystemId())
        assertEquals(400, data.getLatitude())
        assertEquals(500, data.getLongitude())
    }
}
