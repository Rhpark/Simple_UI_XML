package kr.open.library.simple_ui.core.unit.logcat.internal.file_writer

import java.nio.file.Files
import java.util.EnumSet
import kr.open.library.simple_ui.core.logcat.config.LogxConfig
import kr.open.library.simple_ui.core.logcat.internal.file_writer.LogxFileWriter
import kr.open.library.simple_ui.core.logcat.internal.file_writer.LogxFileWriterFactory
import kr.open.library.simple_ui.core.logcat.internal.file_writer.NoOpLogFileWriter
import kr.open.library.simple_ui.core.logcat.model.LogxType
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogxFileWriterFactoryTest {

    @Test
    fun `no-op writer swallows write and cleanup`() {
        val writer = NoOpLogFileWriter()

        writer.writeLog(LogxType.DEBUG, "Tag", "Message")
        writer.cleanup()
    }

    @Test
    fun `factory returns no-op writer when saving disabled`() {
        val config = LogxConfig(
            isDebugSave = false,
            debugLogTypeList = EnumSet.allOf(LogxType::class.java),
        )

        val writer = LogxFileWriterFactory.create(config, null)

        assertTrue(writer is NoOpLogFileWriter)
    }

    @Test
    fun `factory returns file writer when saving enabled`() {
        val tempDir = Files.createTempDirectory("logx-writer").toFile()
        val config = LogxConfig(
            isDebugSave = true,
            saveFilePath = tempDir.absolutePath,
            debugLogTypeList = EnumSet.allOf(LogxType::class.java),
        )

        val writer = LogxFileWriterFactory.create(config, null)

        assertTrue(writer is LogxFileWriter)
        writer.cleanup()
        tempDir.deleteRecursively()
    }
}
