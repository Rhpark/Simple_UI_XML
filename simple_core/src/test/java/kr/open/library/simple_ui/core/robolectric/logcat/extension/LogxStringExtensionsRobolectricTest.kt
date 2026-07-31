package kr.open.library.simple_ui.core.robolectric.logcat.extension

import android.os.Build
import android.os.Process
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.extension.logd
import kr.open.library.simple_ui.core.logcat.extension.loge
import kr.open.library.simple_ui.core.logcat.extension.logi
import kr.open.library.simple_ui.core.logcat.extension.logj
import kr.open.library.simple_ui.core.logcat.extension.logp
import kr.open.library.simple_ui.core.logcat.extension.logt
import kr.open.library.simple_ui.core.logcat.extension.logv
import kr.open.library.simple_ui.core.logcat.extension.logw
import kr.open.library.simple_ui.core.logcat.internal.pipeline.LogxPipeline
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for Kotlin Logx extension-function delegation.<br><br>
 * Kotlin Logx 확장 함수의 위임 계약을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class LogxStringExtensionsRobolectricTest {
    private lateinit var pipeline: LogxPipeline

    @Before
    fun setUp() {
        pipeline = mock(LogxPipeline::class.java)
        Logx.setPipelineForTest(pipeline)
    }

    @After
    fun tearDown() {
        Logx.resetPipelineForTest()
    }

    /**
     * Verifies standard extensions without tags delegate the receiver as the message.<br><br>
     * 태그 없는 표준 확장 함수가 수신 객체를 메시지로 위임하는지 검증합니다.<br>
     */
    @Test
    fun standardExtensions_delegateReceiverWithoutTag() {
        val message = TestMessage("message")

        message.logv()
        message.logd()
        message.logi()
        message.logw()
        message.loge()

        verify(pipeline).logStandard(LogType.VERBOSE, null, message, true, false)
        verify(pipeline).logStandard(LogType.DEBUG, null, message, true, false)
        verify(pipeline).logStandard(LogType.INFO, null, message, true, false)
        verify(pipeline).logStandard(LogType.WARN, null, message, true, false)
        verify(pipeline).logStandard(LogType.ERROR, null, message, true, false)
    }

    /**
     * Verifies tagged standard extensions delegate both tag and receiver.<br><br>
     * 태그가 있는 표준 확장 함수가 태그와 수신 객체를 함께 위임하는지 검증합니다.<br>
     */
    @Test
    fun standardExtensions_delegateReceiverWithTag() {
        val message = TestMessage("message")

        message.logv(TAG)
        message.logd(TAG)
        message.logi(TAG)
        message.logw(TAG)
        message.loge(TAG)

        verify(pipeline).logStandard(LogType.VERBOSE, TAG, message, true, true)
        verify(pipeline).logStandard(LogType.DEBUG, TAG, message, true, true)
        verify(pipeline).logStandard(LogType.INFO, TAG, message, true, true)
        verify(pipeline).logStandard(LogType.WARN, TAG, message, true, true)
        verify(pipeline).logStandard(LogType.ERROR, TAG, message, true, true)
    }

    /**
     * Verifies extended functions without tags delegate to their matching pipeline methods.<br><br>
     * 태그 없는 확장 로그 함수가 대응하는 파이프라인 메서드로 위임되는지 검증합니다.<br>
     */
    @Test
    fun extendedFunctions_delegateReceiverWithoutTag() {
        val message = TestMessage("message")
        val json = "{\"key\":\"value\"}"
        val threadId = Process.myTid().toLong()

        message.logp()
        message.logt()
        json.logj()

        verify(pipeline).logParent(null, message, true, false)
        verify(pipeline).logThread(null, message, true, false, threadId)
        verify(pipeline).logJson(null, json, false)
    }

    /**
     * Verifies tagged extended functions preserve tag and receiver semantics.<br><br>
     * 태그가 있는 확장 로그 함수가 태그와 수신 객체 의미를 보존하는지 검증합니다.<br>
     */
    @Test
    fun extendedFunctions_delegateReceiverWithTag() {
        val message = TestMessage("message")
        val json = "{\"key\":\"value\"}"
        val threadId = Process.myTid().toLong()

        message.logp(TAG)
        message.logt(TAG)
        json.logj(TAG)

        verify(pipeline).logParent(TAG, message, true, true)
        verify(pipeline).logThread(TAG, message, true, true, threadId)
        verify(pipeline).logJson(TAG, json, true)
    }

    private data class TestMessage(
        val value: String,
    )

    private companion object {
        const val TAG = "NETWORK"
    }
}
