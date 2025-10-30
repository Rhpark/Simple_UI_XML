package kr.open.library.simple_ui.presenter.ui.view.recyclerview

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecyclerViewScrollStateImpKtTest {

    @Test
    fun safeEmit_returnsTrueWhenEmissionSucceeds() = runBlocking {
        val flow = MutableSharedFlow<Int>(replay = 1)
        var failureCalled = false

        val result = flow.safeEmit(42) { failureCalled = true }

        assertTrue(result)
        assertFalse(failureCalled)
        assertEquals(42, flow.first())
    }

    @Test
    fun safeEmit_invokesFailureWhenEmissionFails() = runBlocking {
        val flow = MutableSharedFlow<Int>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.SUSPEND  // 기본값
        )

        // 구독자 추가 (느리게 소비)
        val collectedValues = mutableListOf<Int>()
        val job = launch {
            flow.collect {
                delay(500) // 의도적으로 느리게
                collectedValues.add(it)
            }
        }
        delay(10) // 구독 확립 대기

        // 버퍼 채우기
        assertTrue(flow.tryEmit(1))

        var failureCalled = false

        // 버퍼 가득 + 구독자가 아직 소비 안함 = 실패
        val result = flow.safeEmit(7) { failureCalled = true }

        assertFalse("Should fail when buffer is full", result)
        assertTrue("Failure callback should be invoked", failureCalled)

        job.cancel()
    }

}
