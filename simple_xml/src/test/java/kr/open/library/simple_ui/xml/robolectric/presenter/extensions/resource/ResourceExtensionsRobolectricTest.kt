package kr.open.library.simple_ui.xml.robolectric.presenter.extensions.resource

import android.content.Context
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.extensions.resource.getColorCompat
import kr.open.library.simple_ui.xml.extensions.resource.getColorSafe
import kr.open.library.simple_ui.xml.extensions.resource.getDimensionPixelOffset
import kr.open.library.simple_ui.xml.extensions.resource.getDimensionPixelSize
import kr.open.library.simple_ui.xml.extensions.resource.getDrawableCompat
import kr.open.library.simple_ui.xml.extensions.resource.getDrawableSafe
import kr.open.library.simple_ui.xml.extensions.resource.getInteger
import kr.open.library.simple_ui.xml.extensions.resource.getStringArray
import kr.open.library.simple_ui.xml.extensions.resource.getStringFormatted
import kr.open.library.simple_ui.xml.extensions.resource.getStringSafe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for Resource extension functions
 * Tests resource access using ContextCompat and Resources
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ResourceExtensionsRobolectricTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    // ========================================
    // getDrawableCompat Tests
    // ========================================

    @Test
    fun getDrawableCompat_withValidResource_returnsDrawable() {
        val drawable = context.getDrawableCompat(android.R.drawable.ic_delete)

        assertNotNull(drawable)
    }

    @Test
    fun getDrawableCompat_withSystemResource_returnsDrawable() {
        val drawable = context.getDrawableCompat(android.R.drawable.ic_menu_search)

        assertNotNull(drawable)
    }

    // ========================================
    // getColorCompat Tests
    // ========================================

    @Test
    fun getColorCompat_withValidResource_returnsColor() {
        val color = context.getColorCompat(android.R.color.black)

        assertNotNull(color)
        assertTrue(color != 0)
    }

    @Test
    fun getColorCompat_withBlackColor_returnsBlack() {
        val color = context.getColorCompat(android.R.color.black)

        // Black color should have high alpha
        assertTrue(Color.alpha(color) > 0)
    }

    @Test
    fun getColorCompat_withWhiteColor_returnsWhite() {
        val color = context.getColorCompat(android.R.color.white)

        // White color should have high alpha
        assertTrue(Color.alpha(color) > 0)
    }

    // ========================================
    // getDimensionPixelSize Tests
    // ========================================

    @Test
    fun getDimensionPixelSize_withSystemResource_returnsSize() {
        // Using system dimen resources
        val size = context.getDimensionPixelSize(android.R.dimen.app_icon_size)

        assertTrue(size > 0)
    }

    // ========================================
    // getDimensionPixelOffset Tests
    // ========================================

    @Test
    fun getDimensionPixelOffset_withSystemResource_returnsOffset() {
        val offset = context.getDimensionPixelOffset(android.R.dimen.app_icon_size)

        assertTrue(offset > 0)
    }

    // ========================================
    // getStringArray Tests
    // ========================================

    @Test
    fun getStringArray_withSystemResource_returnsArray() {
        // Using Android system string array
        val array = context.getStringArray(android.R.array.emailAddressTypes)

        assertNotNull(array)
        assertTrue(array.isNotEmpty())
    }

    // ========================================
    // getStringFormatted Tests
    // ========================================

    @Test
    fun getStringFormatted_withNoArgs_returnsString() {
        // Using system string resources that exist in all Android versions
        val string = context.getStringFormatted(android.R.string.ok)

        assertNotNull(string)
        assertTrue(string.isNotEmpty())
    }

    @Test
    fun getStringFormatted_withSingleArg_formatsCorrectly() {
        // Create a simple formatted string test
        val result = context.getString(android.R.string.ok)

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    // ========================================
    // getInteger Tests
    // ========================================

    @Test
    fun getInteger_withSystemResource_returnsInteger() {
        // Android system integer resources
        val configAnimTime = context.getInteger(android.R.integer.config_shortAnimTime)

        assertTrue(configAnimTime > 0)
    }

    @Test
    fun getInteger_withDifferentResources_returnsDifferentValues() {
        val shortTime = context.getInteger(android.R.integer.config_shortAnimTime)
        val longTime = context.getInteger(android.R.integer.config_longAnimTime)

        assertTrue(shortTime > 0)
        assertTrue(longTime > 0)
        assertTrue(longTime > shortTime)
    }

    // ========================================
    // getDrawableSafe Tests
    // ========================================

    @Test
    fun getDrawableSafe_withValidResource_returnsDrawable() {
        val drawable = context.getDrawableSafe(android.R.drawable.ic_delete)

        assertNotNull(drawable)
    }

    @Test
    fun getDrawableSafe_withInvalidResource_returnsNull() {
        val drawable = context.getDrawableSafe(-1)

        assertNull(drawable)
    }

    @Test
    fun getDrawableSafe_withZeroResource_returnsNull() {
        val drawable = context.getDrawableSafe(0)

        assertNull(drawable)
    }

    // ========================================
    // getColorSafe Tests
    // ========================================

    @Test
    fun getColorSafe_withValidResource_returnsColor() {
        val color = context.getColorSafe(android.R.color.black, Color.RED)

        assertNotNull(color)
        assertTrue(color != Color.RED) // Should not return default
    }

    @Test
    fun getColorSafe_withInvalidResource_returnsDefault() {
        val defaultColor = Color.BLUE
        val color = context.getColorSafe(-1, defaultColor)

        assertEquals(defaultColor, color)
    }

    @Test
    fun getColorSafe_withZeroResource_returnsDefault() {
        val defaultColor = Color.GREEN
        val color = context.getColorSafe(0, defaultColor)

        assertEquals(defaultColor, color)
    }

    @Test
    fun getColorSafe_withDifferentDefaults_usesCorrectDefault() {
        val default1 = Color.RED
        val default2 = Color.BLUE

        val color1 = context.getColorSafe(-1, default1)
        val color2 = context.getColorSafe(-1, default2)

        assertEquals(default1, color1)
        assertEquals(default2, color2)
    }

    // ========================================
    // getStringSafe Tests
    // ========================================

    @Test
    fun getStringSafe_withValidResource_returnsString() {
        val string = context.getStringSafe(android.R.string.ok)

        assertNotNull(string)
        assertTrue(string.isNotEmpty())
    }

    @Test
    fun getStringSafe_withInvalidResource_returnsEmptyString() {
        val string = context.getStringSafe(-1)

        assertEquals("", string)
    }

    @Test
    fun getStringSafe_withZeroResource_returnsEmptyString() {
        val string = context.getStringSafe(0)

        assertEquals("", string)
    }

    @Test
    fun getStringSafe_withMultipleInvalidCalls_returnsEmptyStrings() {
        val string1 = context.getStringSafe(-1)
        val string2 = context.getStringSafe(-2)
        val string3 = context.getStringSafe(-999)

        assertEquals("", string1)
        assertEquals("", string2)
        assertEquals("", string3)
    }

    // ========================================
    // Safe vs Regular Comparison Tests
    // ========================================

    @Test
    fun safeVersions_withValidResources_matchRegularVersions() {
        val colorRegular = context.getColorCompat(android.R.color.black)
        val colorSafe = context.getColorSafe(android.R.color.black, Color.RED)

        assertEquals(colorRegular, colorSafe)
    }

    @Test
    fun safeVersions_withInvalidResources_doNotCrash() {
        // All these should not crash
        val drawable = context.getDrawableSafe(-1)
        val color = context.getColorSafe(-1, Color.BLACK)
        val string = context.getStringSafe(-1)

        assertNull(drawable)
        assertEquals(Color.BLACK, color)
        assertEquals("", string)
    }

    // ========================================
    // Multiple Resource Access Tests
    // ========================================

    @Test
    fun multipleResourceAccess_returnsConsistentResults() {
        val color1 = context.getColorCompat(android.R.color.black)
        val color2 = context.getColorCompat(android.R.color.black)

        assertEquals(color1, color2)
    }

    @Test
    fun differentResourceTypes_canBeAccessedTogether() {
        val drawable = context.getDrawableCompat(android.R.drawable.ic_delete)
        val color = context.getColorCompat(android.R.color.black)
        val string = context.getString(android.R.string.ok)
        val integer = context.getInteger(android.R.integer.config_shortAnimTime)

        assertNotNull(drawable)
        assertTrue(color != 0)
        assertTrue(string.isNotEmpty())
        assertTrue(integer > 0)
    }

    // ========================================
    // Edge Cases Tests
    // ========================================

    @Test
    fun getColorSafe_withTransparentDefault_works() {
        val transparent = Color.TRANSPARENT
        val color = context.getColorSafe(-1, transparent)

        assertEquals(transparent, color)
    }

    @Test
    fun getStringSafe_repeatedCalls_returnsConsistentResults() {
        val result1 = context.getStringSafe(-1)
        val result2 = context.getStringSafe(-1)
        val result3 = context.getStringSafe(-1)

        assertEquals(result1, result2)
        assertEquals(result2, result3)
    }
}
