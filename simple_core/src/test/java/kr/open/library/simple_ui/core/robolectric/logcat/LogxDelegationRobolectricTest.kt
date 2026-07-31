package kr.open.library.simple_ui.core.robolectric.logcat

import android.os.Build
import android.os.Process
import android.util.Log
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigStore
import kr.open.library.simple_ui.core.logcat.internal.pipeline.LogxPipeline
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

/**
 * Robolectric tests for the public Logx entry-point delegation contract.<br><br>
 * 공개 Logx 진입점의 파이프라인 위임 계약을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class LogxDelegationRobolectricTest {
    private lateinit var pipeline: LogxPipeline

    @Before
    fun setUp() {
        pipeline = mock(LogxPipeline::class.java)
        Logx.setPipelineForTest(pipeline)
        resetConfig()
        ShadowLog.clear()
    }

    @After
    fun tearDown() {
        Logx.resetPipelineForTest()
        resetConfig()
        ShadowLog.clear()
    }

    /**
     * Verifies that message-less standard overloads preserve type and absence flags.<br><br>
     * 메시지가 없는 표준 오버로드가 타입과 미입력 상태를 그대로 전달하는지 검증합니다.<br>
     */
    @Test
    fun standardLogOverloads_delegateWithoutMessage() {
        Logx.v()
        Logx.d()
        Logx.i()
        Logx.w()
        Logx.e()

        verify(pipeline).logStandard(LogType.VERBOSE, null, null, false, false)
        verify(pipeline).logStandard(LogType.DEBUG, null, null, false, false)
        verify(pipeline).logStandard(LogType.INFO, null, null, false, false)
        verify(pipeline).logStandard(LogType.WARN, null, null, false, false)
        verify(pipeline).logStandard(LogType.ERROR, null, null, false, false)
    }

    /**
     * Verifies that standard message overloads preserve the original message object.<br><br>
     * 표준 메시지 오버로드가 원본 메시지 객체를 그대로 전달하는지 검증합니다.<br>
     */
    @Test
    fun standardLogOverloads_delegateWithMessage() {
        val message = TestMessage("message")

        Logx.v(message)
        Logx.d(message)
        Logx.i(message)
        Logx.w(message)
        Logx.e(message)

        verify(pipeline).logStandard(LogType.VERBOSE, null, message, true, false)
        verify(pipeline).logStandard(LogType.DEBUG, null, message, true, false)
        verify(pipeline).logStandard(LogType.INFO, null, message, true, false)
        verify(pipeline).logStandard(LogType.WARN, null, message, true, false)
        verify(pipeline).logStandard(LogType.ERROR, null, message, true, false)
    }

    /**
     * Verifies that tagged standard overloads preserve both tag and message.<br><br>
     * 태그가 있는 표준 오버로드가 태그와 메시지를 모두 그대로 전달하는지 검증합니다.<br>
     */
    @Test
    fun standardLogOverloads_delegateWithTagAndMessage() {
        val message = TestMessage("message")

        Logx.v(TAG, message)
        Logx.d(TAG, message)
        Logx.i(TAG, message)
        Logx.w(TAG, message)
        Logx.e(TAG, message)

        verify(pipeline).logStandard(LogType.VERBOSE, TAG, message, true, true)
        verify(pipeline).logStandard(LogType.DEBUG, TAG, message, true, true)
        verify(pipeline).logStandard(LogType.INFO, TAG, message, true, true)
        verify(pipeline).logStandard(LogType.WARN, TAG, message, true, true)
        verify(pipeline).logStandard(LogType.ERROR, TAG, message, true, true)
    }

    /**
     * Verifies PARENT and JSON overload delegation including tag-presence flags.<br><br>
     * PARENT와 JSON 오버로드가 태그 입력 상태까지 정확히 전달하는지 검증합니다.<br>
     */
    @Test
    fun parentAndJsonOverloads_delegateToMatchingPipelineMethods() {
        val message = TestMessage("message")
        val json = "{\"key\":\"value\"}"

        Logx.p()
        Logx.p(message)
        Logx.p(TAG, message)
        Logx.j(json)
        Logx.j(TAG, json)

        verify(pipeline).logParent(null, null, false, false)
        verify(pipeline).logParent(null, message, true, false)
        verify(pipeline).logParent(TAG, message, true, true)
        verify(pipeline).logJson(null, json, false)
        verify(pipeline).logJson(TAG, json, true)
    }

    /**
     * Verifies THREAD overload delegation with the current Android thread id.<br><br>
     * THREAD 오버로드가 현재 Android 스레드 ID와 함께 위임되는지 검증합니다.<br>
     */
    @Test
    fun threadOverloads_delegateWithCurrentThreadId() {
        val message = TestMessage("message")
        val threadId = Process.myTid().toLong()

        Logx.t()
        Logx.t(message)
        Logx.t(TAG, message)

        verify(pipeline).logThread(null, null, false, false, threadId)
        verify(pipeline).logThread(null, message, true, false, threadId)
        verify(pipeline).logThread(TAG, message, true, true, threadId)
    }

    /**
     * Verifies that public configuration setters and getters expose the latest values.<br><br>
     * 공개 설정 Setter와 Getter가 최신 설정값을 노출하는지 검증합니다.<br>
     */
    @Test
    fun configurationAccessors_roundTripPublicValues() {
        Logx.setLogging(false)
        Logx.setLogTypes(setOf(LogType.ERROR))
        Logx.setLogTagBlockListEnabled(true)
        Logx.setLogTagBlockList(setOf(TAG))
        Logx.setSaveEnabled(false)
        Logx.setStorageType(LogStorageType.INTERNAL)
        Logx.setAppName(APP_NAME)
        Logx.addSkipPackages(setOf(SKIP_PACKAGE))

        assertFalse(Logx.isLogging())
        assertEquals(setOf(LogType.ERROR), Logx.getLogTypes())
        assertTrue(Logx.isLogTagBlockListEnabled())
        assertEquals(setOf(TAG), Logx.getLogTagBlockList())
        assertFalse(Logx.isSaveEnabled())
        assertEquals(LogStorageType.INTERNAL, Logx.getStorageType())
        assertEquals(APP_NAME, Logx.getAppName())
        assertTrue(Logx.getSkipPackages().contains(SKIP_PACKAGE))
    }

    /**
     * Verifies the documented sanitizing behavior for blank blocklist inputs.<br><br>
     * 차단 목록의 빈 문자열·공백 입력을 제거하고 경고하는 기존 계약을 검증합니다.<br>
     */
    @Test
    fun setLogTagBlockList_removesBlankInputsAndLogsWarning() {
        Logx.setLogTagBlockList(setOf(TAG, "", "   "))

        assertEquals(setOf(TAG), Logx.getLogTagBlockList())
        assertTrue(
            ShadowLog.getLogs().any { item ->
                item.type == Log.ERROR && item.msg.contains("blank tags")
            },
        )
    }

    private fun resetConfig() {
        LogxConfigStore.setLogging(true)
        LogxConfigStore.setLogTypes(enumValues<LogType>().toSet())
        LogxConfigStore.setLogTagBlockListEnabled(false)
        LogxConfigStore.setLogTagBlockList(emptySet())
        LogxConfigStore.setSaveEnabled(false)
        LogxConfigStore.setStorageType(LogStorageType.APP_EXTERNAL)
        LogxConfigStore.setSaveDirectory(null)
        LogxConfigStore.setAppName("AppName")
    }

    private data class TestMessage(
        val value: String,
    )

    private companion object {
        const val TAG = "NETWORK"
        const val APP_NAME = "TestApp"
        const val SKIP_PACKAGE = "com.example.logger."
    }
}
