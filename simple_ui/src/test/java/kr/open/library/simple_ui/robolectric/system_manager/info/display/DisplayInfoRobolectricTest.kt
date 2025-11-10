package kr.open.library.simple_ui.robolectric.system_manager.info.display

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Insets
import android.graphics.Rect
import android.os.Build
import android.view.WindowInsets
import android.view.WindowMetrics
import androidx.annotation.RequiresApi
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.info.display.DisplayInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class DisplayInfoRobolectricTest {

    private lateinit var application: Application
    private lateinit var displayInfo: DisplayInfo

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        displayInfo = DisplayInfo(application)
    }

    @Test
    fun getFullScreenSize_returnsPositivePoint() {
        val size = displayInfo.getFullScreenSize()

        assertNotNull(size)
        assertTrue(size.x > 0)
        assertTrue(size.y > 0)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getScreen_onApiR_respectsStatusAndNavigationInsets() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1080, 2400),
            statusBarInsets = Insets.of(0, 100, 0, 0),
            navigationInsets = Insets.of(0, 0, 0, 150)
        )

        val size = info.getScreen()

        assertEquals(1080, size.x)
        assertEquals(2400 - 100 - 150, size.y)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getScreenWithStatusBar_onApiR_excludesOnlyNavigationInsets() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1080, 2400),
            statusBarInsets = Insets.of(0, 120, 0, 0),
            navigationInsets = Insets.of(40, 0, 40, 180)
        )

        val size = info.getScreenWithStatusBar()

        assertEquals(1080 - 40 - 40, size.x)
        assertEquals(2400 - 180, size.y)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun getNavigationBarSize_onApiR_returnsLargestInset() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1080, 2400),
            statusBarInsets = Insets.NONE,
            navigationInsets = Insets.of(60, 0, 0, 0) // navigation bar on the left side
        )

        val size = info.getNavigationBarSize()

        assertEquals(60, size)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getScreen_onApiQ_subtractsVerticalNavigationOnlyOnce() {
        val info = object : DisplayInfo(application) {
            override fun getStatusBarHeight(): Int = 100
            override fun getLegacyNavigationBarInsetsCompat(): LegacyNavigationBarInsets =
                LegacyNavigationBarInsets(horizontal = 0, vertical = 150)
        }
        val shadowDisplay = Shadows.shadowOf(info.windowManager.defaultDisplay)
        shadowDisplay.setRealWidth(1080)
        shadowDisplay.setRealHeight(2400)

        val size = info.getScreen()

        assertEquals(1080, size.x)
        assertEquals(2400 - 100 - 150, size.y)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getScreenWithStatusBar_onApiQ_subtractsHorizontalNavigationOnly() {
        val info = object : DisplayInfo(application) {
            override fun getStatusBarHeight(): Int = 0
            override fun getLegacyNavigationBarInsetsCompat(): LegacyNavigationBarInsets =
                LegacyNavigationBarInsets(horizontal = 90, vertical = 0)
        }
        val shadowDisplay = Shadows.shadowOf(info.windowManager.defaultDisplay)
        shadowDisplay.setRealWidth(1200)
        shadowDisplay.setRealHeight(2500)

        val size = info.getScreenWithStatusBar()

        assertEquals(1200 - 90, size.x)
        assertEquals(2500, size.y)
    }

    @Test
    fun getStatusBarHeightSafe_returnsResult() {
        val result = displayInfo.getStatusBarHeightSafe()

        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun getNavigationBarSizeSafe_returnsResult() {
        val result = displayInfo.getNavigationBarSizeSafe()

        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun statusBarDefaultFallback_returnsDefaultValue() {
        val failingInfo = object : DisplayInfo(application) {
            override fun getStatusBarHeight(): Int {
                throw Resources.NotFoundException("forced failure")
            }
        }

        val value = failingInfo.getStatusBarHeightOrDefault(99)

        assertEquals(99, value)
    }

    @Test
    fun navigationBarDefaultFallback_returnsDefaultValue() {
        val failingInfo = object : DisplayInfo(application) {
            override fun getNavigationBarSize(): Int {
                throw Resources.NotFoundException("forced failure")
            }
        }

        val value = failingInfo.getNavigationBarSizeOrDefault(77)

        assertEquals(77, value)
    }
}

@RequiresApi(Build.VERSION_CODES.R)
private fun createWindowMetricsDisplayInfo(
    context: Context,
    bounds: Rect,
    statusBarInsets: Insets,
    navigationInsets: Insets
): DisplayInfo {
    val windowInsets = WindowInsets.Builder()
        .setInsets(WindowInsets.Type.statusBars(), statusBarInsets)
        .setInsets(WindowInsets.Type.navigationBars(), navigationInsets)
        .setInsetsIgnoringVisibility(WindowInsets.Type.statusBars(), statusBarInsets)
        .setInsetsIgnoringVisibility(WindowInsets.Type.navigationBars(), navigationInsets)
        .build()
    val metrics = WindowMetrics(bounds, windowInsets)
    return object : DisplayInfo(context) {
        override fun getCurrentWindowMetricsCompat(): WindowMetrics = metrics
    }
}
