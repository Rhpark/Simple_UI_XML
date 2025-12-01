package kr.open.library.simple_ui.core.robolectric.logcat.internal.file_writer

import android.app.Application
import android.content.ComponentCallbacks2
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kr.open.library.simple_ui.core.logcat.internal.file_writer.LogxLifecycleFlushManager
import org.junit.After
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class LogxLifecycleFlushManagerTest {
    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        LogxLifecycleFlushManager.getInstance().forceCleanup()
    }

    @After
    fun tearDown() {
        LogxLifecycleFlushManager.getInstance().forceCleanup()
    }

    @Test
    fun manualFlushCancelsLogScope() {
        val job = Job()
        val scope = CoroutineScope(job)
        val manager = LogxLifecycleFlushManager.getInstance()

        manager.initialize(application, scope)
        manager.manualFlush("TEST")

        assertTrue(job.isCancelled)
    }

    @Test
    fun trimMemoryTriggersFlush() {
        val job = Job()
        val scope = CoroutineScope(job)
        val manager = LogxLifecycleFlushManager.getInstance()

        manager.initialize(application, scope)
        manager.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND)

        assertTrue(job.isCancelled)
    }

    @Test
    fun forceCleanupResetsSingleton() {
        val first = LogxLifecycleFlushManager.getInstance()
        first.forceCleanup()
        val second = LogxLifecycleFlushManager.getInstance()

        assertNotSame(first, second)
    }
}
