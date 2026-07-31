package kr.open.library.simple_ui.core.robolectric.logcat.internal.writer

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigSnapshot
import kr.open.library.simple_ui.core.logcat.internal.writer.LogxFileWriter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.io.File

/**
 * Verifies asynchronous file writes, close/reopen behavior, and failure reporting.<br><br>
 * 비동기 파일 기록·종료 후 재개·실패 보고 동작을 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class LogxFileWriterRobolectricTest {
    private lateinit var context: Application
    private lateinit var directory: File
    private val writers = mutableListOf<LogxFileWriter>()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        directory = File(context.cacheDir, "logx-writer-test").apply {
            deleteRecursively()
            mkdirs()
        }
        ShadowLog.clear()
    }

    @After
    fun tearDown() {
        writers.forEach { writer -> writer.shutdown() }
        directory.deleteRecursively()
        ShadowLog.clear()
    }

    /**
     * Verifies that queued lines create a file and are flushed immediately.<br><br>
     * 큐에 전달한 로그가 파일을 생성하고 즉시 flush되는지 검증합니다.<br>
     */
    @Test
    fun writeLinesCreatesFileAndWritesContent() {
        val writer = createWriter()
        val config = config()
        writer.writeLines(context, config, listOf("line1", "line2"), "ERROR")

        val file = waitForLogFile(directory)
        assertNotNull(file)
        val content = file!!.readText()
        assertTrue(content.contains("line1"))
        assertTrue(content.contains("line2"))
    }

    /**
     * Verifies that an empty line list does not create a file.<br><br>
     * 빈 로그 목록은 파일을 생성하지 않는지 검증합니다.<br>
     */
    @Test
    fun writeLines_skipsEmptyInput() {
        val writer = createWriter()

        writer.writeLines(context, config(), emptyList(), ERROR_TAG)

        assertFalse(directory.listFiles().orEmpty().any { file -> file.extension == "txt" })
    }

    /**
     * Verifies that a close command is ordered before the next write and the file is reopened.<br><br>
     * close 명령 다음의 로그가 순서대로 처리되어 기존 파일을 다시 여는지 검증합니다.<br>
     */
    @Test
    fun requestClose_reopensSessionForFollowingWrite() {
        val writer = createWriter()
        val config = config()
        writer.writeLines(context, config, listOf("first"), ERROR_TAG)
        val file = waitForLogFile(directory)
        assertNotNull(file)

        writer.requestClose()
        writer.writeLines(context, config, listOf("second"), ERROR_TAG)

        assertTrue(waitForContent(file!!, "second"))
        val content = file.readText()
        assertTrue(content.contains("first"))
        assertTrue(content.contains("second"))
    }

    /**
     * Verifies that every failed write emits an error instead of terminating the writer loop.<br><br>
     * 파일 쓰기가 반복 실패해도 Writer 루프를 종료하지 않고 매번 오류를 출력하는지 검증합니다.<br>
     */
    @Test
    fun writeLines_logsEveryFileWriteFailure() {
        val writer = createWriter()
        val invalidConfig = config(appName = "missing/InvalidApp")

        writer.writeLines(context, invalidConfig, listOf("first"), ERROR_TAG)
        writer.writeLines(context, invalidConfig, listOf("second"), ERROR_TAG)

        assertTrue(waitForErrorCount(ERROR_TAG, 2))
        assertEquals(
            2,
            ShadowLog.getLogsForTag(ERROR_TAG).count { item ->
                item.msg.contains("Failed to write log file")
            },
        )
    }

    /**
     * Verifies that an unresolved directory skips the file session and reports the path error.<br><br>
     * 저장 경로를 계산하지 못하면 파일 세션을 열지 않고 경로 오류를 출력하는지 검증합니다.<br>
     */
    @Test
    fun writeLines_skipsWriteWhenDirectoryCannotBeResolved() {
        val writer = createWriter()
        val blockingFile = File(directory, "not-a-directory").apply { writeText("file") }

        writer.writeLines(
            context,
            config(saveDirectory = blockingFile.absolutePath),
            listOf("line"),
            ERROR_TAG,
        )

        assertTrue(waitForErrorMessage(ERROR_TAG, "not a directory"))
        assertFalse(directory.listFiles().orEmpty().any { file -> file.extension == "txt" })
    }

    /**
     * Verifies that commands submitted after shutdown fail visibly.<br><br>
     * shutdown 이후 전달한 쓰기·종료 명령이 경고 없이 유실되지 않는지 검증합니다.<br>
     */
    @Test
    fun shutdown_rejectsFurtherCommandsWithErrors() {
        val writer = createWriter()
        writer.shutdown()
        ShadowLog.clear()

        writer.writeLines(context, config(), listOf("line"), ERROR_TAG)
        writer.requestClose()

        assertTrue(
            ShadowLog.getLogsForTag(ERROR_TAG).any { item ->
                item.msg.contains("Failed to enqueue file log lines")
            },
        )
        assertTrue(
            ShadowLog.getLogsForTag("LogxFileWriter").any { item ->
                item.msg.contains("Failed. WriterCommand.Close")
            },
        )
    }

    private fun createWriter(): LogxFileWriter = LogxFileWriter().also { writer -> writers += writer }

    private fun config(
        appName: String = "TestApp",
        saveDirectory: String = directory.absolutePath,
    ): LogxConfigSnapshot = LogxConfigSnapshot(
        isLogging = true,
        logTypes = enumValues<LogType>().toSet(),
        isLogTagBlockListEnabled = false,
        logTagBlockList = emptySet(),
        isSaveEnabled = true,
        storageType = LogStorageType.INTERNAL,
        saveDirectory = saveDirectory,
        appName = appName,
        skipPackages = emptySet(),
    )

    private fun waitForContent(file: File, expected: String, timeoutMs: Long = 2000): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (file.exists() && file.readText().contains(expected)) return true
            Thread.sleep(50)
        }
        return false
    }

    private fun waitForErrorCount(tag: String, expectedCount: Int, timeoutMs: Long = 2000): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val count = ShadowLog.getLogsForTag(tag).count { item -> item.msg.contains("Failed to write log file") }
            if (count >= expectedCount) return true
            Thread.sleep(50)
        }
        return false
    }

    private fun waitForErrorMessage(tag: String, expected: String, timeoutMs: Long = 2000): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (ShadowLog.getLogsForTag(tag).any { item -> item.msg.contains(expected) }) return true
            Thread.sleep(50)
        }
        return false
    }

    private fun waitForLogFile(directory: File, timeoutMs: Long = 2000): File? {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val file = directory.listFiles()?.firstOrNull { it.isFile && it.name.endsWith(".txt") }
            if (file != null && file.length() > 0) return file
            Thread.sleep(50)
        }
        return null
    }

    private companion object {
        const val ERROR_TAG = "WRITER_ERROR"
    }
}
