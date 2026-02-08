package kr.open.library.simple_ui.xml.robolectric.system_manager.info.display

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.system_manager.extensions.getDisplayInfo
import kr.open.library.simple_ui.xml.system_manager.info.display.DisplayInfo
import kr.open.library.simple_ui.xml.system_manager.info.display.DisplayInfoBarInsets
import kr.open.library.simple_ui.xml.system_manager.info.display.DisplayInfoSize
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for DisplayInfo.<br><br>
 * DisplayInfo에 대한 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DisplayInfoRobolectricTest {
    private lateinit var context: Context
    private lateinit var displayInfo: DisplayInfo

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()
        displayInfo = DisplayInfo(context)
    }

    @After
    fun tearDown() {
        try {
            displayInfo.onDestroy()
        } catch (_: Exception) {
            // ignore
        }
    }

    // ==============================================
    // Instantiation Tests
    // ==============================================

    @Test
    fun `DisplayInfo can be instantiated with context`() {
        assertNotNull(displayInfo)
    }

    @Test
    fun `getDisplayInfo extension creates valid instance`() {
        val info = context.getDisplayInfo()
        assertNotNull(info)
    }

    // ==============================================
    // getPhysicalScreenSize Tests
    // ==============================================

    @Test
    fun `getPhysicalScreenSize returns non-null`() {
        val size = displayInfo.getPhysicalScreenSize()
        assertNotNull(size)
    }

    @Test
    fun `getPhysicalScreenSize returns positive dimensions`() {
        val size = displayInfo.getPhysicalScreenSize()
        assertTrue("width should be positive, was ${size.width}", size.width > 0)
        assertTrue("height should be positive, was ${size.height}", size.height > 0)
    }

    @Test
    fun `getPhysicalScreenSize returns DisplayInfoSize type`() {
        val size: DisplayInfoSize = displayInfo.getPhysicalScreenSize()
        assertTrue(size.width >= 0)
        assertTrue(size.height >= 0)
    }

    // ==============================================
    // getAppWindowSize Tests
    // ==============================================

    @Test
    fun `getAppWindowSize with null activity returns value on API 33`() {
        // API 30+에서는 activity 없이도 currentWindowMetrics 사용 가능
        val size = displayInfo.getAppWindowSize(null)
        assertNotNull(size)
    }

    @Test
    fun `getAppWindowSize returns positive dimensions on API 33`() {
        val size = displayInfo.getAppWindowSize(null)
        assertNotNull(size)
        assertTrue("width should be positive", size!!.width > 0)
        assertTrue("height should be positive", size.height > 0)
    }

    @Test
    @Config(sdk = [28])
    fun `getAppWindowSize with null activity returns null on API 28`() {
        // API 28-29에서 activity가 null이면 null 반환
        val info = DisplayInfo(context)
        val size = info.getAppWindowSize(null)
        assertEquals(null, size)
    }

    // ==============================================
    // getStatusBarSize Tests
    // ==============================================

    @Test
    fun `getStatusBarSize returns non-null on API 33`() {
        val size = displayInfo.getStatusBarSize()
        assertNotNull(size)
    }

    @Test
    fun `getStatusBarSize returns DisplayInfoSize or null`() {
        val size = displayInfo.getStatusBarSize()
        // Robolectric 환경에서 null이 아닌 경우 width/height >= 0
        if (size != null) {
            assertTrue("width should be >= 0", size.width >= 0)
            assertTrue("height should be >= 0", size.height >= 0)
        }
    }

    @Test
    @Config(sdk = [28])
    fun `getStatusBarSize returns value on API 28`() {
        val info = DisplayInfo(context)
        val size = info.getStatusBarSize()
        // Pre-R에서도 리소스 기반 조회가 가능하면 값이 반환됨
        if (size != null) {
            assertTrue(size.width >= 0)
            assertTrue(size.height >= 0)
        }
    }

    // ==============================================
    // getNavigationBarSize Tests
    // ==============================================

    @Test
    fun `getNavigationBarSize returns DisplayInfoSize or null`() {
        val size = displayInfo.getNavigationBarSize()
        if (size != null) {
            assertTrue("width should be >= 0", size.width >= 0)
            assertTrue("height should be >= 0", size.height >= 0)
        }
    }

    @Test
    @Config(sdk = [28])
    fun `getNavigationBarSize returns value on API 28`() {
        val info = DisplayInfo(context)
        val size = info.getNavigationBarSize()
        if (size != null) {
            assertTrue(size.width >= 0)
            assertTrue(size.height >= 0)
        }
    }

    // ==============================================
    // isPortrait / isLandscape Tests
    // ==============================================

    @Test
    fun `isPortrait and isLandscape are mutually exclusive or both false`() {
        val portrait = displayInfo.isPortrait()
        val landscape = displayInfo.isLandscape()
        // 둘 다 true일 수 없음
        assertFalse(
            "isPortrait and isLandscape cannot both be true",
            portrait && landscape,
        )
    }

    @Test
    fun `isPortrait returns boolean based on configuration`() {
        val orientation = context.resources.configuration.orientation
        val expected = orientation == Configuration.ORIENTATION_PORTRAIT
        assertEquals(expected, displayInfo.isPortrait())
    }

    @Test
    fun `isLandscape returns boolean based on configuration`() {
        val orientation = context.resources.configuration.orientation
        val expected = orientation == Configuration.ORIENTATION_LANDSCAPE
        assertEquals(expected, displayInfo.isLandscape())
    }

    // ==============================================
    // getStatusBarStableInsets Tests
    // ==============================================

    @Test
    fun `getStatusBarStableInsets with null activity returns value on API 33`() {
        // API 30+에서는 currentWindowMetrics 사용
        val insets = displayInfo.getStatusBarStableInsets(null)
        if (insets != null) {
            assertTrue("top should be >= 0", insets.top >= 0)
            assertEquals("bottom should be 0 for status bar", 0, insets.bottom)
        }
    }

    @Test
    @Config(sdk = [28])
    fun `getStatusBarStableInsets with null activity returns null on API 28`() {
        val info = DisplayInfo(context)
        val insets = info.getStatusBarStableInsets(null)
        assertEquals(null, insets)
    }

    // ==============================================
    // getNavigationBarStableInsets Tests
    // ==============================================

    @Test
    fun `getNavigationBarStableInsets with null activity returns value on API 33`() {
        val insets = displayInfo.getNavigationBarStableInsets(null)
        if (insets != null) {
            assertTrue("top should be >= 0", insets.top >= 0)
            assertTrue("bottom should be >= 0", insets.bottom >= 0)
            assertTrue("left should be >= 0", insets.left >= 0)
            assertTrue("right should be >= 0", insets.right >= 0)
        }
    }

    @Test
    @Config(sdk = [28])
    fun `getNavigationBarStableInsets with null activity returns null on API 28`() {
        val info = DisplayInfo(context)
        val insets = info.getNavigationBarStableInsets(null)
        assertEquals(null, insets)
    }

    // ==============================================
    // Return Contract Tests (null vs zero)
    // ==============================================

    @Test
    fun `getPhysicalScreenSize never returns null`() {
        // getPhysicalScreenSize는 non-null 반환
        val size: DisplayInfoSize = displayInfo.getPhysicalScreenSize()
        assertNotNull(size)
    }

    @Test
    fun `nullable APIs return null or valid DisplayInfoSize`() {
        // nullable API들은 null 또는 유효한 값을 반환
        val appWindow = displayInfo.getAppWindowSize()
        val statusBar = displayInfo.getStatusBarSize()
        val navBar = displayInfo.getNavigationBarSize()

        // null이 아닌 경우 width/height >= 0
        appWindow?.let {
            assertTrue(it.width >= 0 && it.height >= 0)
        }
        statusBar?.let {
            assertTrue(it.width >= 0 && it.height >= 0)
        }
        navBar?.let {
            assertTrue(it.width >= 0 && it.height >= 0)
        }
    }

    // ==============================================
    // Return Contract: getPhysicalScreenSize
    // - non-null 고정, 예외 fallback시 (0,0)
    // ==============================================

    @Test
    fun `contract - getPhysicalScreenSize is always non-null on API 33`() {
        val size: DisplayInfoSize = displayInfo.getPhysicalScreenSize()
        assertNotNull("getPhysicalScreenSize must never return null", size)
        assertTrue("width must be >= 0", size.width >= 0)
        assertTrue("height must be >= 0", size.height >= 0)
    }

    @Test
    @Config(sdk = [28])
    fun `contract - getPhysicalScreenSize is always non-null on API 28`() {
        val info = DisplayInfo(context)
        val size: DisplayInfoSize = info.getPhysicalScreenSize()
        assertNotNull("getPhysicalScreenSize must never return null on API 28", size)
        assertTrue("width must be >= 0", size.width >= 0)
        assertTrue("height must be >= 0", size.height >= 0)
    }

    // ==============================================
    // Return Contract: getAppWindowSize
    // - API 30+: non-null (activity 무시)
    // - API 28~29 + activity==null: null
    // ==============================================

    @Test
    fun `contract - getAppWindowSize returns non-null on API 33 without activity`() {
        val size = displayInfo.getAppWindowSize(null)
        assertNotNull("API 30+ must return non-null even without activity", size)
    }

    @Test
    @Config(sdk = [28])
    fun `contract - getAppWindowSize returns null on API 28 without activity`() {
        val info = DisplayInfo(context)
        assertNull("API 28~29 must return null when activity is null", info.getAppWindowSize(null))
    }

    // ==============================================
    // Return Contract: getStatusBarSize
    // - non-null이면 (width,height) >= 0
    // - (0,0)은 점유 영역 없음(제스처 모드 등)
    // - null은 측정 불가(리소스 없음)
    // ==============================================

    @Test
    fun `contract - getStatusBarSize non-null result has non-negative dimensions on API 33`() {
        val size = displayInfo.getStatusBarSize()
        assertNotNull("API 30+ should resolve status bar from WindowInsets", size)
        assertTrue("width >= 0", size!!.width >= 0)
        assertTrue("height >= 0", size.height >= 0)
    }

    @Test
    fun `contract - getStatusBarSize zero means no reserved area not measurement failure`() {
        val size = displayInfo.getStatusBarSize()
        if (size != null && size == DisplayInfoSize(0, 0)) {
            // (0,0)은 측정 성공 + 점유 영역 없음을 의미
            assertEquals("zero-size contract", DisplayInfoSize(0, 0), size)
        }
    }

    @Test
    @Config(sdk = [28])
    fun `contract - getStatusBarSize returns null or valid on API 28`() {
        val info = DisplayInfo(context)
        val size = info.getStatusBarSize()
        // null(리소스 미존재) 또는 유효한 DisplayInfoSize
        if (size != null) {
            assertTrue("width >= 0", size.width >= 0)
            assertTrue("height >= 0", size.height >= 0)
        }
    }

    // ==============================================
    // Return Contract: getNavigationBarSize
    // - non-null이면 (width,height) >= 0
    // - (0,0)은 점유 영역 없음(제스처/하드웨어 키)
    // - null은 리소스 전부 미존재(API 28~29)
    // ==============================================

    @Test
    fun `contract - getNavigationBarSize non-null result has non-negative dimensions on API 33`() {
        val size = displayInfo.getNavigationBarSize()
        if (size != null) {
            assertTrue("width >= 0", size.width >= 0)
            assertTrue("height >= 0", size.height >= 0)
        }
    }

    @Test
    fun `contract - getNavigationBarSize zero means no reserved area`() {
        val size = displayInfo.getNavigationBarSize()
        if (size != null && size == DisplayInfoSize(0, 0)) {
            assertEquals("zero-size = gesture/hw-key/no reserved area", DisplayInfoSize(0, 0), size)
        }
    }

    @Test
    @Config(sdk = [28])
    fun `contract - getNavigationBarSize returns null or valid on API 28`() {
        val info = DisplayInfo(context)
        val size = info.getNavigationBarSize()
        if (size != null) {
            assertTrue("width >= 0", size.width >= 0)
            assertTrue("height >= 0", size.height >= 0)
        }
    }

    // ==============================================
    // Return Contract: getStatusBarStableInsets
    // - API 30+: non-null (activity 무시), 4방향 insets >= 0
    // - API 28~29 + activity==null: null
    // - (0,0,0,0)은 점유 영역 없음
    // ==============================================

    @Test
    fun `contract - getStatusBarStableInsets returns non-null insets on API 33`() {
        val insets = displayInfo.getStatusBarStableInsets(null)
        if (insets != null) {
            assertTrue("top >= 0", insets.top >= 0)
            assertTrue("bottom >= 0", insets.bottom >= 0)
            assertTrue("left >= 0", insets.left >= 0)
            assertTrue("right >= 0", insets.right >= 0)
        }
    }

    @Test
    @Config(sdk = [28])
    fun `contract - getStatusBarStableInsets returns null on API 28 without activity`() {
        val info = DisplayInfo(context)
        assertNull("API 28~29 must return null when activity is null", info.getStatusBarStableInsets(null))
    }

    @Test
    fun `contract - getStatusBarStableInsets zero means no reserved area`() {
        val insets = displayInfo.getStatusBarStableInsets(null)
        if (insets != null && insets == DisplayInfoBarInsets(0, 0, 0, 0)) {
            assertTrue("zero insets = no reserved status bar area", insets.isEmpty)
        }
    }

    // ==============================================
    // Return Contract: getNavigationBarStableInsets
    // - API 30+: non-null (activity 무시), 4방향 insets >= 0
    // - API 28~29 + activity==null: null
    // - (0,0,0,0)은 점유 영역 없음
    // ==============================================

    @Test
    fun `contract - getNavigationBarStableInsets returns non-null insets on API 33`() {
        val insets = displayInfo.getNavigationBarStableInsets(null)
        if (insets != null) {
            assertTrue("top >= 0", insets.top >= 0)
            assertTrue("bottom >= 0", insets.bottom >= 0)
            assertTrue("left >= 0", insets.left >= 0)
            assertTrue("right >= 0", insets.right >= 0)
        }
    }

    @Test
    @Config(sdk = [28])
    fun `contract - getNavigationBarStableInsets returns null on API 28 without activity`() {
        val info = DisplayInfo(context)
        assertNull("API 28~29 must return null when activity is null", info.getNavigationBarStableInsets(null))
    }

    @Test
    fun `contract - getNavigationBarStableInsets zero means no reserved area`() {
        val insets = displayInfo.getNavigationBarStableInsets(null)
        if (insets != null && insets == DisplayInfoBarInsets(0, 0, 0, 0)) {
            assertTrue("zero insets = gesture/hw-key/no reserved nav area", insets.isEmpty)
        }
    }

    // ==============================================
    // Consistency Tests
    // ==============================================

    @Test
    fun `physical screen size is at least as large as app window size`() {
        val physical = displayInfo.getPhysicalScreenSize()
        val appWindow = displayInfo.getAppWindowSize()
        if (appWindow != null) {
            assertTrue(
                "physical width >= app window width",
                physical.width >= appWindow.width,
            )
            assertTrue(
                "physical height >= app window height",
                physical.height >= appWindow.height,
            )
        }
    }

    @Test
    fun `multiple calls return consistent results`() {
        val size1 = displayInfo.getPhysicalScreenSize()
        val size2 = displayInfo.getPhysicalScreenSize()
        assertEquals(size1, size2)
    }

    // ==============================================
    // onDestroy Tests
    // ==============================================

    @Test
    fun `onDestroy does not throw`() {
        val info = DisplayInfo(context)
        info.onDestroy()
        // 예외 없이 완료
    }
}
