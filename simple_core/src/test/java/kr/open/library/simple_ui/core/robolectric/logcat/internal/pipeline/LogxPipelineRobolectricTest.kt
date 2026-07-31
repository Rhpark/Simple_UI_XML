package kr.open.library.simple_ui.core.robolectric.logcat.internal.pipeline

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigSnapshot
import kr.open.library.simple_ui.core.logcat.config.LogxConfigStore
import kr.open.library.simple_ui.core.logcat.internal.pipeline.LogxPipeline
import kr.open.library.simple_ui.core.logcat.internal.writer.LogxFileWriter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.verifyNoInteractions
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

/**
 * Robolectric tests for Logx pipeline filtering, formatting, and output routing.<br><br>
 * Logx 파이프라인의 필터링·포맷팅·출력 연결을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class LogxPipelineRobolectricTest {
    private lateinit var app: Application
    private lateinit var fileWriter: LogxFileWriter

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        fileWriter = mock(LogxFileWriter::class.java)
        resetConfig()
        ShadowLog.clear()
    }

    @After
    fun tearDown() {
        resetConfig()
        ShadowLog.clear()
    }

    /**
     * Verifies standard output prefix, Android level, and null-message conversion.<br><br>
     * 표준 출력의 접두사·Android 레벨·null 메시지 변환을 검증합니다.<br>
     */
    @Test
    fun logStandard_writesExpectedPrefixLevelAndNullMessage() {
        val pipeline = createPipeline()

        pipeline.logStandard(LogType.DEBUG, ALLOWED_TAG, null, true, true)

        val logItem = ShadowLog.getLogsForTag("$APP_NAME[$ALLOWED_TAG]").single()
        assertEquals(Log.DEBUG, logItem.type)
        assertTrue(logItem.msg.startsWith("("))
        assertTrue(logItem.msg.contains(")."))
        assertTrue(logItem.msg.endsWith(" - null"))
    }

    /**
     * Verifies that PARENT output emits parent and current markers as two Logcat entries.<br><br>
     * PARENT 출력이 부모·현재 마커를 두 개의 Logcat 항목으로 출력하는지 검증합니다.<br>
     */
    @Test
    fun logParent_writesTwoMarkedLines() {
        val pipeline = createPipeline()

        pipeline.logParent(ALLOWED_TAG, MESSAGE, true, true)

        val logs = ShadowLog.getLogsForTag("$APP_NAME[$ALLOWED_TAG]")
        assertEquals(2, logs.size)
        assertTrue(logs[0].msg.contains("┌[PARENT]"))
        assertTrue(logs[1].msg.contains("└[PARENT]"))
        assertTrue(logs[1].msg.endsWith(" - $MESSAGE"))
    }

    /**
     * Verifies THREAD output includes the supplied thread id and message.<br><br>
     * THREAD 출력에 전달한 스레드 ID와 메시지가 포함되는지 검증합니다.<br>
     */
    @Test
    fun logThread_writesProvidedThreadIdAndMessage() {
        val pipeline = createPipeline()

        pipeline.logThread(null, MESSAGE, true, false, THREAD_ID)

        val logItem = ShadowLog.getLogsForTag(APP_NAME).single()
        assertEquals(Log.DEBUG, logItem.type)
        assertTrue(logItem.msg.contains("[TID = $THREAD_ID]"))
        assertTrue(logItem.msg.endsWith(" - $MESSAGE"))
    }

    /**
     * Verifies JSON output is emitted as one multiline Logcat message with fixed markers.<br><br>
     * JSON 출력이 고정 마커를 포함한 단일 멀티라인 Logcat 메시지인지 검증합니다.<br>
     */
    @Test
    fun logJson_writesOneMultilineMessage() {
        val pipeline = createPipeline()

        pipeline.logJson(JSON_TAG, "{\"key\":\"value\"}", true)

        val logs = ShadowLog.getLogsForTag("$APP_NAME[$JSON_TAG]")
        assertEquals(1, logs.size)
        assertTrue(logs.single().msg.contains("[JSON]"))
        assertTrue(logs.single().msg.contains("\n    \"key\": \"value\"\n"))
        assertTrue(logs.single().msg.endsWith("[End]"))
    }

    /**
     * Verifies disabled logging prevents both console and file output.<br><br>
     * 로그 비활성화가 콘솔과 파일 출력을 모두 차단하는지 검증합니다.<br>
     */
    @Test
    fun logStandard_skipsConsoleAndFile_whenLoggingIsDisabled() {
        LogxConfigStore.setLogging(false)
        LogxConfigStore.setSaveEnabled(true)
        val pipeline = createPipeline(contextProvider = { app })

        pipeline.logStandard(LogType.DEBUG, FILE_TAG, MESSAGE, true, true)

        assertTrue(ShadowLog.getLogs().isEmpty())
        verifyNoInteractions(fileWriter)
    }

    /**
     * Verifies enabled file logging routes formatted lines and the original error tag to the writer.<br><br>
     * 파일 저장 활성화 시 포맷된 라인과 원본 오류 태그를 Writer로 전달하는지 검증합니다.<br>
     */
    @Test
    fun logStandard_routesFormattedLinesToFileWriter_whenSavingIsEnabled() {
        LogxConfigStore.setSaveEnabled(true)
        val pipeline = createPipeline(contextProvider = { app })

        pipeline.logStandard(LogType.DEBUG, FILE_TAG, MESSAGE, true, true)

        val invocation = mockingDetails(fileWriter).invocations.single { it.method.name == "writeLines" }
        val config = invocation.arguments[1] as LogxConfigSnapshot
        val lines = invocation.arguments[2] as List<*>

        assertSame(app, invocation.arguments[0])
        assertTrue(config.isSaveEnabled)
        assertEquals(FILE_TAG, invocation.arguments[3])
        assertEquals(1, lines.size)
        assertTrue(lines.single().toString().contains("[D] $APP_NAME[$FILE_TAG]"))
        assertTrue(lines.single().toString().endsWith(" - $MESSAGE"))
    }

    /**
     * Verifies a missing file context produces one development warning and no writer interaction.<br><br>
     * 파일 Context가 없을 때 개발 경고를 한 번만 출력하고 Writer를 호출하지 않는지 검증합니다.<br>
     */
    @Test
    fun logStandard_warnsOnceAndSkipsFileWriter_whenContextIsMissing() {
        LogxConfigStore.setSaveEnabled(true)
        app.applicationInfo.flags = app.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE
        val pipeline = createPipeline()
        pipeline.setDevelopmentMode(app)

        pipeline.logStandard(LogType.DEBUG, MISSING_CONTEXT_TAG, MESSAGE, true, true)
        pipeline.logStandard(LogType.DEBUG, MISSING_CONTEXT_TAG, MESSAGE, true, true)

        val warnings = ShadowLog.getLogsForTag(MISSING_CONTEXT_TAG)
        assertEquals(1, warnings.size)
        assertEquals(Log.ERROR, warnings.single().type)
        assertTrue(warnings.single().msg.contains("Context is not initialized"))
        verifyNoInteractions(fileWriter)
    }

    private fun createPipeline(contextProvider: () -> Application? = { null }): LogxPipeline = LogxPipeline(
        contextProvider = contextProvider,
        fileWriter = fileWriter,
    )

    private fun resetConfig() {
        LogxConfigStore.setLogging(true)
        LogxConfigStore.setLogTypes(enumValues<LogType>().toSet())
        LogxConfigStore.setLogTagBlockListEnabled(false)
        LogxConfigStore.setLogTagBlockList(emptySet())
        LogxConfigStore.setSaveEnabled(false)
        LogxConfigStore.setStorageType(LogStorageType.APP_EXTERNAL)
        LogxConfigStore.setSaveDirectory(null)
        LogxConfigStore.setAppName(APP_NAME)
    }

    private companion object {
        const val APP_NAME = "TestApp"
        const val ALLOWED_TAG = "NETWORK"
        const val JSON_TAG = "JSON"
        const val FILE_TAG = "FILE"
        const val MISSING_CONTEXT_TAG = "MISSING_CONTEXT"
        const val MESSAGE = "message"
        const val THREAD_ID = 77L
    }
}
