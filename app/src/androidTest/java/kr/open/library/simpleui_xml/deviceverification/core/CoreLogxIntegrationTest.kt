package kr.open.library.simpleui_xml.deviceverification.core

import android.content.Context
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
internal class CoreLogxIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun coreP1001_logxFileOutputAppliesTypeAndTagFilters() {
        val originalConfig = captureConfig()
        val searchRoots = resolveSearchRoots(context)
        physicalDeviceRule.cleanup.register("CORE-P1-001 Logx 설정 및 테스트 파일 복원") {
            restoreConfigAndDeleteTestFiles(originalConfig, searchRoots)
        }

        Logx.setSaveEnabled(false)
        SystemClock.sleep(WRITER_CLOSE_WAIT_MILLIS)
        deleteTestLogFiles(searchRoots)
        configureLogxForTest()

        Logx.d(ALLOWED_TAG, ALLOWED_MESSAGE)
        Logx.d(BLOCKED_TAG, BLOCKED_MESSAGE)
        Logx.i(FILTERED_TYPE_TAG, FILTERED_TYPE_MESSAGE)

        val logFile = awaitTestLogFile(searchRoots)
        val content = logFile.readText()

        assertTrue(logFile.name.matches(TEST_FILE_NAME_PATTERN))
        assertTrue(content.contains("[D] $TEST_APP_NAME[$ALLOWED_TAG]"))
        assertTrue(content.contains(ALLOWED_MESSAGE))
        assertFalse(content.contains(BLOCKED_TAG))
        assertFalse(content.contains(BLOCKED_MESSAGE))
        assertFalse(content.contains(FILTERED_TYPE_TAG))
        assertFalse(content.contains(FILTERED_TYPE_MESSAGE))
    }

    private fun configureLogxForTest() {
        Logx.initialize(context)
        Logx.setLogging(true)
        Logx.setLogTypes(setOf(LogType.DEBUG))
        Logx.setLogTagBlockListEnabled(true)
        Logx.setLogTagBlockList(setOf(BLOCKED_TAG))
        Logx.setStorageType(LogStorageType.APP_EXTERNAL)
        Logx.setAppName(TEST_APP_NAME)
        Logx.setSaveEnabled(true)
    }

    private fun captureConfig(): LogxConfigBackup = LogxConfigBackup(
        isLogging = Logx.isLogging(),
        logTypes = Logx.getLogTypes(),
        isLogTagBlockListEnabled = Logx.isLogTagBlockListEnabled(),
        logTagBlockList = Logx.getLogTagBlockList(),
        isSaveEnabled = Logx.isSaveEnabled(),
        storageType = Logx.getStorageType(),
        appName = Logx.getAppName(),
    )

    private fun restoreConfigAndDeleteTestFiles(
        config: LogxConfigBackup,
        searchRoots: List<File>,
    ) {
        Logx.setSaveEnabled(false)
        SystemClock.sleep(WRITER_CLOSE_WAIT_MILLIS)
        deleteTestLogFiles(searchRoots)
        Logx.setLogging(config.isLogging)
        Logx.setLogTypes(config.logTypes)
        Logx.setLogTagBlockListEnabled(config.isLogTagBlockListEnabled)
        Logx.setLogTagBlockList(config.logTagBlockList)
        Logx.setStorageType(config.storageType)
        Logx.setAppName(config.appName)
        Logx.setSaveEnabled(config.isSaveEnabled)
    }

    private fun awaitTestLogFile(searchRoots: List<File>): File {
        val deadline = SystemClock.elapsedRealtime() + FILE_WRITE_TIMEOUT_MILLIS
        while (SystemClock.elapsedRealtime() < deadline) {
            val logFile = findTestLogFiles(searchRoots).firstOrNull { file ->
                runCatching { file.readText().contains(ALLOWED_MESSAGE) }.getOrDefault(false)
            }
            if (logFile != null) return logFile
            SystemClock.sleep(FILE_POLL_INTERVAL_MILLIS)
        }

        fail(
            "CORE-P1-001 로그 파일 생성 시간 초과: " +
                "roots=${searchRoots.joinToString { it.absolutePath }}, " +
                physicalDeviceRule.environment.asDiagnosticText(),
        )
        error("JUnit fail must throw")
    }

    private fun deleteTestLogFiles(searchRoots: List<File>) {
        findTestLogFiles(searchRoots).forEach { file ->
            check(file.delete()) {
                "CORE-P1-001 테스트 로그 삭제 실패: ${file.absolutePath}"
            }
        }
    }

    private fun findTestLogFiles(searchRoots: List<File>): List<File> = searchRoots
        .asSequence()
        .filter(File::exists)
        .flatMap { root -> root.walkTopDown().asSequence() }
        .filter(File::isFile)
        .filter { file -> file.name.startsWith("${TEST_APP_NAME}_") && file.extension == "txt" }
        .toList()

    private fun resolveSearchRoots(context: Context): List<File> = buildList {
        add(context.filesDir)
        context.getExternalFilesDirs(null).filterNotNull().forEach(::add)
    }.distinctBy(File::getAbsolutePath)

    private data class LogxConfigBackup(
        val isLogging: Boolean,
        val logTypes: Set<LogType>,
        val isLogTagBlockListEnabled: Boolean,
        val logTagBlockList: Set<String>,
        val isSaveEnabled: Boolean,
        val storageType: LogStorageType,
        val appName: String,
    )

    private companion object {
        const val TEST_APP_NAME = "DeviceVerificationCoreP1001"
        const val ALLOWED_TAG = "ALLOWED_TAG"
        const val ALLOWED_MESSAGE = "CORE-P1-001 allowed file message"
        const val BLOCKED_TAG = "BLOCKED_TAG"
        const val BLOCKED_MESSAGE = "CORE-P1-001 blocked file message"
        const val FILTERED_TYPE_TAG = "FILTERED_INFO_TAG"
        const val FILTERED_TYPE_MESSAGE = "CORE-P1-001 filtered info message"
        const val WRITER_CLOSE_WAIT_MILLIS = 300L
        const val FILE_WRITE_TIMEOUT_MILLIS = 5_000L
        const val FILE_POLL_INTERVAL_MILLIS = 50L
        val TEST_FILE_NAME_PATTERN = Regex(
            "${TEST_APP_NAME}_\\d{4}_\\d{2}_\\d{2}__\\d{2}-\\d{2}-\\d{2}-\\d{3}(?:_\\d+)?\\.txt",
        )
    }
}
