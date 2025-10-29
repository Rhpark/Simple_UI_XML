package kr.open.library.simple_ui.logcat.internal.filter

import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.util.EnumSet

class DefaultLogFilterTest {

    @Test
    fun filter_allowsEverythingWhenFilterDisabled() {
        val config = LogxConfig(
            isDebug = true,
            isDebugFilter = false,
            debugLogTypeList = EnumSet.allOf(LogxType::class.java)
        )
        val filter = DefaultLogFilter(config)

        assertTrue(filter.shouldLog("AnyTag", "AnyFile"))
    }

    @Test
    fun filter_respectsTagAndFileFiltersWhenEnabled() {
        val config = LogxConfig(
            isDebug = true,
            isDebugFilter = true,
            debugFilterList = setOf("AllowedTag", "AllowedFile"),
            debugLogTypeList = EnumSet.allOf(LogxType::class.java)
        )
        val filter = DefaultLogFilter(config)

        assertTrue(filter.shouldLog("AllowedTag", "OtherFile"))
        assertTrue(filter.shouldLog("OtherTag", "AllowedFile"))
        assertFalse(filter.shouldLog("OtherTag", "OtherFile"))
    }

    // ========== Extended Tests ==========

    @Test
    fun filter_blocksEverythingWhenFilterListEmpty() {
        val config = LogxConfig(
            isDebug = true,
            isDebugFilter = true,
            debugFilterList = emptySet(),
            debugLogTypeList = EnumSet.allOf(LogxType::class.java)
        )
        val filter = DefaultLogFilter(config)

        assertFalse(filter.shouldLog("SomeTag", "SomeFile"))
        assertFalse(filter.shouldLog("", ""))
    }

    @Test
    fun filter_allowsWhenOnlyTagMatches() {
        val config = LogxConfig(
            isDebug = true,
            isDebugFilter = true,
            debugFilterList = setOf("MainActivity"),
            debugLogTypeList = EnumSet.allOf(LogxType::class.java)
        )
        val filter = DefaultLogFilter(config)

        assertTrue(filter.shouldLog("MainActivity", "SomeOtherFile.kt"))
    }

    @Test
    fun filter_allowsWhenOnlyFileNameMatches() {
        val config = LogxConfig(
            isDebug = true,
            isDebugFilter = true,
            debugFilterList = setOf("NetworkManager.kt"),
            debugLogTypeList = EnumSet.allOf(LogxType::class.java)
        )
        val filter = DefaultLogFilter(config)

        assertTrue(filter.shouldLog("SomeTag", "NetworkManager.kt"))
    }

    @Test
    fun filter_allowsWhenBothTagAndFileNameMatch() {
        val config = LogxConfig(
            isDebug = true,
            isDebugFilter = true,
            debugFilterList = setOf("MyTag", "MyFile"),
            debugLogTypeList = EnumSet.allOf(LogxType::class.java)
        )
        val filter = DefaultLogFilter(config)

        assertTrue(filter.shouldLog("MyTag", "MyFile"))
    }

    @Test
    fun filter_isCaseSensitive() {
        val config = LogxConfig(
            isDebug = true,
            isDebugFilter = true,
            debugFilterList = setOf("MyTag"),
            debugLogTypeList = EnumSet.allOf(LogxType::class.java)
        )
        val filter = DefaultLogFilter(config)

        assertTrue(filter.shouldLog("MyTag", "file"))
        assertFalse(filter.shouldLog("mytag", "file"))
        assertFalse(filter.shouldLog("MYTAG", "file"))
    }

    @Test
    fun filter_handlesSpecialCharacters() {
        val config = LogxConfig(
            isDebug = true,
            isDebugFilter = true,
            debugFilterList = setOf("My-Tag_123", "File\$Name.kt"),
            debugLogTypeList = EnumSet.allOf(LogxType::class.java)
        )
        val filter = DefaultLogFilter(config)

        assertTrue(filter.shouldLog("My-Tag_123", "other"))
        assertTrue(filter.shouldLog("other", "File\$Name.kt"))
        assertFalse(filter.shouldLog("My-Tag", "File\$Name"))
    }

    @Test
    fun filter_handlesEmptyStrings() {
        val config = LogxConfig(
            isDebug = true,
            isDebugFilter = true,
            debugFilterList = setOf("ValidTag"),
            debugLogTypeList = EnumSet.allOf(LogxType::class.java)
        )
        val filter = DefaultLogFilter(config)

        assertFalse(filter.shouldLog("", ""))
        assertFalse(filter.shouldLog("", "SomeFile"))
        assertFalse(filter.shouldLog("SomeTag", ""))
    }

    @Test
    fun filter_allowsEmptyStringIfInFilterList() {
        val config = LogxConfig(
            isDebug = true,
            isDebugFilter = true,
            debugFilterList = setOf(""),
            debugLogTypeList = EnumSet.allOf(LogxType::class.java)
        )
        val filter = DefaultLogFilter(config)

        assertTrue(filter.shouldLog("", "other"))
        assertTrue(filter.shouldLog("other", ""))
    }

    @Test
    fun filter_worksWithMultipleAllowedValues() {
        val config = LogxConfig(
            isDebug = true,
            isDebugFilter = true,
            debugFilterList = setOf("Tag1", "Tag2", "Tag3", "File1", "File2"),
            debugLogTypeList = EnumSet.allOf(LogxType::class.java)
        )
        val filter = DefaultLogFilter(config)

        assertTrue(filter.shouldLog("Tag1", "unknown"))
        assertTrue(filter.shouldLog("Tag2", "unknown"))
        assertTrue(filter.shouldLog("Tag3", "unknown"))
        assertTrue(filter.shouldLog("unknown", "File1"))
        assertTrue(filter.shouldLog("unknown", "File2"))
        assertFalse(filter.shouldLog("Tag4", "File3"))
    }
}
