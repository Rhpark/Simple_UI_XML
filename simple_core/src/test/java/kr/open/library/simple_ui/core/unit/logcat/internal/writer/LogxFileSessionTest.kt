package kr.open.library.simple_ui.core.unit.logcat.internal.writer

import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.internal.writer.LogxFileConstants
import kr.open.library.simple_ui.core.logcat.internal.writer.LogxFileSession
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files

/**
 * Verifies file reuse, reset, close, and rotation behavior of a Logx file session.<br><br>
 * Logx 파일 세션의 재사용·초기화·종료·로테이션 동작을 검증합니다.<br>
 */
class LogxFileSessionTest {
    private lateinit var directory: File
    private lateinit var session: LogxFileSession

    @Before
    fun setUp() {
        directory = Files.createTempDirectory("logx-session-").toFile()
        session = LogxFileSession()
    }

    @After
    fun tearDown() {
        session.close()
        directory.deleteRecursively()
    }

    /**
     * Verifies that an unchanged signature reuses the same open writer.<br><br>
     * 저장 경로·저장 타입·앱 이름이 같으면 열린 Writer를 재사용하는지 검증합니다.<br>
     */
    @Test
    fun getWriter_reusesOpenWriterForSameSignature() {
        val firstWriter = session.getWriter(directory, APP_NAME, LogStorageType.INTERNAL)
        firstWriter.write("first")
        firstWriter.flush()

        val secondWriter = session.getWriter(directory, APP_NAME, LogStorageType.INTERNAL)

        assertSame(firstWriter, secondWriter)
        assertEquals(1, logFiles().size)
    }

    /**
     * Verifies that changing the application name starts a separate file session.<br><br>
     * 앱 이름이 변경되면 기존 Writer를 닫고 별도 파일 세션을 시작하는지 검증합니다.<br>
     */
    @Test
    fun getWriter_createsNewFileWhenSignatureChanges() {
        val firstWriter = session.getWriter(directory, APP_NAME, LogStorageType.INTERNAL)
        firstWriter.write("first")
        firstWriter.flush()

        val secondWriter = session.getWriter(directory, OTHER_APP_NAME, LogStorageType.INTERNAL)
        secondWriter.write("second")
        secondWriter.flush()

        assertNotSame(firstWriter, secondWriter)
        assertEquals(2, logFiles().size)
        assertTrue(logFiles().any { file -> file.name.startsWith("${APP_NAME}_") })
        assertTrue(logFiles().any { file -> file.name.startsWith("${OTHER_APP_NAME}_") })
    }

    /**
     * Verifies that close releases the writer and the next request appends to the same file.<br><br>
     * close 후 다음 요청이 Writer를 다시 열어 동일 파일에 이어 쓰는지 검증합니다.<br>
     */
    @Test
    fun close_reopensAndAppendsToSameFile() {
        val firstWriter = session.getWriter(directory, APP_NAME, LogStorageType.INTERNAL)
        firstWriter.write("first")
        firstWriter.newLine()
        firstWriter.flush()
        session.close()

        val secondWriter = session.getWriter(directory, APP_NAME, LogStorageType.INTERNAL)
        secondWriter.write("second")
        secondWriter.flush()
        session.close()

        val files = logFiles()
        assertNotSame(firstWriter, secondWriter)
        assertEquals(1, files.size)
        assertTrue(files.single().readText().contains("first"))
        assertTrue(files.single().readText().contains("second"))
    }

    /**
     * Verifies that a file at the size limit rotates to the first counted file.<br><br>
     * 파일이 10MB 기준에 도달하면 첫 번째 카운트 파일로 로테이션하는지 검증합니다.<br>
     */
    @Test
    fun getWriter_rotatesWhenActiveFileReachesSizeLimit() {
        val firstWriter = session.getWriter(directory, APP_NAME, LogStorageType.INTERNAL)
        firstWriter.write("seed")
        firstWriter.flush()
        session.close()
        val baseFile = logFiles().single()
        RandomAccessFile(baseFile, "rw").use { file ->
            file.setLength(LogxFileConstants.MAX_FILE_SIZE_BYTES)
        }

        val rotatedWriter = session.getWriter(directory, APP_NAME, LogStorageType.INTERNAL)
        rotatedWriter.write("rotated")
        rotatedWriter.flush()
        session.close()

        val files = logFiles()
        assertEquals(2, files.size)
        assertTrue(files.any { file -> file.name.endsWith("_1.txt") && file.readText().contains("rotated") })
    }

    private fun logFiles(): List<File> =
        directory.listFiles()?.filter { file -> file.isFile && file.extension == "txt" }.orEmpty()

    private companion object {
        const val APP_NAME = "SessionTestApp"
        const val OTHER_APP_NAME = "OtherSessionApp"
    }
}
