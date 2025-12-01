package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.tdscdma

import android.os.Build
import android.telephony.CellIdentityTdscdma
import android.telephony.ClosedSubscriberGroupInfo
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.tdscdma.CellIdentityTdscdmaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test for CellIdentityTdscdmaData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellIdentityTdscdmaDataRobolectricTest {
    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellIdentity_createsInstance() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        assertNotNull(data)
        assertNull(data.cellIdentity)
    }

    @Test
    fun constructor_withValidCellIdentity_createsInstance() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        assertNotNull(data)
        assertNotNull(data.cellIdentity)
        assertEquals(mockCell, data.cellIdentity)
    }

    // ==============================================
    // MCC Tests
    // ==============================================

    @Test
    fun getMcc_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getMcc()

        assertNull(result)
    }

    @Test
    fun getMcc_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.mccString).thenReturn("460")
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getMcc()

        assertEquals("460", result)
    }

    // ==============================================
    // MNC Tests
    // ==============================================

    @Test
    fun getMnc_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getMnc()

        assertNull(result)
    }

    @Test
    fun getMnc_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.mncString).thenReturn("00")
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getMnc()

        assertEquals("00", result)
    }

    // ==============================================
    // Operator Alpha Long Tests
    // ==============================================

    @Test
    fun getOperatorAlphaLong_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getOperatorAlphaLong()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaLong_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.operatorAlphaLong).thenReturn("TD-SCDMA Operator Long")
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaLong()

        assertEquals("TD-SCDMA Operator Long", result)
    }

    // ==============================================
    // Operator Alpha Short Tests
    // ==============================================

    @Test
    fun getOperatorAlphaShort_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getOperatorAlphaShort()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaShort_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.operatorAlphaShort).thenReturn("TD-S Op")
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaShort()

        assertEquals("TD-S Op", result)
    }

    // ==============================================
    // Mobile Network Operator Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getMobileNetworkOperator_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getMobileNetworkOperator()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getMobileNetworkOperator_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.mobileNetworkOperator).thenReturn("46000")
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getMobileNetworkOperator()

        assertEquals("46000", result)
    }

    // ==============================================
    // LAC Tests
    // ==============================================

    @Test
    fun getLac_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getLac()

        assertNull(result)
    }

    @Test
    fun getLac_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.lac).thenReturn(12345)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getLac()

        assertEquals(12345, result)
    }

    // ==============================================
    // CPID Tests
    // ==============================================

    @Test
    fun getCpid_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getCpid()

        assertNull(result)
    }

    @Test
    fun getCpid_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.cpid).thenReturn(127)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getCpid()

        assertEquals(127, result)
    }

    // ==============================================
    // UARFCN Tests (API 29+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getUarfcn_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getUarfcn()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getUarfcn_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.uarfcn).thenReturn(10054)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getUarfcn()

        assertEquals(10054, result)
    }

    // ==============================================
    // UCID Tests
    // ==============================================

    @Test
    fun getUCid_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getUCid()

        assertNull(result)
    }

    @Test
    fun getUCid_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.cid).thenReturn(268435455)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getUCid()

        assertEquals(268435455, result)
    }

    // ==============================================
    // CSG Identity Tests (API 30+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIdentity_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getCsgIdentity()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIdentity_withNullCsgInfo_returnsNull() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(null)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getCsgIdentity()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIdentity_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        val mockCsgInfo = mock(ClosedSubscriberGroupInfo::class.java)
        `when`(mockCsgInfo.csgIdentity).thenReturn(123456)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(mockCsgInfo)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getCsgIdentity()

        assertEquals(123456, result)
    }

    // ==============================================
    // CSG Indicator Tests (API 30+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIndicator_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getCsgIndicator()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIndicator_withTrueValue_returnsTrue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        val mockCsgInfo = mock(ClosedSubscriberGroupInfo::class.java)
        `when`(mockCsgInfo.csgIndicator).thenReturn(true)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(mockCsgInfo)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getCsgIndicator()

        assertTrue(result == true)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIndicator_withFalseValue_returnsFalse() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        val mockCsgInfo = mock(ClosedSubscriberGroupInfo::class.java)
        `when`(mockCsgInfo.csgIndicator).thenReturn(false)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(mockCsgInfo)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getCsgIndicator()

        assertFalse(result == true)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIndicator_withNullCsgInfo_returnsNull() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(null)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getCsgIndicator()

        assertNull(result)
    }

    // ==============================================
    // Home NodeB Name Tests (API 30+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getHomeNodebName_withNullCellIdentity_returnsNull() {
        val data = CellIdentityTdscdmaData(cellIdentity = null)

        val result = data.getHomeNodebName()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getHomeNodebName_withNullCsgInfo_returnsNull() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(null)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getHomeNodebName()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getHomeNodebName_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        val mockCsgInfo = mock(ClosedSubscriberGroupInfo::class.java)
        `when`(mockCsgInfo.homeNodebName).thenReturn("TestNodeB")
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(mockCsgInfo)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.getHomeNodebName()

        assertEquals("TestNodeB", result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameIdentityProducesSameData() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        val data1 = CellIdentityTdscdmaData(cellIdentity = mockCell)
        val data2 = CellIdentityTdscdmaData(cellIdentity = mockCell)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_copy_createsNewInstance() {
        val mockCell1 = mock(CellIdentityTdscdma::class.java)
        val mockCell2 = mock(CellIdentityTdscdma::class.java)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell1)

        val copied = data.copy(cellIdentity = mockCell2)

        assertEquals(mockCell1, data.cellIdentity)
        assertEquals(mockCell2, copied.cellIdentity)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedCell_returnCorrectValues() {
        val mockCell = mock(CellIdentityTdscdma::class.java)
        val mockCsgInfo = mock(ClosedSubscriberGroupInfo::class.java)

        `when`(mockCell.mccString).thenReturn("460")
        `when`(mockCell.mncString).thenReturn("00")
        `when`(mockCell.operatorAlphaLong).thenReturn("TD-SCDMA Long")
        `when`(mockCell.operatorAlphaShort).thenReturn("TD-S")
        `when`(mockCell.mobileNetworkOperator).thenReturn("46000")
        `when`(mockCell.lac).thenReturn(12345)
        `when`(mockCell.cpid).thenReturn(127)
        `when`(mockCell.uarfcn).thenReturn(10054)
        `when`(mockCell.cid).thenReturn(268435455)
        `when`(mockCsgInfo.csgIdentity).thenReturn(123456)
        `when`(mockCsgInfo.csgIndicator).thenReturn(true)
        `when`(mockCsgInfo.homeNodebName).thenReturn("TestNodeB")
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(mockCsgInfo)

        val data = CellIdentityTdscdmaData(cellIdentity = mockCell)

        assertEquals("460", data.getMcc())
        assertEquals("00", data.getMnc())
        assertEquals("TD-SCDMA Long", data.getOperatorAlphaLong())
        assertEquals("TD-S", data.getOperatorAlphaShort())
        assertEquals("46000", data.getMobileNetworkOperator())
        assertEquals(12345, data.getLac())
        assertEquals(127, data.getCpid())
        assertEquals(10054, data.getUarfcn())
        assertEquals(268435455, data.getUCid())
        assertEquals(123456, data.getCsgIdentity())
        assertTrue(data.getCsgIndicator() == true)
        assertEquals("TestNodeB", data.getHomeNodebName())
    }
}
