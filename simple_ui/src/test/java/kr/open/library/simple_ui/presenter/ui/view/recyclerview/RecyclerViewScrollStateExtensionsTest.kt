package kr.open.library.simple_ui.presenter.ui.view.recyclerview

import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecyclerViewScrollStateExtensionsTest {

    @Test
    fun safeEmit_emitsWhenBufferAvailable() {
        val flow = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
        var failureCalled = false

        flow.safeEmit("value") { failureCalled = true }

        val secondAttemptSucceeded = flow.tryEmit("second")

        assertFalse(secondAttemptSucceeded)
        assertFalse(failureCalled)
    }

    @Test
    fun safeEmit_invokesFailureWhenBufferFull() {
        val flow = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 0)
        var failureCalled = false

        flow.safeEmit("value") { failureCalled = true }

        assertTrue(failureCalled)
    }
}
