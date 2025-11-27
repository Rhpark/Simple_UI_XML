package kr.open.library.simple_ui.core.unit.logcat.config

import android.os.Build
import kr.open.library.simple_ui.core.logcat.config.LogxConfig
import kr.open.library.simple_ui.core.logcat.config.LogxConfigManager
import kr.open.library.simple_ui.core.logcat.model.LogxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.EnumSet

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class LogxConfigManagerTest {

    private lateinit var manager: LogxConfigManager

    @Before
    fun setUp() {
        manager = LogxConfigManager()
    }

    @Test
    fun `listeners continue notification even when one fails`() {
        var notified = false
        val failing = object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                throw IllegalStateException("boom")
            }
        }
        val working = object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                notified = true
            }
        }

        manager.addConfigChangeListener(failing)
        manager.addConfigChangeListener(working)

        manager.setAppName("TestApp")

        assertEquals("TestApp", manager.config.appName)
        assertTrue("Other listeners should still be notified", notified)
    }

    @Test
    fun `removeAllConfigChangeListener clears registry`() {
        var invoked = false
        val listener = object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                invoked = true
            }
        }

        manager.addConfigChangeListener(listener)
        manager.removeAllConfigChangeListener()

        manager.setDebugMode(true)

        assertFalse("Listener should not be invoked after removal", invoked)
    }

    @Test
    fun `setters update config snapshots`() {
        manager.setDebugMode(true)
        manager.setDebugFilter(true)
        manager.setSaveToFile(true)
        manager.setFilePath("/tmp/logs")
        manager.setAppName("NewApp")
        manager.setDebugLogTypeList(EnumSet.of(LogxType.DEBUG, LogxType.ERROR))
        manager.setDebugFilterList(listOf("tag1", "tag1", "tag2"))

        val config = manager.config
        assertTrue(config.isDebug)
        assertTrue(config.isDebugFilter)
        assertTrue(config.isDebugSave)
        assertEquals("/tmp/logs", config.saveFilePath)
        assertEquals("NewApp", config.appName)
        assertEquals(EnumSet.of(LogxType.DEBUG, LogxType.ERROR), config.debugLogTypeList)
        assertEquals(setOf("tag1", "tag2"), config.debugFilterList)
    }

    @Test
    fun `removeConfigChangeListener removes specific listener`() {
        var firstCalled = false
        var secondCalled = false
        val first = object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                firstCalled = true
            }
        }
        val second = object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                secondCalled = true
            }
        }

        manager.addConfigChangeListener(first)
        manager.addConfigChangeListener(second)
        manager.removeConfigChangeListener(first)

        manager.setAppName("OnlySecond")

        assertFalse(firstCalled)
        assertTrue(secondCalled)
    }

    @Test
    fun `private notifyListeners still informs listeners`() {
        var notified = false
        val succeed = object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                notified = true
            }
        }
        val failing = object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                throw IllegalStateException("should be caught")
            }
        }

        manager.addConfigChangeListener(failing)
        manager.addConfigChangeListener(succeed)

        val method = LogxConfigManager::class.java.getDeclaredMethod("notifyListeners", LogxConfig::class.java)
        method.isAccessible = true
        method.invoke(manager, manager.config.copy(appName = "Direct"))

        assertTrue(notified)
    }

    @Test
    fun `lock write branch executes when already holding lock`() {
        val lockField = LogxConfigManager::class.java.getDeclaredField("lock").apply { isAccessible = true }
        val lock = lockField.get(manager) as java.util.concurrent.locks.ReentrantReadWriteLock

        val listener = object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) = Unit
        }

        lock.writeLock().lock()
        try {
            manager.addConfigChangeListener(listener)
            manager.removeConfigChangeListener(listener)
            manager.addConfigChangeListener(listener)
            manager.removeAllConfigChangeListener()
            manager.updateConfig(manager.config.copy(appName = "Reentrant"))
        } finally {
            lock.writeLock().unlock()
        }
    }
}