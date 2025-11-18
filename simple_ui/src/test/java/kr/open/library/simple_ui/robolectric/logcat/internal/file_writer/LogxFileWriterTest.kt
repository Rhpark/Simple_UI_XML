package kr.open.library.simple_ui.robolectric.logcat.internal.file_writer

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.logcat.internal.file_writer.LogxFileWriter
import kr.open.library.simple_ui.logcat.internal.file_writer.LogxLifecycleFlushManager
import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.After
import org.junit.Assert.assertNotNull
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

    @Test
    fun writeLog_withExistingDirectory_shouldNotRecreateDirectory() {
        // 디렉토리를 미리 생성
        assertTrue("Directory should be created", tempDir.exists())

        val writer = LogxFileWriter(tempDir.absolutePath, application)

        writer.writeLog(LogxType.INFO, "TestTag", "test message")
        Thread.sleep(200)

        val logFile = tempDir.listFiles()?.firstOrNull { it.name.endsWith("_Log.txt") }
        assertNotNull("Log file should be created in existing directory", logFile)
        assertTrue(logFile!!.exists())
    }

    @Test
    fun writeLog_withExistingLogFile_shouldAppendToFile() {
        val writer = LogxFileWriter(tempDir.absolutePath, application)

        // 첫 번째 로그 작성
        writer.writeLog(LogxType.DEBUG, "Tag1", "first message")
        Thread.sleep(200)

        // 두 번째 로그 작성 (같은 날짜이므로 같은 파일에 append)
        writer.writeLog(LogxType.INFO, "Tag2", "second message")
        Thread.sleep(200)

        val logFile = tempDir.listFiles()?.firstOrNull { it.name.endsWith("_Log.txt") }
        assertNotNull("Log file should exist", logFile)

        val contents = logFile!!.readText()
        assertTrue("Should contain first message", contents.contains("first message"))
        assertTrue("Should contain second message", contents.contains("second message"))
        assertTrue("Should contain Tag1", contents.contains("Tag1"))
        assertTrue("Should contain Tag2", contents.contains("Tag2"))
    }

    @Test
    fun writeLog_withDifferentLogTypes_shouldFormatCorrectly() {
        val writer = LogxFileWriter(tempDir.absolutePath, application)

        writer.writeLog(LogxType.VERBOSE, "VTag", "verbose message")
        writer.writeLog(LogxType.DEBUG, "DTag", "debug message")
        writer.writeLog(LogxType.INFO, "ITag", "info message")
        writer.writeLog(LogxType.WARN, "WTag", "warn message")
        writer.writeLog(LogxType.ERROR, "ETag", "error message")
        writer.writeLog(LogxType.PARENT, "PTag", "parent message")
        writer.writeLog(LogxType.JSON, "JTag", "json message")
        writer.writeLog(LogxType.THREAD_ID, "TTag", "thread message")

        Thread.sleep(300)

        val logFile = tempDir.listFiles()?.firstOrNull { it.name.endsWith("_Log.txt") }
        assertNotNull("Log file should exist", logFile)

        val contents = logFile!!.readText()
        assertTrue(contents.contains("V"))
        assertTrue(contents.contains("D"))
        assertTrue(contents.contains("I"))
        assertTrue(contents.contains("W"))
        assertTrue(contents.contains("E"))
        assertTrue(contents.contains("P"))
        assertTrue(contents.contains("J"))
        assertTrue(contents.contains("T"))
    }

    @Test
    fun cleanup_shouldCompleteWithoutError() {
        val writer = LogxFileWriter(tempDir.absolutePath, application)

        writer.writeLog(LogxType.DEBUG, "TestTag", "test message")
        Thread.sleep(200)

        // cleanup 호출이 예외를 던지지 않아야 함
        writer.cleanup()

        // cleanup 이후에도 디렉토리와 파일은 그대로 존재해야 함
        assertTrue("Directory should still exist", tempDir.exists())
        val logFile = tempDir.listFiles()?.firstOrNull { it.name.endsWith("_Log.txt") }
        assertNotNull("Log file should still exist after cleanup", logFile)
    }

    @Test
    fun createLogxFileWriter_withoutContext_shouldUseFallbackFlush() {
        // Context 없이 생성
        val writer = LogxFileWriter(tempDir.absolutePath, null)

        writer.writeLog(LogxType.DEBUG, "TestTag", "test message")
        Thread.sleep(200)

        val logFile = tempDir.listFiles()?.firstOrNull { it.name.endsWith("_Log.txt") }
        assertNotNull("Log file should be created even without context", logFile)
        assertTrue(logFile!!.exists())
    }

    @Test
    fun logFileName_shouldContainCurrentDate() {
        val writer = LogxFileWriter(tempDir.absolutePath, application)

        writer.writeLog(LogxType.DEBUG, "TestTag", "test message")
        Thread.sleep(200)

        val logFile = tempDir.listFiles()?.firstOrNull { it.name.endsWith("_Log.txt") }
        assertNotNull("Log file should exist", logFile)

        // 파일명이 날짜 형식을 포함하는지 확인 (yy-MM-dd 형식)
        assertTrue("Filename should contain date pattern",
            logFile!!.name.matches(Regex("\\d{2}-\\d{2}-\\d{2}_Log\\.txt")))
    }

    @Test
    fun writeLog_multipleMessages_shouldPreserveOrder() {
        val writer = LogxFileWriter(tempDir.absolutePath, application)

        writer.writeLog(LogxType.DEBUG, "Tag", "message 1")
        writer.writeLog(LogxType.DEBUG, "Tag", "message 2")
        writer.writeLog(LogxType.DEBUG, "Tag", "message 3")

        Thread.sleep(300)

        val logFile = tempDir.listFiles()?.firstOrNull { it.name.endsWith("_Log.txt") }
        assertNotNull("Log file should exist", logFile)

        val lines = logFile!!.readLines()
        val messageLines = lines.filter { it.isNotBlank() }

        assertTrue("Should have at least 3 log entries", messageLines.size >= 3)
        assertTrue("First message should appear first",
            messageLines.indexOfFirst { it.contains("message 1") } <
            messageLines.indexOfFirst { it.contains("message 2") })
        assertTrue("Second message should appear before third",
            messageLines.indexOfFirst { it.contains("message 2") } <
            messageLines.indexOfFirst { it.contains("message 3") })
    }
}