package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.wcdma

import android.os.Build
import android.telephony.CellIdentityWcdma
import android.telephony.ClosedSubscriberGroupInfo
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.wcdma.CellIdentityWcdmaData
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
 * Robolectric test for CellIdentityWcdmaData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellIdentityWcdmaDataRobolectricTest {
    // ==============================================
    // Constructor and Initialization Tests
    // ==============================================

    @Test
    fun constructor_withNullCellIdentity_createsInstance() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        assertNotNull(data)
        assertNull(data.cellIdentity)
    }

    @Test
    fun constructor_withValidCellIdentity_createsInstance() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        assertNotNull(data)
        assertNotNull(data.cellIdentity)
        assertEquals(mockCell, data.cellIdentity)
    }

    // ==============================================
    // MCC Tests
    // ==============================================

    @Test
    fun getMcc_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getMcc()

        assertNull(result)
    }

    @Test
    fun getMcc_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.mccString).thenReturn("450")
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getMcc()

        assertEquals("450", result)
    }

    // ==============================================
    // MNC Tests
    // ==============================================

    @Test
    fun getMnc_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getMnc()

        assertNull(result)
    }

    @Test
    fun getMnc_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.mncString).thenReturn("05")
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getMnc()

        assertEquals("05", result)
    }

    // ==============================================
    // Operator Alpha Long Tests
    // ==============================================

    @Test
    fun getOperatorAlphaLong_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getOperatorAlphaLong()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaLong_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.operatorAlphaLong).thenReturn("WCDMA Operator Long")
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaLong()

        assertEquals("WCDMA Operator Long", result)
    }

    // ==============================================
    // Operator Alpha Short Tests
    // ==============================================

    @Test
    fun getOperatorAlphaShort_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getOperatorAlphaShort()

        assertNull(result)
    }

    @Test
    fun getOperatorAlphaShort_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.operatorAlphaShort).thenReturn("WCDMA Op")
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getOperatorAlphaShort()

        assertEquals("WCDMA Op", result)
    }

    // ==============================================
    // Mobile Network Operator Tests
    // ==============================================

    @Test
    fun getMobileNetworkOperator_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getMobileNetworkOperator()

        assertNull(result)
    }

    @Test
    fun getMobileNetworkOperator_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.mobileNetworkOperator).thenReturn("45005")
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getMobileNetworkOperator()

        assertEquals("45005", result)
    }

    // ==============================================
    // LAC Tests
    // ==============================================

    @Test
    fun getLac_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getLac()

        assertNull(result)
    }

    @Test
    fun getLac_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.lac).thenReturn(12345)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getLac()

        assertEquals(12345, result)
    }

    // ==============================================
    // PSC Tests
    // ==============================================

    @Test
    fun getPsc_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getPsc()

        assertNull(result)
    }

    @Test
    fun getPsc_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.psc).thenReturn(511)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getPsc()

        assertEquals(511, result)
    }

    // ==============================================
    // UARFCN Tests
    // ==============================================

    @Test
    fun getUarfcn_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getUarfcn()

        assertNull(result)
    }

    @Test
    fun getUarfcn_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.uarfcn).thenReturn(10562)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getUarfcn()

        assertEquals(10562, result)
    }

    // ==============================================
    // UCID Tests
    // ==============================================

    @Test
    fun getUCid_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getUCid()

        assertNull(result)
    }

    @Test
    fun getUCid_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.cid).thenReturn(268435455)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getUCid()

        assertEquals(268435455, result)
    }

    // ==============================================
    // CID Tests (Calculated from raw CID)
    // ==============================================

    @Test
    fun getCid_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getCid()

        assertNull(result)
    }

    @Test
    fun getCid_withValidValue_returnsCorrectCalculation() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        // CID = 0x12345 (74565) -> Lower 16 bits = 74565 & 0xFFFF = 9029
        `when`(mockCell.cid).thenReturn(74565)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getCid()

        assertEquals(9029, result)
    }

    // ==============================================
    // Home RNC Tests (Calculated from raw CID)
    // ==============================================

    @Test
    fun getHomeRnc_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getHomeRnc()

        assertNull(result)
    }

    @Test
    fun getHomeRnc_withValidValue_returnsCorrectCalculation() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        // CID = 0x12345 (74565) -> (74565 >> 16) & 0xFFF = 1
        `when`(mockCell.cid).thenReturn(74565)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getHomeRnc()

        assertEquals(1, result)
    }

    // ==============================================
    // CSG Identity Tests (API 30+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIdentity_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getCsgIdentity()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIdentity_withNullCsgInfo_returnsNull() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(null)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getCsgIdentity()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIdentity_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        val mockCsgInfo = mock(ClosedSubscriberGroupInfo::class.java)
        `when`(mockCsgInfo.csgIdentity).thenReturn(789012)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(mockCsgInfo)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getCsgIdentity()

        assertEquals(789012, result)
    }

    // ==============================================
    // CSG Indicator Tests (API 30+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIndicator_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getCsgIndicator()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIndicator_withTrueValue_returnsTrue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        val mockCsgInfo = mock(ClosedSubscriberGroupInfo::class.java)
        `when`(mockCsgInfo.csgIndicator).thenReturn(true)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(mockCsgInfo)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getCsgIndicator()

        assertTrue(result == true)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIndicator_withFalseValue_returnsFalse() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        val mockCsgInfo = mock(ClosedSubscriberGroupInfo::class.java)
        `when`(mockCsgInfo.csgIndicator).thenReturn(false)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(mockCsgInfo)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getCsgIndicator()

        assertFalse(result == true)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getCsgIndicator_withNullCsgInfo_returnsNull() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(null)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getCsgIndicator()

        assertNull(result)
    }

    // ==============================================
    // Home NodeB Name Tests (API 30+)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getHomeNodebName_withNullCellIdentity_returnsNull() {
        val data = CellIdentityWcdmaData(cellIdentity = null)

        val result = data.getHomeNodebName()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getHomeNodebName_withNullCsgInfo_returnsNull() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(null)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getHomeNodebName()

        assertNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getHomeNodebName_withValidValue_returnsCorrectValue() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        val mockCsgInfo = mock(ClosedSubscriberGroupInfo::class.java)
        `when`(mockCsgInfo.homeNodebName).thenReturn("WCDMANodeB")
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(mockCsgInfo)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.getHomeNodebName()

        assertEquals("WCDMANodeB", result)
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameIdentityProducesSameData() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        val data1 = CellIdentityWcdmaData(cellIdentity = mockCell)
        val data2 = CellIdentityWcdmaData(cellIdentity = mockCell)

        assertEquals(data1, data2)
    }

    @Test
    fun dataClass_copy_createsNewInstance() {
        val mockCell1 = mock(CellIdentityWcdma::class.java)
        val mockCell2 = mock(CellIdentityWcdma::class.java)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell1)

        val copied = data.copy(cellIdentity = mockCell2)

        assertEquals(mockCell1, data.cellIdentity)
        assertEquals(mockCell2, copied.cellIdentity)
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        val result = data.toString()

        assertNotNull(result)
    }

    // ==============================================
    // Integration Test - All Values
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedCell_returnCorrectValues() {
        val mockCell = mock(CellIdentityWcdma::class.java)
        val mockCsgInfo = mock(ClosedSubscriberGroupInfo::class.java)

        `when`(mockCell.mccString).thenReturn("450")
        `when`(mockCell.mncString).thenReturn("05")
        `when`(mockCell.operatorAlphaLong).thenReturn("WCDMA Long")
        `when`(mockCell.operatorAlphaShort).thenReturn("WCDMA")
        `when`(mockCell.mobileNetworkOperator).thenReturn("45005")
        `when`(mockCell.lac).thenReturn(12345)
        `when`(mockCell.psc).thenReturn(511)
        `when`(mockCell.uarfcn).thenReturn(10562)
        `when`(mockCell.cid).thenReturn(268435455) // 0xFFFFFFF
        `when`(mockCsgInfo.csgIdentity).thenReturn(789012)
        `when`(mockCsgInfo.csgIndicator).thenReturn(true)
        `when`(mockCsgInfo.homeNodebName).thenReturn("WCDMANodeB")
        `when`(mockCell.closedSubscriberGroupInfo).thenReturn(mockCsgInfo)

        val data = CellIdentityWcdmaData(cellIdentity = mockCell)

        assertEquals("450", data.getMcc())
        assertEquals("05", data.getMnc())
        assertEquals("WCDMA Long", data.getOperatorAlphaLong())
        assertEquals("WCDMA", data.getOperatorAlphaShort())
        assertEquals("45005", data.getMobileNetworkOperator())
        assertEquals(12345, data.getLac())
        assertEquals(511, data.getPsc())
        assertEquals(10562, data.getUarfcn())
        assertEquals(268435455, data.getUCid())
        assertEquals(65535, data.getCid()) // 268435455 & 0xFFFF
        assertEquals(4095, data.getHomeRnc()) // (268435455 >> 16) & 0xFFF
        assertEquals(789012, data.getCsgIdentity())
        assertTrue(data.getCsgIndicator() == true)
        assertEquals("WCDMANodeB", data.getHomeNodebName())
    }
}
