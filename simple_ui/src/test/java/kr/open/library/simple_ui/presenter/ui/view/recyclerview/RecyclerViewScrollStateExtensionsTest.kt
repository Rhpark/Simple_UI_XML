package kr.open.library.simple_ui.presenter.ui.view.recyclerview

import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

@Ignore("임시로 비활성화")
class RecyclerViewScrollStateExtensionsTest {

    @Test
    fun safeEmit_emitsWhenBufferAvailable() {
        val flow = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
        var failureCalled = false

        val emitted = flow.safeEmit("value") { failureCalled = true }
        val secondAttempt = flow.tryEmit("second") // 버퍼가 남아 있으면 true

        assertTrue(emitted)
        assertTrue(secondAttempt) // SharedFlow는 여전히 수용 가능
        assertFalse(failureCalled)
    }

    @Test
    fun safeEmit_withoutSubscribers_returnsTrue() {
        val flow = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 0)
        var failureCalled = false

        val emitted = flow.safeEmit("value") { failureCalled = true }

        assertTrue(emitted)
        assertFalse(failureCalled)
    }
}
