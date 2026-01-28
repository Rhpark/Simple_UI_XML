package kr.open.library.simple_ui.core.unit.logcat.config

import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigStore
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LogxConfigStoreTest {
    @Before
    fun setUp() {
        LogxConfigStore.setLogging(true)
        LogxConfigStore.setLogTypes(enumValues<LogType>().toSet())
        LogxConfigStore.setLogTagBlockListEnabled(false)
        LogxConfigStore.setLogTagBlockList(emptySet())
        LogxConfigStore.setSaveEnabled(false)
        LogxConfigStore.setStorageType(LogStorageType.APP_EXTERNAL)
        LogxConfigStore.setSaveDirectory(null)
        LogxConfigStore.setAppName("AppName")
    }

    @Test
    fun defaultsAreApplied() {
        assertTrue(LogxConfigStore.isLogging())
        assertEquals(enumValues<LogType>().toSet(), LogxConfigStore.getLogTypes())
        assertFalse(LogxConfigStore.isLogTagBlockListEnabled())
        assertTrue(LogxConfigStore.getLogTagBlockList().isEmpty())
        assertFalse(LogxConfigStore.isSaveEnabled())
        assertEquals(LogStorageType.APP_EXTERNAL, LogxConfigStore.getStorageType())
        assertNull(LogxConfigStore.getSaveDirectory())
        assertEquals("AppName", LogxConfigStore.getAppName())

        val skipPackages = LogxConfigStore.getSkipPackages()
        assertTrue(skipPackages.contains("kr.open.library.simple_ui.core.logcat"))
        assertTrue(skipPackages.contains("java."))
    }

    @Test
    fun snapshotReflectsLatestValues() {
        LogxConfigStore.setLogging(false)
        LogxConfigStore.setLogTypes(setOf(LogType.ERROR))
        LogxConfigStore.setLogTagBlockListEnabled(true)
        LogxConfigStore.setLogTagBlockList(setOf("BLOCK"))
        LogxConfigStore.setSaveEnabled(true)
        LogxConfigStore.setStorageType(LogStorageType.INTERNAL)
        LogxConfigStore.setSaveDirectory("C:/tmp")
        LogxConfigStore.setAppName("TestApp")

        val baseSkip = LogxConfigStore.getSkipPackages()
        LogxConfigStore.addSkipPackages(setOf("com.example."))

        val snapshot = LogxConfigStore.snapshot()
        assertFalse(snapshot.isLogging)
        assertEquals(setOf(LogType.ERROR), snapshot.logTypes)
        assertTrue(snapshot.isLogTagBlockListEnabled)
        assertEquals(setOf("BLOCK"), snapshot.logTagBlockList)
        assertTrue(snapshot.isSaveEnabled)
        assertEquals(LogStorageType.INTERNAL, snapshot.storageType)
        assertEquals("C:/tmp", snapshot.saveDirectory)
        assertEquals("TestApp", snapshot.appName)
        assertTrue(snapshot.skipPackages.containsAll(baseSkip))
        assertTrue(snapshot.skipPackages.contains("com.example."))
    }

    @Test
    fun addSkipPackagesIsAdditive() {
        val before = LogxConfigStore.getSkipPackages()
        LogxConfigStore.addSkipPackages(setOf("com.example.one", "com.example.two"))
        val after = LogxConfigStore.getSkipPackages()

        assertTrue(after.containsAll(before))
        assertTrue(after.contains("com.example.one"))
        assertTrue(after.contains("com.example.two"))
    }

    @Test
    fun addSkipPackagesIgnoresBlankValues() {
        val before = LogxConfigStore.getSkipPackages()
        LogxConfigStore.addSkipPackages(setOf("", "   ", "com.example.valid"))
        val after = LogxConfigStore.getSkipPackages()

        assertTrue(after.containsAll(before))
        assertTrue(after.contains("com.example.valid"))
        assertFalse(after.contains(""))
        assertFalse(after.contains("   "))
    }
}
