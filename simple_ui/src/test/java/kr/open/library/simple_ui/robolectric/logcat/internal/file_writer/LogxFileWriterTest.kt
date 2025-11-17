package kr.open.library.simple_ui.robolectric.logcat.internal.file_writer

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.delay
import kr.open.library.simple_ui.logcat.internal.file_writer.LogxFileWriter
import kr.open.library.simple_ui.logcat.internal.file_writer.LogxLifecycleFlushManager
import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class LogxFileWriterTest {

    private lateinit var application: Application
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        tempDir = createTempDir(prefix = "logxWriter").apply {
            deleteOnExit()
        }
        LogxLifecycleFlushManager.getInstance().forceCleanup()
    }

    @After
    fun tearDown() {
        LogxLifecycleFlushManager.getInstance().forceCleanup()
        tempDir.deleteRecursively()
    }

    @Test
    fun writeLogCreatesFileWithFormattedEntry() {
        val writer = LogxFileWriter(tempDir.absolutePath, application)

        writer.writeLog(LogxType.DEBUG, "TestTag", "hello world")

        Thread.sleep(200) // allow IO coroutine to flush

        val logFile = tempDir.listFiles()?.firstOrNull { it.name.endsWith("_Log.txt") }
        assertTrue("Log file should be created", logFile != null && logFile.exists())
        val contents = logFile!!.readText()
        assertTrue(contents.contains("TestTag"))
        assertTrue(contents.contains("hello world"))
    }
}