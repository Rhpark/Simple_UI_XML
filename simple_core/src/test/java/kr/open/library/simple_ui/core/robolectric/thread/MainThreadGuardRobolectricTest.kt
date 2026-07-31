package kr.open.library.simple_ui.core.robolectric.thread

import android.os.Build
import kr.open.library.simple_ui.core.BuildConfig
import kr.open.library.simple_ui.core.thread.assertMainThreadDebug
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicReference

/**
 * Robolectric tests for the main-thread guard's library build-variant policy.<br><br>
 * 메인 스레드 가드의 라이브러리 빌드 변형별 정책을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class MainThreadGuardRobolectricTest {
    /**
     * Verifies that main-thread calls always pass regardless of the library build variant.<br><br>
     * 라이브러리 빌드 변형과 관계없이 메인 스레드 호출이 정상 통과하는지 검증합니다.<br>
     */
    @Test
    fun assertMainThreadDebug_passes_onMainThread() {
        assertMainThreadDebug(API_NAME)
    }

    /**
     * Verifies that worker-thread calls fail only when the library variant is Debug.<br><br>
     * 워커 스레드 호출이 라이브러리 Debug 변형에서만 실패하는지 검증합니다.<br>
     */
    @Test
    fun assertMainThreadDebug_followsBuildVariantPolicy_onWorkerThread() {
        val failure = captureWorkerFailure {
            assertMainThreadDebug(API_NAME)
        }

        if (BuildConfig.DEBUG) {
            assertTrue(failure is IllegalStateException)
            assertEquals("$API_NAME must be called on Main thread", failure?.message)
        } else {
            assertNull(failure)
        }
    }

    private fun captureWorkerFailure(block: () -> Unit): Throwable? {
        val failure = AtomicReference<Throwable?>()
        val worker = Thread {
            failure.set(runCatching(block).exceptionOrNull())
        }

        worker.start()
        worker.join()

        return failure.get()
    }

    private companion object {
        const val API_NAME = "MainThreadGuardTest.api"
    }
}
