package kr.open.library.simple_ui.core.robolectric.logcat.internal.writer

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigSnapshot
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.internal.writer.LogxFileWriter
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class LogxFileWriterRobolectricTest {
    @Test
    fun writeLinesCreatesFileAndWritesContent() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val dir = File(context.cacheDir, "logx_test").apply {
            if (exists()) deleteRecursively()
            mkdirs()
        }

        val config = LogxConfigSnapshot(
            isLogging = true,
            logTypes = enumValues<LogType>().toSet(),
            isLogTagBlockListEnabled = false,
            logTagBlockList = emptySet(),
            isSaveEnabled = true,
            storageType = LogStorageType.INTERNAL,
            saveDirectory = dir.absolutePath,
            appName = "TestApp",
            skipPackages = emptySet(),
        )

        val writer = LogxFileWriter()
        writer.writeLines(context, config, listOf("line1", "line2"), "ERROR")

        val file = waitForLogFile(dir)
        assertNotNull(file)
        val content = file!!.readText()
        assertTrue(content.contains("line1"))
        assertTrue(content.contains("line2"))

        writer.requestClose()
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
}
