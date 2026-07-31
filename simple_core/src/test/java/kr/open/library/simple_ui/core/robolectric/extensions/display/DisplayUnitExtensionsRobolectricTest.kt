package kr.open.library.simple_ui.core.robolectric.extensions.display

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.extensions.display.dpToPx
import kr.open.library.simple_ui.core.extensions.display.dpToSp
import kr.open.library.simple_ui.core.extensions.display.pxToDp
import kr.open.library.simple_ui.core.extensions.display.pxToSp
import kr.open.library.simple_ui.core.extensions.display.spToDp
import kr.open.library.simple_ui.core.extensions.display.spToPx
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Robolectric tests for public display-unit conversion extensions.<br><br>
 * 공개 디스플레이 단위 변환 확장 함수의 density·fontScale 계약을 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], qualifiers = "xhdpi")
class DisplayUnitExtensionsRobolectricTest {
    private lateinit var app: Application
    private var originalFontScale: Float = 0f

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        originalFontScale = RuntimeEnvironment.getFontScale()
        RuntimeEnvironment.setFontScale(FONT_SCALE)
    }

    @After
    fun tearDown() {
        RuntimeEnvironment.setFontScale(originalFontScale)
    }

    /**
     * Verifies that dp-to-px conversion applies display density.<br><br>
     * dp→px 변환에 화면 density가 적용되는지 검증합니다.<br>
     */
    @Test
    fun dpToPx_appliesDisplayDensity() {
        assertEquals(20f, 10.dpToPx(app), DELTA)
    }

    /**
     * Verifies that dp-to-sp conversion accounts for font scale.<br><br>
     * dp→sp 변환에 fontScale이 반영되는지 검증합니다.<br>
     */
    @Test
    fun dpToSp_appliesFontScale() {
        assertEquals(8f, 12.dpToSp(app), DELTA)
    }

    /**
     * Verifies that px-to-dp conversion divides by display density.<br><br>
     * px→dp 변환이 화면 density로 나누어지는지 검증합니다.<br>
     */
    @Test
    fun pxToDp_appliesDisplayDensity() {
        assertEquals(10f, 20.pxToDp(app), DELTA)
    }

    /**
     * Verifies that px-to-sp conversion accounts for density and font scale.<br><br>
     * px→sp 변환에 density와 fontScale이 모두 반영되는지 검증합니다.<br>
     */
    @Test
    fun pxToSp_appliesDensityAndFontScale() {
        assertEquals(10f, 30.pxToSp(app), DELTA)
    }

    /**
     * Verifies that sp-to-px conversion applies scaled density.<br><br>
     * sp→px 변환에 scaledDensity가 적용되는지 검증합니다.<br>
     */
    @Test
    fun spToPx_appliesScaledDensity() {
        assertEquals(30f, 10.spToPx(app), DELTA)
    }

    /**
     * Verifies that sp-to-dp conversion multiplies by font scale.<br><br>
     * sp→dp 변환에 fontScale을 곱하는지 검증합니다.<br>
     */
    @Test
    fun spToDp_appliesFontScale() {
        assertEquals(18f, 12.spToDp(app), DELTA)
    }

    private companion object {
        const val DENSITY = 2f
        const val FONT_SCALE = 1.5f
        const val DELTA = 0.001f
    }
}
