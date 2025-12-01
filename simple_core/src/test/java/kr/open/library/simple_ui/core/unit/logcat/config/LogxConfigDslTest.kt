package kr.open.library.simple_ui.core.unit.logcat.config

import kr.open.library.simple_ui.core.logcat.config.LogxConfig
import kr.open.library.simple_ui.core.logcat.config.LogxPathUtils
import kr.open.library.simple_ui.core.logcat.config.LogxStorageType
import kr.open.library.simple_ui.core.logcat.config.logxConfig
import kr.open.library.simple_ui.core.logcat.model.LogxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.EnumSet

class LogxConfigDslTest {
    @Test
    fun `logxConfig keeps default values when no blocks provided`() {
        val config = logxConfig { }

        assertTrue(config.isDebug)
        assertFalse(config.isDebugFilter)
        assertFalse(config.isDebugSave)
        assertEquals("RhPark", config.appName)
        assertEquals(LogxPathUtils.getDefaultLogPath(), config.saveFilePath)
        assertEquals(emptySet<String>(), config.debugFilterList)
        assertEquals(EnumSet.allOf(LogxType::class.java), config.debugLogTypeList)
    }

    @Test
    fun `logxConfig applies nested builder blocks`() {
        val customPath = (System.getProperty("java.io.tmpdir") ?: error("tmp dir missing")) + "/logx-dsl"

        val config =
            logxConfig {
                debugMode = false
                debugFilter = true
                appName = "TestApp"
                fileConfig {
                    saveToFile = true
                    filePath = customPath
                }
                logTypes {
                    LogxType.entries.forEach { -it }
                    +LogxType.INFO
                    +LogxType.ERROR
                }
                filters {
                    +"ImportantTag"
                    +"FileName"
                    -"FileName"
                }
            }

        assertFalse(config.isDebug)
        assertTrue(config.isDebugFilter)
        assertTrue(config.isDebugSave)
        assertEquals(customPath, config.saveFilePath)
        assertEquals("TestApp", config.appName)
        assertEquals(setOf("ImportantTag"), config.debugFilterList)
        assertEquals(EnumSet.of(LogxType.INFO, LogxType.ERROR), config.debugLogTypeList)
    }

    @Test
    fun `createFallback uses internal storage defaults`() {
        val config = LogxConfig.createFallback()

        assertFalse(config.isDebugSave)
        assertEquals(LogxStorageType.INTERNAL, config.storageType)
        assertEquals(LogxPathUtils.getDefaultLogPath(), config.saveFilePath)
    }
}
