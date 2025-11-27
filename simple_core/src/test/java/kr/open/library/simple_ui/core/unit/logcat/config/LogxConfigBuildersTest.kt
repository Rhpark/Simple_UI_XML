package kr.open.library.simple_ui.core.unit.logcat.config

import kr.open.library.simple_ui.core.logcat.config.LogxFileConfigBuilder
import kr.open.library.simple_ui.core.logcat.config.LogxFilterConfigBuilder
import kr.open.library.simple_ui.core.logcat.config.LogxTypeConfigBuilder
import kr.open.library.simple_ui.core.logcat.model.LogxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.util.EnumSet

class LogxConfigBuildersTest {

    @Test
    fun fileConfigBuilder_appliesOverrides() {
        val builder = LogxFileConfigBuilder().apply {
            saveToFile = true
            filePath = "/tmp/logs"
        }

        assertTrue(builder.saveToFile)
        assertEquals("/tmp/logs", builder.filePath)
    }

    @Test
    fun logTypeConfigBuilder_supportsOperations() {
        val builder = LogxTypeConfigBuilder().apply {
            -LogxType.JSON
            basic() // re-adds standard types
            extended() // re-adds extended types
            -LogxType.THREAD_ID
        }

        val types = builder.types
        assertTrue(types.containsAll(listOf(LogxType.VERBOSE, LogxType.DEBUG, LogxType.INFO, LogxType.WARN, LogxType.ERROR, LogxType.JSON)))
        assertFalse(types.contains(LogxType.THREAD_ID))

        builder.apply {
            +LogxType.THREAD_ID
        }
        assertTrue(builder.types.contains(LogxType.THREAD_ID))
    }

    @Test
    fun logTypeConfigBuilder_allAddsEveryType() {
        val builder = LogxTypeConfigBuilder().apply {
            LogxType.entries.forEach { -it }
            all()
        }

        assertEquals(EnumSet.copyOf(LogxType.entries), builder.types)
    }

    @Test
    fun filterConfigBuilder_managesFilters() {
        val builder = LogxFilterConfigBuilder().apply {
            +"Tag1"
            +"Tag2"
            -"Tag1"
            addAll("Tag3", "Tag4")
        }

        assertEquals(setOf("Tag2", "Tag3", "Tag4"), builder.filters)

        builder.clear()
        assertEquals(emptySet<String>(), builder.filters)
    }
}
