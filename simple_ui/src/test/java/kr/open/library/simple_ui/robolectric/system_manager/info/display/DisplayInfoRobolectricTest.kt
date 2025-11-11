package kr.open.library.simple_ui.robolectric.system_manager.info.display

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Insets
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.WindowInsets
import android.view.WindowMetrics
import androidx.annotation.RequiresApi
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.info.display.DisplayInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.spy
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class DisplayInfoRobolectricTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun getFullScreenSize_onApiR_matchesWindowBounds() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1440, 3088),
            statusBarInsets = Insets.of(0, 100, 0, 0),
            navigationInsets = Insets.of(0, 0, 0, 160),
        )

        assertEquals(Point(1440, 3088), info.getFullScreenSize())
        assertEquals(1440, info.getFullScreenWidth())
        assertEquals(3088, info.getFullScreenHeight())
    }

    @Test
    fun getStatusBarSize_onApiR_reflectsInsetsAndFullWidth() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1080, 2400),
            statusBarInsets = Insets.of(0, 80, 0, 0),
            navigationInsets = Insets.of(0, 0, 0, 140),
        )

        val statusSize = info.getStatusBarSize()

        assertEquals(info.getFullScreenWidth(), statusSize.x)
        assertEquals(80, statusSize.y)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getStatusBarSize_onLegacy_usesAndroidResources() {
        val info = DisplayInfo(application)
        configureLegacyDisplay(info, width = 1080, height = 2240)

        val expectedHeight = requireAndroidDimen(application.resources, "status_bar_height")

        val size = info.getStatusBarSize()

        assertEquals(info.getFullScreenWidth(), size.x)
        assertEquals(expectedHeight, size.y)
    }

    @Test
    fun getStatusBarHeight_returnsFailureWhenSizeThrows() {
        val failingInfo = spy(DisplayInfo(application))
        doThrow(Resources.NotFoundException("boom")).`when`(failingInfo).getStatusBarSize()

        val heightResult = failingInfo.getStatusBarHeight()

        assertTrue(heightResult.isFailure)
    }

    @Test
    fun getStatusBarWidth_returnsFailureWhenSizeThrows() {
        val failingInfo = spy(DisplayInfo(application))
        doThrow(Resources.NotFoundException("boom")).`when`(failingInfo).getStatusBarSize()

        val widthResult = failingInfo.getStatusBarWidth()

        assertTrue(widthResult.isFailure)
    }

    @Test
    fun getNavigationBarSize_onApiR_bottomBarUsesFullWidth() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1200, 2600),
            statusBarInsets = Insets.of(0, 70, 0, 0),
            navigationInsets = Insets.of(0, 0, 0, 200),
        )

        val navSize = info.getNavigationBarSize()

        assertEquals(1200, navSize.x)
        assertEquals(200, navSize.y)
    }

    @Test
    fun getNavigationBarSize_onApiR_sideBarUsesInsetWidth() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 2000, 1400),
            statusBarInsets = Insets.of(0, 50, 0, 0),
            navigationInsets = Insets.of(90, 0, 0, 0),
        )

        val navSize = info.getNavigationBarSize()

        assertEquals(90, navSize.x)
        assertEquals(1400, navSize.y)
    }

    @Test
    fun getNavigationBarSize_onApiR_topBarUsesFullWidth() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1300, 1900),
            statusBarInsets = Insets.of(0, 50, 0, 0),
            navigationInsets = Insets.of(0, 120, 0, 0),
        )

        val navSize = info.getNavigationBarSize()

        assertEquals(1300, navSize.x)
        assertEquals(120, navSize.y)
    }

    @Test(expected = Resources.NotFoundException::class)
    fun getNavigationBarSize_onApiR_hiddenNavigationThrows() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1440, 3000),
            statusBarInsets = Insets.NONE,
            navigationInsets = Insets.NONE,
        )

        info.getNavigationBarSize()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getNavigationBarSize_onLegacy_portraitUsesHeightResource() {
        val info = DisplayInfo(application)
        configureLegacyDisplay(info, 1080, 2400)
        setOrientation(application.resources, Configuration.ORIENTATION_PORTRAIT)

        val expectedHeight = requireAndroidDimen(application.resources, "navigation_bar_height")

        val navSize = info.getNavigationBarSize()

        assertEquals(info.getFullScreenWidth(), navSize.x)
        assertEquals(expectedHeight, navSize.y)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getNavigationBarSize_onLegacy_landscapePrefersSideWidth() {
        val info = DisplayInfo(application)
        configureLegacyDisplay(info, 1920, 1200)

        val resources = application.resources
        setOrientation(resources, Configuration.ORIENTATION_LANDSCAPE)

        val sideWidth = requireAndroidDimen(resources, "navigation_bar_width")
        val landscapeHeight = requireAndroidDimen(resources, "navigation_bar_height_landscape")
        assumeTrue("side width should be >= landscape height for this test", sideWidth >= landscapeHeight)

        val navSize = info.getNavigationBarSize()

        assertEquals(sideWidth, navSize.x)
        assertEquals(info.getFullScreenHeight(), navSize.y)
    }

    @Test
    fun getScreenWidthHeight_onApiR_withBottomNavigation() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1080, 2400),
            statusBarInsets = Insets.of(0, 90, 0, 0),
            navigationInsets = Insets.of(0, 0, 0, 150),
        )

        assertEquals(1080, info.getScreenWidth())
        assertEquals(2400 - 90 - 150, info.getScreenHeight())
        assertEquals(Point(1080, 2400 - 90 - 150), info.getScreenSize())
    }

    @Test
    fun getScreenWidthHeight_onApiR_withSideNavigation() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 2200, 1600),
            statusBarInsets = Insets.of(0, 60, 0, 0),
            navigationInsets = Insets.of(120, 0, 0, 0),
        )

        assertEquals(2200 - 120, info.getScreenWidth())
        assertEquals(1600 - 60, info.getScreenHeight())
    }

    @Test
    fun getScreenWidth_whenNavigationWidthNotSmallerThanFullWidth_returnsFullWidth() {
        val spyInfo = spy(DisplayInfo(application))
        doReturn(Point(2000, 0)).`when`(spyInfo).getNavigationBarSize()
        doReturn(2000).`when`(spyInfo).getFullScreenWidth()

        assertEquals(2000, spyInfo.getScreenWidth())
    }

    @Test
    fun getScreenHeight_whenNavigationHeightNotSmallerThanFullHeight_returnsFullMinusStatus() {
        val spyInfo = spy(DisplayInfo(application))
        doReturn(Point(0, 2200)).`when`(spyInfo).getNavigationBarSize()
        doReturn(2000).`when`(spyInfo).getFullScreenHeight()
        doReturn(Result.success(150)).`when`(spyInfo).getStatusBarHeight()

        assertEquals(2000 - 150, spyInfo.getScreenHeight())
    }

    @Test
    fun isStatusBarHided_reflectsInsetZeroState() {
        val shownInfo = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1080, 2000),
            statusBarInsets = Insets.of(0, 80, 0, 0),
            navigationInsets = Insets.of(0, 0, 0, 120),
        )
        assertFalse(shownInfo.isStatusBarHided())

        val hiddenInfo = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1080, 2000),
            statusBarInsets = Insets.NONE,
            navigationInsets = Insets.of(0, 0, 0, 120),
        )
        assertTrue(hiddenInfo.isStatusBarHided())
    }

    @Test
    fun isNavigationBarHided_reliesOnNavigationBarHeight() {
        val spyInfo = spy(DisplayInfo(application))
        doReturn(Point(0, 0)).`when`(spyInfo).getNavigationBarSize()

        assertTrue(spyInfo.isNavigationBarHided())
    }

    @Test
    fun isNavigationBarHided_returnsFalseWhenHeightPositive() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1000, 1600),
            statusBarInsets = Insets.of(0, 50, 0, 0),
            navigationInsets = Insets.of(0, 0, 0, 120),
        )

        assertFalse(info.isNavigationBarHided())
    }

    @Test
    fun isFullScreen_reflectsEqualityBetweenFullAndUsableSizes() {
        val info = createWindowMetricsDisplayInfo(
            context = application,
            bounds = Rect(0, 0, 1440, 2800),
            statusBarInsets = Insets.of(0, 80, 0, 0),
            navigationInsets = Insets.of(0, 0, 0, 140),
        )
        assertFalse(info.isFullScreen())

        val fullSpy = spy(DisplayInfo(application))
        doReturn(1080).`when`(fullSpy).getFullScreenWidth()
        doReturn(1920).`when`(fullSpy).getFullScreenHeight()
        doReturn(1080).`when`(fullSpy).getScreenWidth()
        doReturn(1920).`when`(fullSpy).getScreenHeight()
        assertTrue(fullSpy.isFullScreen())
    }
}

@Config(sdk = [Build.VERSION_CODES.R])
private fun createWindowMetricsDisplayInfo(
    context: Context,
    bounds: Rect,
    statusBarInsets: Insets,
    navigationInsets: Insets,
): DisplayInfo {
    val windowInsets = WindowInsets.Builder()
        .setInsets(WindowInsets.Type.statusBars(), statusBarInsets)
        .setInsets(WindowInsets.Type.navigationBars(), navigationInsets)
        .setInsetsIgnoringVisibility(WindowInsets.Type.statusBars(), statusBarInsets)
        .setInsetsIgnoringVisibility(WindowInsets.Type.navigationBars(), navigationInsets)
        .build()
    val metrics = WindowMetrics(bounds, windowInsets)
    return object : DisplayInfo(context) {
        override fun getCurrentWindowMetricsSdkR(): WindowMetrics = metrics
    }
}

private fun configureLegacyDisplay(info: DisplayInfo, width: Int, height: Int) {
    val shadowDisplay = Shadows.shadowOf(info.windowManager.defaultDisplay)
    shadowDisplay.setRealWidth(width)
    shadowDisplay.setRealHeight(height)
}

private fun setOrientation(resources: Resources, orientation: Int) {
    val configuration = resources.configuration
    configuration.orientation = orientation
    resources.updateConfiguration(configuration, resources.displayMetrics)
}

private fun requireAndroidDimen(resources: Resources, name: String): Int {
    val resId = resources.getIdentifier(name, "dimen", "android")
    require(resId > 0) { "Missing android dimen: $name" }
    return resources.getDimensionPixelSize(resId)
}
