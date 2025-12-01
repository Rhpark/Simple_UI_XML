package kr.open.library.simple_ui.core.robolectric.system_manager.info.network.telephony.data.cell.tdscdma

import android.os.Build
import android.telephony.CellIdentityTdscdma
import android.telephony.CellInfoTdscdma
import android.telephony.CellSignalStrengthTdscdma
import kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.tdscdma.CellInfoTdscdmaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test for CellInfoTdscdmaData
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CellInfoTdscdmaDataRobolectricTest {
    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun constructor_withValidCellInfo_createsInstance() {
        val mockInfo = mock(CellInfoTdscdma::class.java)
        val mockIdentity = mock(CellIdentityTdscdma::class.java)
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoTdscdmaData(cellInfo = mockInfo)

        assertNotNull(data)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getTimestampMillis_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoTdscdma::class.java)
        val mockIdentity = mock(CellIdentityTdscdma::class.java)
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(8888888888888L)

        val data = CellInfoTdscdmaData(cellInfo = mockInfo)

        assertEquals(8888888888888L, data.getTimestampMillis())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun isRegistered_withTrueValue_returnsTrue() {
        val mockInfo = mock(CellInfoTdscdma::class.java)
        val mockIdentity = mock(CellIdentityTdscdma::class.java)
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(true)

        val data = CellInfoTdscdmaData(cellInfo = mockInfo)

        assertTrue(data.isRegistered())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun isRegistered_withFalseValue_returnsFalse() {
        val mockInfo = mock(CellInfoTdscdma::class.java)
        val mockIdentity = mock(CellIdentityTdscdma::class.java)
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.isRegistered).thenReturn(false)

        val data = CellInfoTdscdmaData(cellInfo = mockInfo)

        assertFalse(data.isRegistered())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getCellConnectionStatus_withValidValue_returnsCorrectValue() {
        val mockInfo = mock(CellInfoTdscdma::class.java)
        val mockIdentity = mock(CellIdentityTdscdma::class.java)
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.cellConnectionStatus).thenReturn(1)

        val data = CellInfoTdscdmaData(cellInfo = mockInfo)

        assertEquals(1, data.getCellConnectionStatus())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getIdentity_withValidCellInfo_returnsIdentityData() {
        val mockInfo = mock(CellInfoTdscdma::class.java)
        val mockIdentity = mock(CellIdentityTdscdma::class.java)
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoTdscdmaData(cellInfo = mockInfo)

        val result = data.getIdentity()

        assertNotNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getSignalStrength_withValidCellInfo_returnsSignalStrengthData() {
        val mockInfo = mock(CellInfoTdscdma::class.java)
        val mockIdentity = mock(CellIdentityTdscdma::class.java)
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)
        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)

        val data = CellInfoTdscdmaData(cellInfo = mockInfo)

        val result = data.getSignalStrength()

        assertNotNull(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun integration_allGetters_withFullyMockedInfo_returnCorrectValues() {
        val mockInfo = mock(CellInfoTdscdma::class.java)
        val mockIdentity = mock(CellIdentityTdscdma::class.java)
        val mockSignal = mock(CellSignalStrengthTdscdma::class.java)

        `when`(mockInfo.cellIdentity).thenReturn(mockIdentity)
        `when`(mockInfo.cellSignalStrength).thenReturn(mockSignal)
        `when`(mockInfo.timestampMillis).thenReturn(8888888888888L)
        `when`(mockInfo.isRegistered).thenReturn(true)
        `when`(mockInfo.cellConnectionStatus).thenReturn(1)

        val data = CellInfoTdscdmaData(cellInfo = mockInfo)

        assertEquals(8888888888888L, data.getTimestampMillis())
        assertTrue(data.isRegistered())
        assertEquals(1, data.getCellConnectionStatus())
        assertNotNull(data.getIdentity())
        assertNotNull(data.getSignalStrength())
    }
}
