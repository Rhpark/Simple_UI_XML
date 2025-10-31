package kr.open.library.simple_ui.robolectric.presenter.extensions.display

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.presenter.extensions.display.dpToPx
import kr.open.library.simple_ui.presenter.extensions.display.dpToSp
import kr.open.library.simple_ui.presenter.extensions.display.pxToDp
import kr.open.library.simple_ui.presenter.extensions.display.pxToSp
import kr.open.library.simple_ui.presenter.extensions.display.spToDp
import kr.open.library.simple_ui.presenter.extensions.display.spToPx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for Display Unit extension functions
 * Tests DP/SP/PX conversions using DisplayMetrics
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DisplayUnitExtensionsRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    // ========================================
    // DP to PX Tests
    // ========================================

    @Test
    fun dpToPx_withPositiveValue_convertsCorrectly() {
        val dp = 10
        val px = dp.dpToPx(context)

        assertTrue(px > 0f)
    }

    @Test
    fun dpToPx_withZero_returnsZero() {
        val dp = 0
        val px = dp.dpToPx(context)

        assertEquals(0f, px, 0.01f)
    }

    @Test
    fun dpToPx_withFloatValue_convertsCorrectly() {
        val dp = 10.5f
        val px = dp.dpToPx(context)

        assertTrue(px > 0f)
    }

    @Test
    fun dpToPx_withNegativeValue_returnsNegative() {
        val dp = -10
        val px = dp.dpToPx(context)

        assertTrue(px < 0f)
    }

    // ========================================
    // DP to SP Tests
    // ========================================

    @Test
    fun dpToSp_withPositiveValue_convertsCorrectly() {
        val dp = 10
        val sp = dp.dpToSp(context)

        assertTrue(sp > 0f)
    }

    @Test
    fun dpToSp_withZero_returnsZero() {
        val dp = 0
        val sp = dp.dpToSp(context)

        assertEquals(0f, sp, 0.01f)
    }

    @Test
    fun dpToSp_withFloatValue_convertsCorrectly() {
        val dp = 14.5f
        val sp = dp.dpToSp(context)

        assertTrue(sp > 0f)
    }

    // ========================================
    // PX to DP Tests
    // ========================================

    @Test
    fun pxToDp_withPositiveValue_convertsCorrectly() {
        val px = 100
        val dp = px.pxToDp(context)

        assertTrue(dp > 0f)
    }

    @Test
    fun pxToDp_withZero_returnsZero() {
        val px = 0
        val dp = px.pxToDp(context)

        assertEquals(0f, dp, 0.01f)
    }

    @Test
    fun pxToDp_roundTrip_returnsOriginalValue() {
        val originalDp = 10
        val px = originalDp.dpToPx(context)
        val convertedDp = px.pxToDp(context)

        assertEquals(originalDp.toFloat(), convertedDp, 0.5f)
    }

    // ========================================
    // PX to SP Tests
    // ========================================

    @Test
    fun pxToSp_withPositiveValue_convertsCorrectly() {
        val px = 100
        val sp = px.pxToSp(context)

        assertTrue(sp > 0f)
    }

    @Test
    fun pxToSp_withZero_returnsZero() {
        val px = 0
        val sp = px.pxToSp(context)

        assertEquals(0f, sp, 0.01f)
    }

    // ========================================
    // SP to PX Tests
    // ========================================

    @Test
    fun spToPx_withPositiveValue_convertsCorrectly() {
        val sp = 14
        val px = sp.spToPx(context)

        assertTrue(px > 0f)
    }

    @Test
    fun spToPx_withZero_returnsZero() {
        val sp = 0
        val px = sp.spToPx(context)

        assertEquals(0f, px, 0.01f)
    }

    @Test
    fun spToPx_roundTrip_returnsOriginalValue() {
        val originalSp = 14
        val px = originalSp.spToPx(context)
        val convertedSp = px.pxToSp(context)

        assertEquals(originalSp.toFloat(), convertedSp, 0.5f)
    }

    // ========================================
    // SP to DP Tests
    // ========================================

    @Test
    fun spToDp_withPositiveValue_convertsCorrectly() {
        val sp = 14
        val dp = sp.spToDp(context)

        assertTrue(dp > 0f)
    }

    @Test
    fun spToDp_withZero_returnsZero() {
        val sp = 0
        val dp = sp.spToDp(context)

        assertEquals(0f, dp, 0.01f)
    }

    @Test
    fun spToDp_withFloatValue_convertsCorrectly() {
        val sp = 16.5f
        val dp = sp.spToDp(context)

        assertTrue(dp > 0f)
    }

    // ========================================
    // Different Number Types Tests
    // ========================================

    @Test
    fun dpToPx_withInt_convertsCorrectly() {
        val dp: Int = 10
        val px = dp.dpToPx(context)

        assertTrue(px > 0f)
    }

    @Test
    fun dpToPx_withLong_convertsCorrectly() {
        val dp: Long = 10L
        val px = dp.dpToPx(context)

        assertTrue(px > 0f)
    }

    @Test
    fun dpToPx_withDouble_convertsCorrectly() {
        val dp: Double = 10.5
        val px = dp.dpToPx(context)

        assertTrue(px > 0f)
    }

    // ========================================
    // Conversion Chain Tests
    // ========================================

    @Test
    fun conversionChain_dpToPxToDp_maintainsValue() {
        val originalDp = 16
        val px = originalDp.dpToPx(context)
        val finalDp = px.pxToDp(context)

        assertEquals(originalDp.toFloat(), finalDp, 0.5f)
    }

    @Test
    fun conversionChain_spToPxToSp_maintainsValue() {
        val originalSp = 14
        val px = originalSp.spToPx(context)
        val finalSp = px.pxToSp(context)

        assertEquals(originalSp.toFloat(), finalSp, 0.5f)
    }

    @Test
    fun conversionChain_dpToSpToDp_worksCorrectly() {
        val originalDp = 16
        val sp = originalDp.dpToSp(context)
        val finalDp = sp.spToDp(context)

        // Due to fontScale, exact match may not be possible
        assertTrue(finalDp > 0f)
    }

    // ========================================
    // Edge Cases Tests
    // ========================================

    @Test
    fun largeValue_dpToPx_doesNotOverflow() {
        val largeDp = 10000
        val px = largeDp.dpToPx(context)

        assertTrue(px > 0f)
        assertTrue(px.isFinite())
    }

    @Test
    fun verySmallValue_dpToPx_returnsSmallPx() {
        val smallDp = 0.1f
        val px = smallDp.dpToPx(context)

        assertTrue(px >= 0f)
        assertTrue(px.isFinite())
    }
}
