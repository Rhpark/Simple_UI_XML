package kr.open.library.simple_ui.core.unit.logcat.internal.filter

import kr.open.library.simple_ui.core.logcat.config.LogxConfig
import kr.open.library.simple_ui.core.logcat.internal.filter.DefaultLogFilter
import kr.open.library.simple_ui.core.logcat.model.LogxType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.EnumSet

class DefaultLogFilterTest {
    @Test
    fun `filter allows any tag when debug filter disabled`() {
        val config =
            LogxConfig(
                isDebugFilter = false,
                debugLogTypeList = EnumSet.allOf(LogxType::class.java),
            )
        val filter = DefaultLogFilter(config)

        assertTrue(filter.shouldLog("RandomTag", "SomeFile"))
    }

    @Test
    fun `filter checks tag and file names when debug filter enabled`() {
        val config =
            LogxConfig(
                isDebugFilter = true,
                debugFilterList = setOf("AllowedTag", "AllowedFile"),
                debugLogTypeList = EnumSet.allOf(LogxType::class.java),
            )
        val filter = DefaultLogFilter(config)

        assertTrue(filter.shouldLog("AllowedTag", "OtherFile"))
        assertTrue(filter.shouldLog("DifferentTag", "AllowedFile"))
        assertFalse(filter.shouldLog("DifferentTag", "OtherFile"))
    }
}
