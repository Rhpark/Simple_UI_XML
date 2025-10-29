package kr.open.library.simple_ui.logcat.config

import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.util.EnumSet
@Ignore("임시로 비활성화")
class LogxConfigDslTest {

    @Test
    fun logxConfig_buildsConfigWithCustomBlocks() {
        val config = logxConfig {
            debugMode = false
            debugFilter = true
            appName = "TestApp"
            fileConfig {
                saveToFile = true
                filePath = "/custom/path"
            }
            logTypes {
                LogxType.values().forEach { -it }
                +LogxType.ERROR
            }
            filters {
                +"TagA"
                +"TagB"
            }
        }

        assertFalse(config.isDebug)
        assertTrue(config.isDebugFilter)
        assertTrue(config.isDebugSave)
        assertEquals("TestApp", config.appName)
        assertEquals("/custom/path", config.saveFilePath)
        assertEquals(EnumSet.of(LogxType.ERROR), config.debugLogTypeList)
        assertEquals(setOf("TagA", "TagB"), config.debugFilterList)
    }
}
